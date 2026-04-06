package platform.ios;

public final class IosSafeAreaInsets {

    public static final IosSafeAreaInsets ZERO = new IosSafeAreaInsets(0f, 0f, 0f, 0f);

    private final float top;
    private final float left;
    private final float bottom;
    private final float right;

    public IosSafeAreaInsets(float top, float left, float bottom, float right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public float getTop() {
        return this.top;
    }

    public float getLeft() {
        return this.left;
    }

    public float getBottom() {
        return this.bottom;
    }

    public float getRight() {
        return this.right;
    }
}
