package render;

import java.util.Objects;

public class OpenGLRenderBackend implements RenderBackend {

    private final OpenGLBridge bridge;

    public OpenGLRenderBackend(OpenGLBridge bridge) {
        this.bridge = Objects.requireNonNull(bridge, "OpenGL rendering requires a bridge implementation.");
    }

    public static OpenGLRenderBackend lwjgl() {
        return new OpenGLRenderBackend(new LwjglOpenGLBridge());
    }

    public static boolean isLwjglAvailable() {
        return LwjglOpenGLBridge.isAvailable();
    }

    @Override
    public String getBackendName() {
        return "opengl";
    }

    public OpenGLBridge getBridge() {
        return this.bridge;
    }

    public boolean supportsOpenGLES() {
        return this.bridge instanceof OpenGLESBridge;
    }

    public OpenGLESBridge getOpenGLESBridge() {
        if (!(this.bridge instanceof OpenGLESBridge openGLESBridge)) {
            throw new IllegalStateException("This OpenGL backend does not expose an OpenGL ES compatible bridge.");
        }
        return openGLESBridge;
    }
}
