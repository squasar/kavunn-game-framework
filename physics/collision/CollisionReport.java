package physics.collision;

import java.util.List;

import physics.body.PhysicsBody;
import physics.geometry.Vector2;

public final class CollisionReport {

    private static final ContactManifold NO_HIT_MANIFOLD = new ContactManifold(List.of(), Vector2.ZERO, 0.0);
    private static final CollisionReport NO_HIT = new CollisionReport(
        false,
        null,
        null,
        NO_HIT_MANIFOLD,
        false,
        Double.POSITIVE_INFINITY
    );

    private final boolean hit;
    private final PhysicsBody bodyA;
    private final PhysicsBody bodyB;
    private final ContactManifold manifold;
    private final boolean sensorOnly;
    private final double timeOfImpact;

    public CollisionReport(
        boolean hit,
        PhysicsBody bodyA,
        PhysicsBody bodyB,
        ContactManifold manifold,
        boolean sensorOnly,
        double timeOfImpact
    ) {
        this.hit = hit;
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.manifold = manifold;
        this.sensorOnly = sensorOnly;
        this.timeOfImpact = timeOfImpact;
    }

    public static CollisionReport noHit() {
        return NO_HIT;
    }

    public boolean isHit() {
        return this.hit;
    }

    public PhysicsBody getBodyA() {
        return this.bodyA;
    }

    public PhysicsBody getBodyB() {
        return this.bodyB;
    }

    public ContactManifold getManifold() {
        return this.manifold;
    }

    public boolean isSensorOnly() {
        return this.sensorOnly;
    }

    public double getTimeOfImpact() {
        return this.timeOfImpact;
    }

    public Vector2 getNormal() {
        return this.manifold.getNormal();
    }

    public double getPenetrationDepth() {
        return this.manifold.getPenetrationDepth();
    }
}
