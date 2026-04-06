package render;

public interface MetalBridge {

    void configureSurface(String surfaceName, int pixelWidth, int pixelHeight, float scaleFactor);

    void beginFrame();

    void clearColor(float red, float green, float blue, float alpha);

    void setViewport(int x, int y, int width, int height);

    void usePipeline(String pipelineKey);

    void useTexture(String textureKey, int textureIndex);

    void setVertexFloatData(int bufferIndex, float[] data);

    void setFragmentFloatData(int bufferIndex, float[] data);

    void drawPrimitives(int primitiveType, int vertexStart, int vertexCount);

    void drawIndexedPrimitives(int primitiveType, int indexCount, int indexType, short[] indices, int indexOffset);

    void endFrame();

    void presentFrame();

    default void onSurfaceDestroyed() {
    }
}
