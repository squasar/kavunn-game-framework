package render;

public interface OpenGLBridge {

    void viewport(int x, int y, int width, int height);

    void clearColor(float red, float green, float blue, float alpha);

    void clear(int mask);

    void useProgram(int programId);

    void bindTexture(int target, int textureId);

    int genTexture();

    void deleteTexture(int textureId);

    void texParameteri(int target, int parameterName, int value);

    void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type, byte[] data);

    void setUniform1f(String name, float value);

    void setUniform1i(String name, int value);

    void setUniformMatrix4(String name, float[] values);

    void drawArrays(int mode, int first, int count);

    void drawElements(int mode, int count, int type, int offset);
}
