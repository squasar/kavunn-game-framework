package render;

@FunctionalInterface
public interface RenderTask {

    void execute(RenderBackend backend, Universe universe, Planet planet, Matter matter);

    default boolean supports(RenderBackend backend) {
        return true;
    }

    default String getTaskName() {
        return this.getClass().getSimpleName();
    }
}
