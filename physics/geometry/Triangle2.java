package physics.geometry;

import java.util.List;

public final class Triangle2 {

    private final Vector2 a;
    private final Vector2 b;
    private final Vector2 c;

    public Triangle2(Vector2 a, Vector2 b, Vector2 c) {
        this.a = a;
        this.b = b;
        this.c = c;
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

    public List<Vector2> getVertices() {
        return List.of(this.a, this.b, this.c);
    }

    public double signedArea() {
        return 0.5 * this.b.subtract(this.a).cross(this.c.subtract(this.a));
    }

    public double area() {
        return Math.abs(signedArea());
    }

    public Vector2 getCentroid() {
        return new Vector2(
            (this.a.getX() + this.b.getX() + this.c.getX()) / 3.0,
            (this.a.getY() + this.b.getY() + this.c.getY()) / 3.0
        );
    }

    public Triangle2 transformed(Transform2 transform) {
        return new Triangle2(
            transform.apply(this.a),
            transform.apply(this.b),
            transform.apply(this.c)
        );
    }

    public Triangle2 translated(Vector2 offset) {
        return new Triangle2(this.a.add(offset), this.b.add(offset), this.c.add(offset));
    }
}
