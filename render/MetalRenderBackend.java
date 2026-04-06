package render;

import java.util.Objects;

import platform.ios.nativebridge.IosMetalNativeBridge;

public class MetalRenderBackend implements RenderBackend {

    private final MetalBridge bridge;

    public MetalRenderBackend(MetalBridge bridge) {
        this.bridge = Objects.requireNonNull(bridge, "Metal rendering requires a bridge implementation.");
    }

    public static MetalRenderBackend ios(IosMetalNativeBridge nativeBridge) {
        return new MetalRenderBackend(new IosMetalBridge(nativeBridge));
    }

    @Override
    public String getBackendName() {
        return "metal";
    }

    public MetalBridge getMetalBridge() {
        return this.bridge;
    }

    @Override
    public void beginFrame(Universe universe, Planet planet) {
        this.bridge.beginFrame();
    }

    @Override
    public void endFrame(Universe universe, Planet planet) {
        this.bridge.endFrame();
        this.bridge.presentFrame();
    }
}
