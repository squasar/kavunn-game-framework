package render;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

public final class AndroidOpenGLESBridge implements OpenGLESBridge {

    private static final String GLES20_CLASS = "android.opengl.GLES20";

    private final Method glViewport;
    private final Method glClearColor;
    private final Method glClear;
    private final Method glEnable;
    private final Method glDisable;
    private final Method glBlendFunc;
    private final Method glActiveTexture;
    private final Method glUseProgram;
    private final Method glBindTexture;
    private final Method glGenTextures;
    private final Method glDeleteTextures;
    private final Method glTexParameteri;
    private final Method glTexImage2D;
    private final Method glGetUniformLocation;
    private final Method glUniform1f;
    private final Method glUniform1i;
    private final Method glUniformMatrix4fv;
    private final Method glDrawArrays;
    private final Method glDrawElements;
    private final Method glCreateShader;
    private final Method glShaderSource;
    private final Method glCompileShader;
    private final Method glDeleteShader;
    private final Method glCreateProgram;
    private final Method glAttachShader;
    private final Method glLinkProgram;
    private final Method glDeleteProgram;
    private final Method glGetAttribLocation;
    private final Method glEnableVertexAttribArray;
    private final Method glDisableVertexAttribArray;
    private final Method glVertexAttribPointer;
    private final Method glGenBuffers;
    private final Method glBindBuffer;
    private final Method glBufferData;
    private final Method glDeleteBuffers;

    private final Map<Integer, Map<String, Integer>> uniformLocationCache = new HashMap<>();
    private int currentProgramId = -1;

    public AndroidOpenGLESBridge() {
        try {
            Class<?> gles20 = Class.forName(GLES20_CLASS);

            this.glViewport = findMethod(gles20, "glViewport", int.class, int.class, int.class, int.class);
            this.glClearColor = findMethod(gles20, "glClearColor", float.class, float.class, float.class, float.class);
            this.glClear = findMethod(gles20, "glClear", int.class);
            this.glEnable = findMethod(gles20, "glEnable", int.class);
            this.glDisable = findMethod(gles20, "glDisable", int.class);
            this.glBlendFunc = findMethod(gles20, "glBlendFunc", int.class, int.class);
            this.glActiveTexture = findMethod(gles20, "glActiveTexture", int.class);
            this.glUseProgram = findMethod(gles20, "glUseProgram", int.class);
            this.glBindTexture = findMethod(gles20, "glBindTexture", int.class, int.class);
            this.glGenTextures = findMethod(gles20, "glGenTextures", int.class, int[].class, int.class);
            this.glDeleteTextures = findMethod(gles20, "glDeleteTextures", int.class, int[].class, int.class);
            this.glTexParameteri = findMethod(gles20, "glTexParameteri", int.class, int.class, int.class);
            this.glTexImage2D = findMethod(
                gles20,
                "glTexImage2D",
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                java.nio.Buffer.class
            );
            this.glGetUniformLocation = findMethod(gles20, "glGetUniformLocation", int.class, String.class);
            this.glUniform1f = findMethod(gles20, "glUniform1f", int.class, float.class);
            this.glUniform1i = findMethod(gles20, "glUniform1i", int.class, int.class);
            this.glUniformMatrix4fv = findMethod(gles20, "glUniformMatrix4fv", int.class, int.class, boolean.class, float[].class, int.class);
            this.glDrawArrays = findMethod(gles20, "glDrawArrays", int.class, int.class, int.class);
            this.glDrawElements = findMethod(gles20, "glDrawElements", int.class, int.class, int.class, int.class);
            this.glCreateShader = findMethod(gles20, "glCreateShader", int.class);
            this.glShaderSource = findMethod(gles20, "glShaderSource", int.class, String.class);
            this.glCompileShader = findMethod(gles20, "glCompileShader", int.class);
            this.glDeleteShader = findMethod(gles20, "glDeleteShader", int.class);
            this.glCreateProgram = findMethod(gles20, "glCreateProgram");
            this.glAttachShader = findMethod(gles20, "glAttachShader", int.class, int.class);
            this.glLinkProgram = findMethod(gles20, "glLinkProgram", int.class);
            this.glDeleteProgram = findMethod(gles20, "glDeleteProgram", int.class);
            this.glGetAttribLocation = findMethod(gles20, "glGetAttribLocation", int.class, String.class);
            this.glEnableVertexAttribArray = findMethod(gles20, "glEnableVertexAttribArray", int.class);
            this.glDisableVertexAttribArray = findMethod(gles20, "glDisableVertexAttribArray", int.class);
            this.glVertexAttribPointer = findMethod(
                gles20,
                "glVertexAttribPointer",
                int.class,
                int.class,
                int.class,
                boolean.class,
                int.class,
                int.class
            );
            this.glGenBuffers = findMethod(gles20, "glGenBuffers", int.class, int[].class, int.class);
            this.glBindBuffer = findMethod(gles20, "glBindBuffer", int.class, int.class);
            this.glBufferData = findMethod(gles20, "glBufferData", int.class, int.class, java.nio.Buffer.class, int.class);
            this.glDeleteBuffers = findMethod(gles20, "glDeleteBuffers", int.class, int[].class, int.class);
        } catch (ClassNotFoundException error) {
            throw new OpenGLBridgeException(
                "Android GLES20 classes were not found. Create AndroidOpenGLESBridge only on Android or with android.jar available.",
                error
            );
        }
    }

    public static boolean isAvailable() {
        try {
            Class.forName(GLES20_CLASS);
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        invoke(this.glViewport, x, y, width, height);
    }

    @Override
    public void clearColor(float red, float green, float blue, float alpha) {
        invoke(this.glClearColor, red, green, blue, alpha);
    }

    @Override
    public void clear(int mask) {
        invoke(this.glClear, mask);
    }

    @Override
    public void enable(int capability) {
        invoke(this.glEnable, capability);
    }

    @Override
    public void disable(int capability) {
        invoke(this.glDisable, capability);
    }

    @Override
    public void blendFunc(int sourceFactor, int destinationFactor) {
        invoke(this.glBlendFunc, sourceFactor, destinationFactor);
    }

    @Override
    public void activeTexture(int textureUnit) {
        invoke(this.glActiveTexture, textureUnit);
    }

    @Override
    public void useProgram(int programId) {
        invoke(this.glUseProgram, programId);
        this.currentProgramId = programId;
    }

    @Override
    public void bindTexture(int target, int textureId) {
        invoke(this.glBindTexture, target, textureId);
    }

    @Override
    public int genTexture() {
        int[] ids = new int[1];
        invoke(this.glGenTextures, 1, ids, 0);
        return ids[0];
    }

    @Override
    public void deleteTexture(int textureId) {
        int[] ids = new int[] {textureId};
        invoke(this.glDeleteTextures, 1, ids, 0);
    }

    @Override
    public void texParameteri(int target, int parameterName, int value) {
        invoke(this.glTexParameteri, target, parameterName, value);
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type, byte[] data) {
        ByteBuffer buffer = data == null ? null : ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
        if (buffer != null) {
            buffer.put(data);
            buffer.flip();
        }
        invoke(this.glTexImage2D, target, level, internalFormat, width, height, 0, format, type, buffer);
    }

    @Override
    public void setUniform1f(String name, float value) {
        int location = resolveUniformLocation(name);
        if (location < 0) {
            return;
        }
        invoke(this.glUniform1f, location, value);
    }

    @Override
    public void setUniform1i(String name, int value) {
        int location = resolveUniformLocation(name);
        if (location < 0) {
            return;
        }
        invoke(this.glUniform1i, location, value);
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

        invoke(this.glUniformMatrix4fv, location, values.length / 16, false, values, 0);
    }

    @Override
    public void drawArrays(int mode, int first, int count) {
        invoke(this.glDrawArrays, mode, first, count);
    }

    @Override
    public void drawElements(int mode, int count, int type, int offset) {
        invoke(this.glDrawElements, mode, count, type, offset);
    }

    @Override
    public int createShader(int shaderType) {
        return ((Integer) invoke(this.glCreateShader, shaderType)).intValue();
    }

    @Override
    public void shaderSource(int shaderId, String source) {
        invoke(this.glShaderSource, shaderId, source);
    }

    @Override
    public void compileShader(int shaderId) {
        invoke(this.glCompileShader, shaderId);
    }

    @Override
    public void deleteShader(int shaderId) {
        invoke(this.glDeleteShader, shaderId);
    }

    @Override
    public int createProgram() {
        return ((Integer) invoke(this.glCreateProgram)).intValue();
    }

    @Override
    public void attachShader(int programId, int shaderId) {
        invoke(this.glAttachShader, programId, shaderId);
    }

    @Override
    public void linkProgram(int programId) {
        invoke(this.glLinkProgram, programId);
    }

    @Override
    public void deleteProgram(int programId) {
        invoke(this.glDeleteProgram, programId);
        this.uniformLocationCache.remove(programId);
        if (this.currentProgramId == programId) {
            this.currentProgramId = -1;
        }
    }

    @Override
    public int getAttribLocation(int programId, String name) {
        return ((Integer) invoke(this.glGetAttribLocation, programId, name)).intValue();
    }

    @Override
    public void enableVertexAttribArray(int index) {
        invoke(this.glEnableVertexAttribArray, index);
    }

    @Override
    public void disableVertexAttribArray(int index) {
        invoke(this.glDisableVertexAttribArray, index);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset) {
        invoke(this.glVertexAttribPointer, index, size, type, normalized, stride, offset);
    }

    @Override
    public int genBuffer() {
        int[] ids = new int[1];
        invoke(this.glGenBuffers, 1, ids, 0);
        return ids[0];
    }

    @Override
    public void bindBuffer(int target, int bufferId) {
        invoke(this.glBindBuffer, target, bufferId);
    }

    @Override
    public void bufferData(int target, float[] data, int usage) {
        FloatBuffer buffer = ByteBuffer
            .allocateDirect(data.length * Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        buffer.put(data);
        buffer.flip();
        invoke(this.glBufferData, target, data.length * Float.BYTES, buffer, usage);
    }

    @Override
    public void bufferData(int target, short[] data, int usage) {
        ShortBuffer buffer = ByteBuffer
            .allocateDirect(data.length * Short.BYTES)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer();
        buffer.put(data);
        buffer.flip();
        invoke(this.glBufferData, target, data.length * Short.BYTES, buffer, usage);
    }

    @Override
    public void deleteBuffer(int bufferId) {
        int[] ids = new int[] {bufferId};
        invoke(this.glDeleteBuffers, 1, ids, 0);
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

        Object location = invoke(this.glGetUniformLocation, this.currentProgramId, name);
        int resolved = ((Integer) location).intValue();
        locationsForProgram.put(name, resolved);
        return resolved;
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            Method method = type.getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException error) {
            throw new OpenGLBridgeException(
                "Required Android GLES method is missing: " + type.getName() + "." + name,
                error
            );
        }
    }

    private static Object invoke(Method method, Object... args) {
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException error) {
            throw new OpenGLBridgeException("Could not access Android GLES method: " + method.getName(), error);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new OpenGLBridgeException("Android GLES call failed: " + method.getName(), cause);
        }
    }
}
