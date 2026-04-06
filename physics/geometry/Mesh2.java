package physics.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Mesh2 {

    private final List<Triangle2> triangles;

    public Mesh2(List<Triangle2> triangles) {
        this.triangles = Collections.unmodifiableList(new ArrayList<>(triangles));
    }

    public static Mesh2 empty() {
        return new Mesh2(List.of());
    }

    public List<Triangle2> getTriangles() {
        return this.triangles;
    }

    public boolean isEmpty() {
        return this.triangles.isEmpty();
    }

    public Bounds2 getBounds() {
        List<Vector2> points = new ArrayList<>();
        for (Triangle2 triangle : this.triangles) {
            points.addAll(triangle.getVertices());
        }
        return points.isEmpty() ? new Bounds2(0.0, 0.0, 0.0, 0.0) : Bounds2.fromPoints(points);
    }

    public Vector2 getCentroid() {
        if (this.triangles.isEmpty()) {
            return Vector2.ZERO;
        }

        double totalArea = 0.0;
        Vector2 accumulator = Vector2.ZERO;
        for (Triangle2 triangle : this.triangles) {
            double area = triangle.area();
            totalArea += area;
            accumulator = accumulator.add(triangle.getCentroid().multiply(area));
        }
        return totalArea == 0.0 ? getBounds().getCenter() : accumulator.divide(totalArea);
    }

    public Mesh2 transformed(Transform2 transform) {
        List<Triangle2> transformed = new ArrayList<>(this.triangles.size());
        for (Triangle2 triangle : this.triangles) {
            transformed.add(triangle.transformed(transform));
        }
        return new Mesh2(transformed);
    }

    public Mesh2 append(Mesh2 other) {
        List<Triangle2> merged = new ArrayList<>(this.triangles.size() + other.triangles.size());
        merged.addAll(this.triangles);
        merged.addAll(other.triangles);
        return new Mesh2(merged);
    }
}
