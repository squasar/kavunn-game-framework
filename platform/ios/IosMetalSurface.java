package platform.ios;

import java.util.Objects;

import render.MetalRenderBackend;
import render.Planet;
import render.RenderingPipeline;
import render.Universe;

public class IosMetalSurface implements IosSurface {

    private final String surfaceName;
    private final RenderingPipeline pipeline;
    private final Universe universe;
    private final Planet planet;
    private final MetalRenderBackend backend;

    private IosInputAdapter inputAdapter;
    private IosInterfaceOrientation orientation = IosInterfaceOrientation.UNKNOWN;
    private IosSafeAreaInsets safeAreaInsets = IosSafeAreaInsets.ZERO;

    private int logicalWidth;
    private int logicalHeight;
    private float scaleFactor = 1f;
    private int pixelWidth;
    private int pixelHeight;
    private boolean created = false;
    private boolean paused = false;
    private boolean destroyed = false;

    public IosMetalSurface(
        String surfaceName,
        RenderingPipeline pipeline,
        Universe universe,
        Planet planet,
        MetalRenderBackend backend
    ) {
        this.surfaceName = Objects.requireNonNull(surfaceName, "iOS surface name cannot be null.");
        this.pipeline = Objects.requireNonNull(pipeline, "iOS surface requires a rendering pipeline.");
        this.universe = Objects.requireNonNull(universe, "iOS surface requires a universe.");
        this.planet = Objects.requireNonNull(planet, "iOS surface requires a planet.");
        this.backend = Objects.requireNonNull(backend, "iOS surface requires a Metal backend.");
    }

    @Override
    public String getSurfaceName() {
        return this.surfaceName;
    }

    public void setInputAdapter(IosInputAdapter inputAdapter) {
        this.inputAdapter = inputAdapter;
    }

    public IosInputAdapter getInputAdapter() {
        return this.inputAdapter;
    }

    public Universe getUniverse() {
        return this.universe;
    }

    public Planet getPlanet() {
        return this.planet;
    }

    public MetalRenderBackend getBackend() {
        return this.backend;
    }

    public int getLogicalWidth() {
        return this.logicalWidth;
    }

    public int getLogicalHeight() {
        return this.logicalHeight;
    }

    public float getScaleFactor() {
        return this.scaleFactor;
    }

    public int getPixelWidth() {
        return this.pixelWidth;
    }

    public int getPixelHeight() {
        return this.pixelHeight;
    }

    public IosInterfaceOrientation getOrientation() {
        return this.orientation;
    }

    public IosSafeAreaInsets getSafeAreaInsets() {
        return this.safeAreaInsets;
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
        this.paused = false;
        this.destroyed = false;
    }

    @Override
    public void onSurfaceChanged(int widthPoints, int heightPoints, float scaleFactor) {
        this.logicalWidth = widthPoints;
        this.logicalHeight = heightPoints;
        this.scaleFactor = scaleFactor <= 0f ? 1f : scaleFactor;
        this.pixelWidth = Math.max(1, Math.round(widthPoints * this.scaleFactor));
        this.pixelHeight = Math.max(1, Math.round(heightPoints * this.scaleFactor));
        this.backend.getMetalBridge().configureSurface(this.surfaceName, this.pixelWidth, this.pixelHeight, this.scaleFactor);
        this.backend.getMetalBridge().setViewport(0, 0, this.pixelWidth, this.pixelHeight);
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
            throw new IllegalStateException("A destroyed iOS surface cannot resume.");
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
        this.backend.getMetalBridge().onSurfaceDestroyed();
        if (this.planet.isActive()) {
            this.planet.closePlanet();
        }
    }

    public void onApplicationDidEnterBackground() {
        if (this.inputAdapter != null) {
            this.inputAdapter.onEnteredBackground();
        }
        onPause();
    }

    public void onApplicationWillResignActive() {
        if (this.inputAdapter != null) {
            this.inputAdapter.onWillResignActive();
        }
        onPause();
    }

    public void onApplicationWillEnterForeground() {
        if (this.inputAdapter != null) {
            this.inputAdapter.onWillEnterForeground();
        }
    }

    public void onApplicationDidBecomeActive() {
        if (this.inputAdapter != null) {
            this.inputAdapter.onDidBecomeActive();
        }
        onResume();
    }

    public void onOrientationChanged(IosInterfaceOrientation orientation) {
        this.orientation = orientation == null ? IosInterfaceOrientation.UNKNOWN : orientation;
        if (this.inputAdapter != null) {
            this.inputAdapter.onOrientationChanged(this.orientation);
        }
    }

    public void onSafeAreaChanged(IosSafeAreaInsets safeAreaInsets) {
        this.safeAreaInsets = safeAreaInsets == null ? IosSafeAreaInsets.ZERO : safeAreaInsets;
        if (this.inputAdapter != null) {
            this.inputAdapter.onSafeAreaChanged(this.safeAreaInsets);
        }
    }

    public void dispatchTouchEvent(IosTouchEvent event) {
        if (this.inputAdapter != null) {
            this.inputAdapter.onTouchEvent(event);
        }
    }
}
