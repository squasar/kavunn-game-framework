package render;

import java.util.Objects;

import platform.ios.nativebridge.IosMetalNativeBridge;

public final class IosMetalBridge implements MetalBridge {

    private final IosMetalNativeBridge nativeBridge;

    public IosMetalBridge(IosMetalNativeBridge nativeBridge) {
        this.nativeBridge = Objects.requireNonNull(nativeBridge, "iOS Metal bridge requires a native bridge implementation.");
    }

    public IosMetalNativeBridge getNativeBridge() {
        return this.nativeBridge;
    }

    @Override
    public void configureSurface(String surfaceName, int pixelWidth, int pixelHeight, float scaleFactor) {
        this.nativeBridge.configureSurface(surfaceName, pixelWidth, pixelHeight, scaleFactor);
    }

    @Override
    public void beginFrame() {
        if (!this.nativeBridge.isReady()) {
            throw new MetalBridgeException("The iOS Metal bridge is not ready to render a frame yet.");
        }
        this.nativeBridge.beginFrame();
    }

    @Override
    public void clearColor(float red, float green, float blue, float alpha) {
        this.nativeBridge.clearColor(red, green, blue, alpha);
    }

    @Override
    public void setViewport(int x, int y, int width, int height) {
        this.nativeBridge.setViewport(x, y, width, height);
    }

    @Override
    public void usePipeline(String pipelineKey) {
        this.nativeBridge.usePipeline(pipelineKey);
    }

    @Override
    public void useTexture(String textureKey, int textureIndex) {
        this.nativeBridge.useTexture(textureKey, textureIndex);
    }

    @Override
    public void setVertexFloatData(int bufferIndex, float[] data) {
        this.nativeBridge.setVertexFloatData(bufferIndex, data);
    }

    @Override
    public void setFragmentFloatData(int bufferIndex, float[] data) {
        this.nativeBridge.setFragmentFloatData(bufferIndex, data);
    }

    @Override
    public void drawPrimitives(int primitiveType, int vertexStart, int vertexCount) {
        this.nativeBridge.drawPrimitives(primitiveType, vertexStart, vertexCount);
    }

    @Override
    public void drawIndexedPrimitives(int primitiveType, int indexCount, int indexType, short[] indices, int indexOffset) {
        this.nativeBridge.drawIndexedPrimitives(primitiveType, indexCount, indexType, indices, indexOffset);
    }

    @Override
    public void endFrame() {
        this.nativeBridge.endFrame();
    }

    @Override
    public void presentFrame() {
        this.nativeBridge.presentFrame();
    }

    @Override
    public void onSurfaceDestroyed() {
        this.nativeBridge.onSurfaceDestroyed();
    }
}
