package physics.form;

import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Triangle2;

public final class TriangleForm implements Form2 {

    private final Triangle2 triangle;

    public TriangleForm(Triangle2 triangle) {
        this.triangle = triangle;
    }

    public Triangle2 getTriangle() {
        return this.triangle;
    }

    @Override
    public String getFormType() {
        return "triangle";
    }

    @Override
    public Bounds2 getLocalBounds() {
        return Bounds2.fromPoints(this.triangle.getVertices());
    }

    @Override
    public Mesh2 toMesh(int subdivisionHint) {
        return new Mesh2(java.util.List.of(this.triangle));
    }

    @Override
    public Contour2 sampleContour(int sampleCount) {
        return new Contour2(this.triangle.getVertices());
    }
}
