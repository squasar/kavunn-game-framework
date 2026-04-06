package render;

import java.util.Collections;
import java.util.List;

public abstract class RenderingPipeline {

    public final void render(Universe universe, Planet planet) {
        render(universe, planet, NoOpRenderBackend.INSTANCE);
    }

    public final void render(Universe universe, Planet planet, RenderBackend backend) {
        validate(universe, planet, backend);
        prepareLifecycle(planet);
        bind(universe, planet);
        backend.beginFrame(universe, planet);
        beforeRender(universe, planet, backend);

        for (Matter matter : resolveMatters(universe, planet)) {
            if (matter == null) {
                continue;
            }

            backend.beforeMatter(universe, planet, matter);
            beforeMatter(universe, planet, matter, backend);

            for (RenderTask task : resolveTasks(universe, planet, matter, backend)) {
                if (task != null && task.supports(backend)) {
                    task.execute(backend, universe, planet, matter);
                }
            }

            afterMatter(universe, planet, matter, backend);
            backend.afterMatter(universe, planet, matter);
        }

        afterRender(universe, planet, backend);
        backend.endFrame(universe, planet);
    }

    protected void validate(Universe universe, Planet planet, RenderBackend backend) {
        if (universe == null) {
            throw new IllegalArgumentException("Rendering requires a universe.");
        }
        if (planet == null) {
            throw new IllegalArgumentException("Rendering requires a planet.");
        }
        if (backend == null) {
            throw new IllegalArgumentException("Rendering requires a backend.");
        }
    }

    protected void prepareLifecycle(Planet planet) {
        if (planet.isExited()) {
            throw new IllegalStateException("An exited planet cannot enter the rendering pipeline.");
        }

        if (planet.isCreated() || planet.isClosed()) {
            planet.startPlanet();
        }
    }

    protected void bind(Universe universe, Planet planet) {
        universe.mountPlanet(planet);
    }

    protected List<Matter> resolveMatters(Universe universe, Planet planet) {
        return planet.getMatters();
    }

    protected List<RenderTask> resolveTasks(
        Universe universe,
        Planet planet,
        Matter matter,
        RenderBackend backend
    ) {
        return Collections.emptyList();
    }

    protected void beforeRender(Universe universe, Planet planet, RenderBackend backend) {
    }

    protected void afterRender(Universe universe, Planet planet, RenderBackend backend) {
    }

    protected void beforeMatter(Universe universe, Planet planet, Matter matter, RenderBackend backend) {
    }

    protected void afterMatter(Universe universe, Planet planet, Matter matter, RenderBackend backend) {
    }
}
