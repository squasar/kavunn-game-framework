package render;

public class OpenGLESRenderBackend extends OpenGLRenderBackend {

    public OpenGLESRenderBackend(OpenGLESBridge bridge) {
        super(bridge);
    }

    public static OpenGLESRenderBackend android() {
        return new OpenGLESRenderBackend(new AndroidOpenGLESBridge());
    }

    public static OpenGLESRenderBackend ios() {
        return new OpenGLESRenderBackend(new IosOpenGLESBridge());
    }

    public static boolean isAndroidAvailable() {
        return AndroidOpenGLESBridge.isAvailable();
    }

    public static boolean isIosAvailable() {
        return IosOpenGLESBridge.isAvailable();
    }

    @Override
    public OpenGLESBridge getOpenGLESBridge() {
        return (OpenGLESBridge) super.getOpenGLESBridge();
    }
}
