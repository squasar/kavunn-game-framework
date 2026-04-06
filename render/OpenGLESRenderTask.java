package render;

public abstract class OpenGLESRenderTask implements RenderTask {

    @Override
    public boolean supports(RenderBackend backend) {
        return backend instanceof OpenGLRenderBackend openGLBackend
            && openGLBackend.supportsOpenGLES();
    }

    @Override
    public final void execute(RenderBackend backend, Universe universe, Planet planet, Matter matter) {
        if (!(backend instanceof OpenGLRenderBackend openGLBackend)) {
            throw new IllegalArgumentException("OpenGLESRenderTask requires an OpenGLRenderBackend.");
        }

        OpenGLESBridge bridge = openGLBackend.getOpenGLESBridge();
        execute(openGLBackend, bridge, universe, planet, matter);
    }

    protected abstract void execute(
        OpenGLRenderBackend backend,
        OpenGLESBridge bridge,
        Universe universe,
        Planet planet,
        Matter matter
    );
}
