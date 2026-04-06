package render;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IosOpenGLESBridge implements OpenGLESBridge {

    private static final String[] OPENGL_CLASS_CANDIDATES = {
        "apple.opengles.OpenGLES",
        "org.robovm.apple.opengles.OpenGLES"
    };

    private static final String[] PTR_FACTORY_CANDIDATES = {
        "org.moe.natj.general.ptr.impl.PtrFactory"
    };

    private static final Object NO_MATCH = new Object();
    private static final Object INCOMPATIBLE = new Object();

    private final Class<?> ptrFactoryType;
    private final String runtimeName;
    private final Map<String, List<Method>> methodsByName = new HashMap<>();
    private final Map<Integer, Map<String, Integer>> uniformLocationCache = new HashMap<>();

    private int currentProgramId = -1;

    public IosOpenGLESBridge() {
        Class<?> resolvedType = null;
        String resolvedRuntime = "ios";
        for (String candidate : OPENGL_CLASS_CANDIDATES) {
            try {
                resolvedType = Class.forName(candidate);
                resolvedRuntime = candidate.startsWith("apple.") ? "moe" : "robovm";
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (resolvedType == null) {
            throw new OpenGLBridgeException(
                "iOS OpenGL ES bindings were not found. Add a Multi-OS Engine or RoboVM iOS runtime before creating IosOpenGLESBridge."
            );
        }

        this.ptrFactoryType = loadOptionalClass(PTR_FACTORY_CANDIDATES);
        this.runtimeName = resolvedRuntime;
        indexMethods(resolvedType);
    }

    public static boolean isAvailable() {
        for (String candidate : OPENGL_CLASS_CANDIDATES) {
            try {
                Class.forName(candidate);
                return true;
            } catch (ClassNotFoundException ignored) {
            }
        }
        return false;
    }

    public String getRuntimeName() {
        return this.runtimeName;
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        invokeRequired("glViewport", x, y, width, height);
    }

    @Override
    public void clearColor(float red, float green, float blue, float alpha) {
        invokeRequired("glClearColor", red, green, blue, alpha);
    }

    @Override
    public void clear(int mask) {
        invokeRequired("glClear", mask);
    }

    @Override
    public void enable(int capability) {
        invokeRequired("glEnable", capability);
    }

    @Override
    public void disable(int capability) {
        invokeRequired("glDisable", capability);
    }

    @Override
    public void blendFunc(int sourceFactor, int destinationFactor) {
        invokeRequired("glBlendFunc", sourceFactor, destinationFactor);
    }

    @Override
    public void activeTexture(int textureUnit) {
        invokeRequired("glActiveTexture", textureUnit);
    }

    @Override
    public void useProgram(int programId) {
        invokeRequired("glUseProgram", programId);
        this.currentProgramId = programId;
    }

    @Override
    public void bindTexture(int target, int textureId) {
        invokeRequired("glBindTexture", target, textureId);
    }

    @Override
    public int genTexture() {
        Object directResult = tryInvoke("glGenTextures");
        if (directResult != NO_MATCH && directResult != null) {
            return asInt(directResult, "glGenTextures");
        }

        int[] ids = new int[1];
        if (tryInvoke("glGenTextures", 1, ids, 0) != NO_MATCH) {
            return ids[0];
        }

        Object intPointer = createIntReference();
        if (intPointer != null && tryInvoke("glGenTextures", 1, intPointer) != NO_MATCH) {
            return readIntReference(intPointer);
        }

        throw new OpenGLBridgeException(
            "No compatible iOS OpenGL ES glGenTextures signature was found for runtime: " + this.runtimeName
        );
    }

    @Override
    public void deleteTexture(int textureId) {
        if (tryInvoke("glDeleteTextures", 1, new int[] {textureId}, 0) != NO_MATCH) {
            return;
        }

        Object pointer = createIntReference(textureId);
        if (pointer != null && tryInvoke("glDeleteTextures", 1, pointer) != NO_MATCH) {
            return;
        }

        invokeRequired("glDeleteTextures", textureId);
    }

    @Override
    public void texParameteri(int target, int parameterName, int value) {
        invokeRequired("glTexParameteri", target, parameterName, value);
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type, byte[] data) {
        ByteBuffer buffer = data == null ? null : ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        if (buffer != null) {
            buffer.put(data);
            buffer.flip();
        }

        if (tryInvoke("glTexImage2D", target, level, internalFormat, width, height, 0, format, type, buffer) != NO_MATCH) {
            return;
        }

        Object pointer = createPointer("newByteArray", data);
        if (pointer != null && tryInvoke("glTexImage2D", target, level, internalFormat, width, height, 0, format, type, pointer) != NO_MATCH) {
            return;
        }

        if (data == null && tryInvoke("glTexImage2D", target, level, internalFormat, width, height, 0, format, type, 0L) != NO_MATCH) {
            return;
        }

        throw new OpenGLBridgeException(
            "No compatible iOS OpenGL ES glTexImage2D signature was found for runtime: " + this.runtimeName
        );
    }

    @Override
    public void setUniform1f(String name, float value) {
        int location = resolveUniformLocation(name);
        if (location < 0) {
            return;
        }
        invokeRequired("glUniform1f", location, value);
    }

    @Override
    public void setUniform1i(String name, int value) {
        int location = resolveUniformLocation(name);
        if (location < 0) {
            return;
        }
        invokeRequired("glUniform1i", location, value);
    }

    @Override
    public void setUniformMatrix4(String name, float[] values) {
        if (values == null || values.length == 0 || values.length % 16 != 0) {
            throw new OpenGLBridgeException("Matrix uniforms must contain one or more 4x4 matrices.");
        }

        int location = resolveUniformLocation(name);
        if (location < 0) {
            return;
        }

        int matrixCount = values.length / 16;

        if (tryInvoke("glUniformMatrix4fv", location, matrixCount, false, values, 0) != NO_MATCH) {
            return;
        }

        FloatBuffer buffer = ByteBuffer
            .allocateDirect(values.length * Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        buffer.put(values);
        buffer.flip();

        if (tryInvoke("glUniformMatrix4fv", location, matrixCount, false, buffer) != NO_MATCH) {
            return;
        }

        if (tryInvoke("glUniformMatrix4fv", location, matrixCount, (byte) 0, buffer) != NO_MATCH) {
            return;
        }

        Object pointer = createPointer("newFloatArray", values);
        if (pointer != null && tryInvoke("glUniformMatrix4fv", location, matrixCount, (byte) 0, pointer) != NO_MATCH) {
            return;
        }

        if (pointer != null && tryInvoke("glUniformMatrix4fv", location, matrixCount, false, pointer) != NO_MATCH) {
            return;
        }

        throw new OpenGLBridgeException(
            "No compatible iOS OpenGL ES glUniformMatrix4fv signature was found for runtime: " + this.runtimeName
        );
    }

    @Override
    public void drawArrays(int mode, int first, int count) {
        invokeRequired("glDrawArrays", mode, first, count);
    }

    @Override
    public void drawElements(int mode, int count, int type, int offset) {
        if (tryInvoke("glDrawElements", mode, count, type, offset) != NO_MATCH) {
            return;
        }

        invokeRequired("glDrawElements", mode, count, type, (long) offset);
    }

    @Override
    public int createShader(int shaderType) {
        return asInt(invokeRequired("glCreateShader", shaderType), "glCreateShader");
    }

    @Override
    public void shaderSource(int shaderId, String source) {
        invokeRequired("glShaderSource", shaderId, source);
    }

    @Override
    public void compileShader(int shaderId) {
        invokeRequired("glCompileShader", shaderId);
    }

    @Override
    public void deleteShader(int shaderId) {
        invokeRequired("glDeleteShader", shaderId);
    }

    @Override
    public int createProgram() {
        return asInt(invokeRequired("glCreateProgram"), "glCreateProgram");
    }

    @Override
    public void attachShader(int programId, int shaderId) {
        invokeRequired("glAttachShader", programId, shaderId);
    }

    @Override
    public void linkProgram(int programId) {
        invokeRequired("glLinkProgram", programId);
    }

    @Override
    public void deleteProgram(int programId) {
        invokeRequired("glDeleteProgram", programId);
        this.uniformLocationCache.remove(programId);
        if (this.currentProgramId == programId) {
            this.currentProgramId = -1;
        }
    }

    @Override
    public int getAttribLocation(int programId, String name) {
        return asInt(invokeRequired("glGetAttribLocation", programId, name), "glGetAttribLocation");
    }

    @Override
    public void enableVertexAttribArray(int index) {
        invokeRequired("glEnableVertexAttribArray", index);
    }

    @Override
    public void disableVertexAttribArray(int index) {
        invokeRequired("glDisableVertexAttribArray", index);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        if (tryInvoke("glVertexAttribPointer", index, size, type, normalized, stride, offset) != NO_MATCH) {
            return;
        }

        if (tryInvoke("glVertexAttribPointer", index, size, type, normalized, stride, (long) offset) != NO_MATCH) {
            return;
        }

        invokeRequired("glVertexAttribPointer", index, size, type, (byte) (normalized ? 1 : 0), stride, (long) offset);
    }

    @Override
    public int genBuffer() {
        Object directResult = tryInvoke("glGenBuffers");
        if (directResult != NO_MATCH && directResult != null) {
            return asInt(directResult, "glGenBuffers");
        }

        int[] ids = new int[1];
        if (tryInvoke("glGenBuffers", 1, ids, 0) != NO_MATCH) {
            return ids[0];
        }

        Object intPointer = createIntReference();
        if (intPointer != null && tryInvoke("glGenBuffers", 1, intPointer) != NO_MATCH) {
            return readIntReference(intPointer);
        }

        throw new OpenGLBridgeException(
            "No compatible iOS OpenGL ES glGenBuffers signature was found for runtime: " + this.runtimeName
        );
    }

    @Override
    public void bindBuffer(int target, int bufferId) {
        invokeRequired("glBindBuffer", target, bufferId);
    }

    @Override
    public void bufferData(int target, float[] data, int usage) {
        FloatBuffer buffer = ByteBuffer
            .allocateDirect(data.length * Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        buffer.put(data);
        buffer.flip();

        int sizeBytes = data.length * Float.BYTES;
        if (tryInvoke("glBufferData", target, sizeBytes, buffer, usage) != NO_MATCH) {
            return;
        }

        Object pointer = createPointer("newFloatArray", data);
        if (pointer != null && tryInvoke("glBufferData", target, sizeBytes, pointer, usage) != NO_MATCH) {
            return;
        }

        throw new OpenGLBridgeException(
            "No compatible iOS OpenGL ES glBufferData signature was found for float data on runtime: " + this.runtimeName
        );
    }

    @Override
    public void bufferData(int target, short[] data, int usage) {
        ShortBuffer buffer = ByteBuffer
            .allocateDirect(data.length * Short.BYTES)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer();
        buffer.put(data);
        buffer.flip();

        int sizeBytes = data.length * Short.BYTES;
        if (tryInvoke("glBufferData", target, sizeBytes, buffer, usage) != NO_MATCH) {
            return;
        }

        Object pointer = createPointer("newShortArray", data);
        if (pointer != null && tryInvoke("glBufferData", target, sizeBytes, pointer, usage) != NO_MATCH) {
            return;
        }

        throw new OpenGLBridgeException(
            "No compatible iOS OpenGL ES glBufferData signature was found for short data on runtime: " + this.runtimeName
        );
    }

    @Override
    public void deleteBuffer(int bufferId) {
        if (tryInvoke("glDeleteBuffers", 1, new int[] {bufferId}, 0) != NO_MATCH) {
            return;
        }

        Object pointer = createIntReference(bufferId);
        if (pointer != null && tryInvoke("glDeleteBuffers", 1, pointer) != NO_MATCH) {
            return;
        }

        invokeRequired("glDeleteBuffers", bufferId);
    }

    private int resolveUniformLocation(String name) {
        if (this.currentProgramId < 0) {
            throw new OpenGLBridgeException("No OpenGL ES program is currently bound. Call useProgram() before setting uniforms.");
        }

        Map<String, Integer> locationsForProgram = this.uniformLocationCache.computeIfAbsent(
            this.currentProgramId,
            ignored -> new HashMap<>()
        );

        Integer cached = locationsForProgram.get(name);
        if (cached != null) {
            return cached;
        }

        int resolved = asInt(invokeRequired("glGetUniformLocation", this.currentProgramId, name), "glGetUniformLocation");
        locationsForProgram.put(name, resolved);
        return resolved;
    }

    private void indexMethods(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            method.setAccessible(true);
            this.methodsByName.computeIfAbsent(method.getName(), ignored -> new ArrayList<>()).add(method);
        }
    }

    private Object invokeRequired(String name, Object... args) {
        Object result = tryInvoke(name, args);
        if (result == NO_MATCH) {
            throw new OpenGLBridgeException(
                "No compatible iOS OpenGL ES binding was found for " + name + " on runtime: " + this.runtimeName
            );
        }
        return result;
    }

    private Object tryInvoke(String name, Object... args) {
        List<Method> methods = this.methodsByName.get(name);
        if (methods == null || methods.isEmpty()) {
            return NO_MATCH;
        }

        for (Method method : methods) {
            Object[] convertedArgs = convertArguments(method.getParameterTypes(), args);
            if (convertedArgs == null) {
                continue;
            }

            try {
                return method.invoke(null, convertedArgs);
            } catch (IllegalAccessException error) {
                throw new OpenGLBridgeException("Could not access iOS OpenGL ES method: " + name, error);
            } catch (IllegalArgumentException ignored) {
                // Keep trying other overloads.
            } catch (InvocationTargetException error) {
                Throwable cause = error.getCause() == null ? error : error.getCause();
                throw new OpenGLBridgeException("iOS OpenGL ES call failed: " + name, cause);
            }
        }

        return NO_MATCH;
    }

    private static Object[] convertArguments(Class<?>[] parameterTypes, Object[] args) {
        if (parameterTypes.length != args.length) {
            return null;
        }

        Object[] converted = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object value = convertArgument(parameterTypes[i], args[i]);
            if (value == INCOMPATIBLE) {
                return null;
            }
            converted[i] = value;
        }
        return converted;
    }

    private static Object convertArgument(Class<?> parameterType, Object arg) {
        if (arg == null) {
            return parameterType.isPrimitive() ? INCOMPATIBLE : null;
        }

        if (parameterType.isInstance(arg)) {
            return arg;
        }

        if (parameterType == int.class || parameterType == Integer.class) {
            return arg instanceof Number ? Integer.valueOf(((Number) arg).intValue()) : INCOMPATIBLE;
        }

        if (parameterType == long.class || parameterType == Long.class) {
            return arg instanceof Number ? Long.valueOf(((Number) arg).longValue()) : INCOMPATIBLE;
        }

        if (parameterType == float.class || parameterType == Float.class) {
            return arg instanceof Number ? Float.valueOf(((Number) arg).floatValue()) : INCOMPATIBLE;
        }

        if (parameterType == double.class || parameterType == Double.class) {
            return arg instanceof Number ? Double.valueOf(((Number) arg).doubleValue()) : INCOMPATIBLE;
        }

        if (parameterType == short.class || parameterType == Short.class) {
            return arg instanceof Number ? Short.valueOf(((Number) arg).shortValue()) : INCOMPATIBLE;
        }

        if (parameterType == byte.class || parameterType == Byte.class) {
            if (arg instanceof Boolean) {
                return Byte.valueOf((byte) (((Boolean) arg).booleanValue() ? 1 : 0));
            }
            return arg instanceof Number ? Byte.valueOf(((Number) arg).byteValue()) : INCOMPATIBLE;
        }

        if (parameterType == boolean.class || parameterType == Boolean.class) {
            if (arg instanceof Boolean) {
                return arg;
            }
            return arg instanceof Number ? Boolean.valueOf(((Number) arg).intValue() != 0) : INCOMPATIBLE;
        }

        if (parameterType == String.class) {
            return arg instanceof CharSequence ? arg.toString() : INCOMPATIBLE;
        }

        if (parameterType == CharSequence.class) {
            return arg instanceof CharSequence ? arg : INCOMPATIBLE;
        }

        if (parameterType.isAssignableFrom(arg.getClass())) {
            return arg;
        }

        return INCOMPATIBLE;
    }

    private Object createPointer(String methodName, Object array) {
        if (this.ptrFactoryType == null) {
            return null;
        }

        Object directPointer = tryInvokeStatic(this.ptrFactoryType, methodName, array);
        if (directPointer != NO_MATCH) {
            return directPointer;
        }

        return null;
    }

    private Object createIntReference() {
        return createIntReference(0);
    }

    private Object createIntReference(int initialValue) {
        if (this.ptrFactoryType == null) {
            return null;
        }

        Object pointer = tryInvokeStatic(this.ptrFactoryType, "newIntPtr", 1, true, true);
        if (pointer == NO_MATCH) {
            pointer = tryInvokeStatic(this.ptrFactoryType, "newIntReference", initialValue);
        }

        if (pointer != NO_MATCH && initialValue != 0) {
            trySetPointerValue(pointer, initialValue);
        }

        return pointer == NO_MATCH ? null : pointer;
    }

    private int readIntReference(Object pointer) {
        try {
            Method getValue = pointer.getClass().getMethod("getValue");
            Object value = getValue.invoke(pointer);
            return asInt(value, "pointer.getValue");
        } catch (NoSuchMethodException error) {
            throw new OpenGLBridgeException("The selected iOS pointer implementation does not expose getValue().", error);
        } catch (IllegalAccessException error) {
            throw new OpenGLBridgeException("Could not read an iOS pointer value.", error);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new OpenGLBridgeException("Reading an iOS pointer value failed.", cause);
        }
    }

    private void trySetPointerValue(Object pointer, int value) {
        try {
            Method setValue = pointer.getClass().getMethod("setValue", int.class);
            setValue.invoke(pointer, value);
        } catch (NoSuchMethodException ignored) {
            // Some pointer implementations are write-on-create only.
        } catch (IllegalAccessException error) {
            throw new OpenGLBridgeException("Could not write an iOS pointer value.", error);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new OpenGLBridgeException("Writing an iOS pointer value failed.", cause);
        }
    }

    private static Object tryInvokeStatic(Class<?> type, String name, Object... args) {
        for (Method method : type.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals(name)) {
                continue;
            }

            Object[] convertedArgs = convertArguments(method.getParameterTypes(), args);
            if (convertedArgs == null) {
                continue;
            }

            try {
                return method.invoke(null, convertedArgs);
            } catch (IllegalAccessException error) {
                throw new OpenGLBridgeException("Could not access iOS helper method: " + name, error);
            } catch (IllegalArgumentException ignored) {
                // Keep trying overloads.
            } catch (InvocationTargetException error) {
                Throwable cause = error.getCause() == null ? error : error.getCause();
                throw new OpenGLBridgeException("iOS helper call failed: " + name, cause);
            }
        }

        return NO_MATCH;
    }

    private static int asInt(Object value, String name) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        throw new OpenGLBridgeException("Expected a numeric result from " + name + " but received: " + value);
    }

    private static Class<?> loadOptionalClass(String[] candidates) {
        for (String candidate : candidates) {
            try {
                return Class.forName(candidate);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}
