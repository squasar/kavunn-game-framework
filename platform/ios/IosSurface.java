package platform.ios;

public interface IosSurface {

    String getSurfaceName();

    void onSurfaceCreated();

    void onSurfaceChanged(int widthPoints, int heightPoints, float scaleFactor);

    void renderFrame();

    void onPause();

    void onResume();

    void onSurfaceDestroyed();
}
