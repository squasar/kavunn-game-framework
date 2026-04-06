package platform.mobile;

public interface MobileInputAdapter {

    default void onTouchEvent(MobileTouchEvent event) {
    }

    default void onPauseRequested() {
    }

    default void onResumeRequested() {
    }

    default void onBackRequested() {
    }
}
