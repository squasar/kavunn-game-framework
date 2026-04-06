package render;

public interface RenderBackend {

    String getBackendName();

    default void beginFrame(Universe universe, Planet planet) {
    }

    default void beforeMatter(Universe universe, Planet planet, Matter matter) {
    }

    default void afterMatter(Universe universe, Planet planet, Matter matter) {
    }

    default void endFrame(Universe universe, Planet planet) {
    }
}
