package render;

public final class NoOpRenderBackend implements RenderBackend {

    public static final NoOpRenderBackend INSTANCE = new NoOpRenderBackend();

    private NoOpRenderBackend() {
    }

    @Override
    public String getBackendName() {
        return "noop";
    }
}
