package physics.collision;

import physics.geometry.Vector2;

public final class ContactPoint {

    private final Vector2 position;
    private final Vector2 normal;
    private final double separation;

    public ContactPoint(Vector2 position, Vector2 normal, double separation) {
        this.position = position;
        this.normal = normal;
        this.separation = separation;
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public Vector2 getNormal() {
        return this.normal;
    }

    public double getSeparation() {
        return this.separation;
    }
}
