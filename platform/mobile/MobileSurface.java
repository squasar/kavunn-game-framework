package platform.mobile;

public interface MobileSurface {

    String getSurfaceName();

    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void renderFrame();

    void onPause();

    void onResume();

    void onSurfaceDestroyed();
}
