package platform.mobile;

public final class MobileTouchEvent {

    private final int pointerId;
    private final float x;
    private final float y;
    private final MobileTouchAction action;
    private final long timestampNanos;

    public MobileTouchEvent(int pointerId, float x, float y, MobileTouchAction action, long timestampNanos) {
        this.pointerId = pointerId;
        this.x = x;
        this.y = y;
        this.action = action;
        this.timestampNanos = timestampNanos;
    }

    public int getPointerId() {
        return this.pointerId;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public MobileTouchAction getAction() {
        return this.action;
    }

    public long getTimestampNanos() {
        return this.timestampNanos;
    }
}
