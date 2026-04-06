package platform.mobile;

import java.util.Objects;

import render.OpenGLESRenderBackend;
import render.RenderingPipeline;
import render.Planet;
import render.Universe;

public class MobileOpenGLESSurface implements MobileSurface {

    private final String surfaceName;
    private final RenderingPipeline pipeline;
    private final Universe universe;
    private final Planet planet;
    private final OpenGLESRenderBackend backend;

    private MobileInputAdapter inputAdapter;
    private int width;
    private int height;
    private boolean created = false;
    private boolean paused = false;
    private boolean destroyed = false;

    public MobileOpenGLESSurface(
        String surfaceName,
        RenderingPipeline pipeline,
        Universe universe,
        Planet planet,
        OpenGLESRenderBackend backend
    ) {
        this.surfaceName = Objects.requireNonNull(surfaceName, "Mobile surface name cannot be null.");
        this.pipeline = Objects.requireNonNull(pipeline, "Mobile surface requires a rendering pipeline.");
        this.universe = Objects.requireNonNull(universe, "Mobile surface requires a universe.");
        this.planet = Objects.requireNonNull(planet, "Mobile surface requires a planet.");
        this.backend = Objects.requireNonNull(backend, "Mobile surface requires an OpenGL ES backend.");
    }

    @Override
    public String getSurfaceName() {
        return this.surfaceName;
    }

    public void setInputAdapter(MobileInputAdapter inputAdapter) {
        this.inputAdapter = inputAdapter;
    }

    public MobileInputAdapter getInputAdapter() {
        return this.inputAdapter;
    }

    public Universe getUniverse() {
        return this.universe;
    }

    public Planet getPlanet() {
        return this.planet;
    }

    public OpenGLESRenderBackend getBackend() {
        return this.backend;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isCreated() {
        return this.created;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public boolean isDestroyed() {
        return this.destroyed;
    }

    @Override
    public void onSurfaceCreated() {
        this.created = true;
        this.destroyed = false;
        this.paused = false;
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        this.width = width;
        this.height = height;
        this.backend.getOpenGLESBridge().viewport(0, 0, width, height);
    }

    @Override
    public void renderFrame() {
        if (!this.created || this.destroyed || this.paused) {
            return;
        }

        this.pipeline.render(this.universe, this.planet, this.backend);
    }

    @Override
    public void onPause() {
        this.paused = true;
        if (this.inputAdapter != null) {
            this.inputAdapter.onPauseRequested();
        }
        if (this.planet.isRunning()) {
            this.planet.pausePlanet();
        }
    }

    @Override
    public void onResume() {
        if (this.destroyed) {
            throw new IllegalStateException("A destroyed mobile surface cannot resume.");
        }

        this.paused = false;
        if (this.inputAdapter != null) {
            this.inputAdapter.onResumeRequested();
        }
        if (this.planet.isPaused()) {
            this.planet.resumePlanet();
        }
    }

    @Override
    public void onSurfaceDestroyed() {
        this.created = false;
        this.paused = false;
        this.destroyed = true;
        if (this.planet.isActive()) {
            this.planet.closePlanet();
        }
    }

    public void dispatchTouchEvent(MobileTouchEvent event) {
        if (this.inputAdapter != null) {
            this.inputAdapter.onTouchEvent(event);
        }
    }

    public void dispatchBackRequested() {
        if (this.inputAdapter != null) {
            this.inputAdapter.onBackRequested();
        }
    }
}
