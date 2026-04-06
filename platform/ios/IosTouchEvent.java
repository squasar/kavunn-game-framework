package platform.ios;

public final class IosTouchEvent {

    private final int pointerId;
    private final float x;
    private final float y;
    private final float pressure;
    private final IosTouchPhase phase;
    private final long timestampNanos;

    public IosTouchEvent(
        int pointerId,
        float x,
        float y,
        float pressure,
        IosTouchPhase phase,
        long timestampNanos
    ) {
        this.pointerId = pointerId;
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.phase = phase;
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

    public float getPressure() {
        return this.pressure;
    }

    public IosTouchPhase getPhase() {
        return this.phase;
    }

    public long getTimestampNanos() {
        return this.timestampNanos;
    }
}
