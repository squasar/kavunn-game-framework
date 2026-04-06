package render;

public abstract class OpenGLRenderTask implements RenderTask {

    @Override
    public boolean supports(RenderBackend backend) {
        return backend instanceof OpenGLRenderBackend;
    }

    @Override
    public final void execute(RenderBackend backend, Universe universe, Planet planet, Matter matter) {
        if (!(backend instanceof OpenGLRenderBackend openGLBackend)) {
            throw new IllegalArgumentException("OpenGLRenderTask requires an OpenGLRenderBackend.");
        }
        execute(openGLBackend, universe, planet, matter);
    }

    protected abstract void execute(
        OpenGLRenderBackend backend,
        Universe universe,
        Planet planet,
        Matter matter
    );
}
