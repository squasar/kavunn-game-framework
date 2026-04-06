package render;

public interface OpenGLESBridge extends OpenGLBridge {

    void enable(int capability);

    void disable(int capability);

    void blendFunc(int sourceFactor, int destinationFactor);

    void activeTexture(int textureUnit);

    int createShader(int shaderType);

    void shaderSource(int shaderId, String source);

    void compileShader(int shaderId);

    void deleteShader(int shaderId);

    int createProgram();

    void attachShader(int programId, int shaderId);

    void linkProgram(int programId);

    void deleteProgram(int programId);

    int getAttribLocation(int programId, String name);

    void enableVertexAttribArray(int index);

    void disableVertexAttribArray(int index);

    void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, int offset);

    int genBuffer();

    void bindBuffer(int target, int bufferId);

    void bufferData(int target, float[] data, int usage);

    void bufferData(int target, short[] data, int usage);

    void deleteBuffer(int bufferId);
}
