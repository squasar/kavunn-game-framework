package physics.collision;

import physics.body.PhysicsBody;
import physics.geometry.Vector2;

public final class RaycastHit {

    private final boolean hit;
    private final PhysicsBody body;
    private final Vector2 point;
    private final Vector2 normal;
    private final double distance;

    public RaycastHit(boolean hit, PhysicsBody body, Vector2 point, Vector2 normal, double distance) {
        this.hit = hit;
        this.body = body;
        this.point = point;
        this.normal = normal;
        this.distance = distance;
    }

    public static RaycastHit noHit() {
        return new RaycastHit(false, null, Vector2.ZERO, Vector2.ZERO, Double.POSITIVE_INFINITY);
    }

    public boolean isHit() {
        return this.hit;
    }

    public PhysicsBody getBody() {
        return this.body;
    }

    public Vector2 getPoint() {
        return this.point;
    }

    public Vector2 getNormal() {
        return this.normal;
    }

    public double getDistance() {
        return this.distance;
    }
}
