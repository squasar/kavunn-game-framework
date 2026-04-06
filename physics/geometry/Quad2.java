package physics.geometry;

import java.util.List;

public final class Quad2 {

    private final Vector2 a;
    private final Vector2 b;
    private final Vector2 c;
    private final Vector2 d;

    public Quad2(Vector2 a, Vector2 b, Vector2 c, Vector2 d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Vector2 getA() {
        return this.a;
    }

    public Vector2 getB() {
        return this.b;
    }

    public Vector2 getC() {
        return this.c;
    }

    public Vector2 getD() {
        return this.d;
    }

    public List<Vector2> getVertices() {
        return List.of(this.a, this.b, this.c, this.d);
    }

    public List<Triangle2> toTriangles() {
        return List.of(
            new Triangle2(this.a, this.b, this.c),
            new Triangle2(this.a, this.c, this.d)
        );
    }

    public Contour2 toContour() {
        return new Contour2(getVertices());
    }

    public Quad2 transformed(Transform2 transform) {
        return new Quad2(
            transform.apply(this.a),
            transform.apply(this.b),
            transform.apply(this.c),
            transform.apply(this.d)
        );
    }
}
