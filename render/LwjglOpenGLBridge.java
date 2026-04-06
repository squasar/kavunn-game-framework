package render;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

public final class LwjglOpenGLBridge implements OpenGLESBridge {

    private static final String GL11_CLASS = "org.lwjgl.opengl.GL11";
    private static final String GL13_CLASS = "org.lwjgl.opengl.GL13";
    private static final String GL15_CLASS = "org.lwjgl.opengl.GL15";
    private static final String GL20_CLASS = "org.lwjgl.opengl.GL20";

    private final Method glViewport;
    private final Method glClearColor;
    private final Method glClear;
    private final Method glEnable;
    private final Method glDisable;
    private final Method glBlendFunc;
    private final Method glActiveTexture;
    private final Method glBindTexture;
    private final Method glGenTextures;
    private final Method glDeleteTextures;
    private final Method glTexParameteri;
    private final Method glTexImage2D;
    private final Method glDrawArrays;
    private final Method glDrawElements;
    private final Method glUseProgram;
    private final Method glGetUniformLocation;
    private final Method glUniform1f;
    private final Method glUniform1i;
    private final Method glUniformMatrix4fv;
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
    private final Method glBufferDataFloat;
    private final Method glBufferDataShort;
    private final Method glDeleteBuffers;

    private final Map<Integer, Map<String, Integer>> uniformLocationCache = new HashMap<>();
    private int currentProgramId = -1;

    public LwjglOpenGLBridge() {
        try {
            Class<?> gl11 = Class.forName(GL11_CLASS);
            Class<?> gl13 = Class.forName(GL13_CLASS);
            Class<?> gl15 = Class.forName(GL15_CLASS);
            Class<?> gl20 = Class.forName(GL20_CLASS);

            this.glViewport = findMethod(gl11, "glViewport", int.class, int.class, int.class, int.class);
            this.glClearColor = findMethod(gl11, "glClearColor", float.class, float.class, float.class, float.class);
            this.glClear = findMethod(gl11, "glClear", int.class);
            this.glEnable = findMethod(gl11, "glEnable", int.class);
            this.glDisable = findMethod(gl11, "glDisable", int.class);
            this.glBlendFunc = findMethod(gl11, "glBlendFunc", int.class, int.class);
            this.glActiveTexture = findMethod(gl13, "glActiveTexture", int.class);
            this.glBindTexture = findMethod(gl11, "glBindTexture", int.class, int.class);
            this.glGenTextures = findMethod(gl11, "glGenTextures");
            this.glDeleteTextures = findMethod(gl11, "glDeleteTextures", int.class);
            this.glTexParameteri = findMethod(gl11, "glTexParameteri", int.class, int.class, int.class);
            this.glTexImage2D = findMethod(
                gl11,
                "glTexImage2D",
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                int.class,
                ByteBuffer.class
            );
            this.glDrawArrays = findMethod(gl11, "glDrawArrays", int.class, int.class, int.class);
            this.glDrawElements = findMethod(gl11, "glDrawElements", int.class, int.class, int.class, long.class);

            this.glUseProgram = findMethod(gl20, "glUseProgram", int.class);
            this.glGetUniformLocation = findMethod(gl20, "glGetUniformLocation", int.class, CharSequence.class);
            this.glUniform1f = findMethod(gl20, "glUniform1f", int.class, float.class);
            this.glUniform1i = findMethod(gl20, "glUniform1i", int.class, int.class);
            this.glUniformMatrix4fv = findMethod(gl20, "glUniformMatrix4fv", int.class, boolean.class, FloatBuffer.class);
            this.glCreateShader = findMethod(gl20, "glCreateShader", int.class);
            this.glShaderSource = findMethod(gl20, "glShaderSource", int.class, CharSequence.class);
            this.glCompileShader = findMethod(gl20, "glCompileShader", int.class);
            this.glDeleteShader = findMethod(gl20, "glDeleteShader", int.class);
            this.glCreateProgram = findMethod(gl20, "glCreateProgram");
            this.glAttachShader = findMethod(gl20, "glAttachShader", int.class, int.class);
            this.glLinkProgram = findMethod(gl20, "glLinkProgram", int.class);
            this.glDeleteProgram = findMethod(gl20, "glDeleteProgram", int.class);
            this.glGetAttribLocation = findMethod(gl20, "glGetAttribLocation", int.class, CharSequence.class);
            this.glEnableVertexAttribArray = findMethod(gl20, "glEnableVertexAttribArray", int.class);
            this.glDisableVertexAttribArray = findMethod(gl20, "glDisableVertexAttribArray", int.class);
            this.glVertexAttribPointer = findMethod(
                gl20,
                "glVertexAttribPointer",
                int.class,
                int.class,
                int.class,
                boolean.class,
                int.class,
                long.class
            );

            this.glGenBuffers = findMethod(gl15, "glGenBuffers");
            this.glBindBuffer = findMethod(gl15, "glBindBuffer", int.class, int.class);
            this.glBufferDataFloat = findMethod(gl15, "glBufferData", int.class, FloatBuffer.class, int.class);
            this.glBufferDataShort = findMethod(gl15, "glBufferData", int.class, ShortBuffer.class, int.class);
            this.glDeleteBuffers = findMethod(gl15, "glDeleteBuffers", int.class);
        } catch (ClassNotFoundException error) {
            throw new OpenGLBridgeException(
                "LWJGL OpenGL classes were not found. Add LWJGL to the classpath before creating LwjglOpenGLBridge.",
                error
            );
        }
    }

    public static boolean isAvailable() {
        try {
            Class.forName(GL11_CLASS);
            Class.forName(GL13_CLASS);
            Class.forName(GL15_CLASS);
            Class.forName(GL20_CLASS);
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
        return ((Integer) invoke(this.glGenTextures)).intValue();
    }

    @Override
    public void deleteTexture(int textureId) {
        invoke(this.glDeleteTextures, textureId);
    }

    @Override
    public void texParameteri(int target, int parameterName, int value) {
        invoke(this.glTexParameteri, target, parameterName, value);
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type, byte[] data) {
        ByteBuffer buffer = data == null
            ? null
            : ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
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

        FloatBuffer buffer = ByteBuffer
            .allocateDirect(values.length * Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        buffer.put(values);
        buffer.flip();

        invoke(this.glUniformMatrix4fv, location, false, buffer);
    }

    @Override
    public void drawArrays(int mode, int first, int count) {
        invoke(this.glDrawArrays, mode, first, count);
    }

    @Override
    public void drawElements(int mode, int count, int type, int offset) {
        invoke(this.glDrawElements, mode, count, type, (long) offset);
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
        invoke(this.glVertexAttribPointer, index, size, type, normalized, stride, (long) offset);
    }

    @Override
    public int genBuffer() {
        return ((Integer) invoke(this.glGenBuffers)).intValue();
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
        invoke(this.glBufferDataFloat, target, buffer, usage);
    }

    @Override
    public void bufferData(int target, short[] data, int usage) {
        ShortBuffer buffer = ByteBuffer
            .allocateDirect(data.length * Short.BYTES)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer();
        buffer.put(data);
        buffer.flip();
        invoke(this.glBufferDataShort, target, buffer, usage);
    }

    @Override
    public void deleteBuffer(int bufferId) {
        invoke(this.glDeleteBuffers, bufferId);
    }

    private int resolveUniformLocation(String name) {
        if (this.currentProgramId < 0) {
            throw new OpenGLBridgeException("No OpenGL program is currently bound. Call useProgram() before setting uniforms.");
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
                "Required LWJGL method is missing: " + type.getName() + "." + name,
                error
            );
        }
    }

    private static Object invoke(Method method, Object... args) {
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException error) {
            throw new OpenGLBridgeException("Could not access LWJGL OpenGL method: " + method.getName(), error);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            throw new OpenGLBridgeException("LWJGL OpenGL call failed: " + method.getName(), cause);
        }
    }
}
