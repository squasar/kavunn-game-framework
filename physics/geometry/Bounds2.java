package physics.geometry;

import java.util.Collection;
import java.util.Objects;

public final class Bounds2 {

    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    public Bounds2(double minX, double minY, double maxX, double maxY) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
    }

    public static Bounds2 fromPoints(Collection<Vector2> points) {
        Objects.requireNonNull(points, "Bounds creation requires points.");
        if (points.isEmpty()) {
            return new Bounds2(0.0, 0.0, 0.0, 0.0);
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Vector2 point : points) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }
        return new Bounds2(minX, minY, maxX, maxY);
    }

    public double getMinX() {
        return this.minX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMaxY() {
        return this.maxY;
    }

    public double getWidth() {
        return this.maxX - this.minX;
    }

    public double getHeight() {
        return this.maxY - this.minY;
    }

    public Vector2 getCenter() {
        return new Vector2((this.minX + this.maxX) * 0.5, (this.minY + this.maxY) * 0.5);
    }

    public boolean contains(Vector2 point) {
        return point.getX() >= this.minX
            && point.getX() <= this.maxX
            && point.getY() >= this.minY
            && point.getY() <= this.maxY;
    }

    public boolean intersects(Bounds2 other) {
        return !(other.maxX < this.minX
            || other.minX > this.maxX
            || other.maxY < this.minY
            || other.minY > this.maxY);
    }

    public Bounds2 union(Bounds2 other) {
        return new Bounds2(
            Math.min(this.minX, other.minX),
            Math.min(this.minY, other.minY),
            Math.max(this.maxX, other.maxX),
            Math.max(this.maxY, other.maxY)
        );
    }
}
