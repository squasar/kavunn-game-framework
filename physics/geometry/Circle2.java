package physics.geometry;

import java.util.ArrayList;
import java.util.List;

public final class Circle2 {

    private final Vector2 center;
    private final double radius;

    public Circle2(Vector2 center, double radius) {
        if (radius < 0.0) {
            throw new IllegalArgumentException("Circle radius cannot be negative.");
        }
        this.center = center;
        this.radius = radius;
    }

    public Vector2 getCenter() {
        return this.center;
    }

    public double getRadius() {
        return this.radius;
    }

    public Bounds2 getBounds() {
        return new Bounds2(
            this.center.getX() - this.radius,
            this.center.getY() - this.radius,
            this.center.getX() + this.radius,
            this.center.getY() + this.radius
        );
    }

    public Contour2 sampleContour(int pointCount) {
        int safeCount = Math.max(8, pointCount);
        List<Vector2> points = new ArrayList<>(safeCount);
        for (int index = 0; index < safeCount; index++) {
            double angle = (Math.PI * 2.0 * index) / safeCount;
            points.add(new Vector2(
                this.center.getX() + Math.cos(angle) * this.radius,
                this.center.getY() + Math.sin(angle) * this.radius
            ));
        }
        return new Contour2(points);
    }
}
