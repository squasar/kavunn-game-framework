package render;

public abstract class MetalRenderTask implements RenderTask {

    @Override
    public boolean supports(RenderBackend backend) {
        return backend instanceof MetalRenderBackend;
    }

    @Override
    public final void execute(RenderBackend backend, Universe universe, Planet planet, Matter matter) {
        if (!(backend instanceof MetalRenderBackend metalBackend)) {
            throw new IllegalArgumentException("MetalRenderTask requires a MetalRenderBackend.");
        }
        execute(metalBackend, metalBackend.getMetalBridge(), universe, planet, matter);
    }

    protected abstract void execute(
        MetalRenderBackend backend,
        MetalBridge bridge,
        Universe universe,
        Planet planet,
        Matter matter
    );
}
