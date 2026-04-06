package render;

public abstract class Java2DRenderTask implements RenderTask {

    @Override
    public boolean supports(RenderBackend backend) {
        return backend instanceof Java2DRenderBackend;
    }

    @Override
    public final void execute(RenderBackend backend, Universe universe, Planet planet, Matter matter) {
        if (!(backend instanceof Java2DRenderBackend java2dBackend)) {
            throw new IllegalArgumentException("Java2DRenderTask requires a Java2DRenderBackend.");
        }
        execute(java2dBackend, universe, planet, matter);
    }

    protected abstract void execute(
        Java2DRenderBackend backend,
        Universe universe,
        Planet planet,
        Matter matter
    );
}
