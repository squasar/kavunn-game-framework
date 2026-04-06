package platform.ios;

public interface IosInputAdapter {

    default void onTouchEvent(IosTouchEvent event) {
    }

    default void onPauseRequested() {
    }

    default void onResumeRequested() {
    }

    default void onOrientationChanged(IosInterfaceOrientation orientation) {
    }

    default void onSafeAreaChanged(IosSafeAreaInsets safeAreaInsets) {
    }

    default void onWillResignActive() {
    }

    default void onDidBecomeActive() {
    }

    default void onEnteredBackground() {
    }

    default void onWillEnterForeground() {
    }
}
