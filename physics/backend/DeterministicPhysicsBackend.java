package physics.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import physics.body.PhysicsBody;
import physics.body.PhysicsMaterial;
import physics.collision.CollisionDetector2;
import physics.collision.CollisionReport;
import physics.collision.RaycastHit;
import physics.geometry.Transform2;
import physics.geometry.Vector2;
import physics.scene.PhysicsScene;
import physics.scene.PhysicsSpace;

public class DeterministicPhysicsBackend implements PhysicsBackend {

    private final int subdivisionHint;
    private final List<CollisionReport> lastReports = new ArrayList<>();
    private final List<CollisionReport> lastReportsView = Collections.unmodifiableList(this.lastReports);
    private final ArrayList<PhysicsBody> workingBodies = new ArrayList<>();
    private final ArrayList<PhysicsBody> candidateBodies = new ArrayList<>();
    private final SpatialHashBroadphase broadphase = new SpatialHashBroadphase(96.0);
    private final HashSet<Long> emittedPairs = new HashSet<>();
    private int lastActiveBodies;
    private long lastNaivePairs;
    private int lastCandidatePairs;
    private int lastCollisionChecks;
    private int lastCollisionHits;
    private int lastOccupiedCells;
    private int lastMaxBucketSize;

    public DeterministicPhysicsBackend() {
        this(24);
    }

    public DeterministicPhysicsBackend(int subdivisionHint) {
        this.subdivisionHint = Math.max(8, subdivisionHint);
    }

    @Override
    public String getBackendName() {
        return "deterministic-2d";
    }

    public List<CollisionReport> getLastReports() {
        return this.lastReportsView;
    }

    public int getLastActiveBodies() {
        return this.lastActiveBodies;
    }

    public long getLastNaivePairs() {
        return this.lastNaivePairs;
    }

    public int getLastCandidatePairs() {
        return this.lastCandidatePairs;
    }

    public int getLastCollisionChecks() {
        return this.lastCollisionChecks;
    }

    public int getLastCollisionHits() {
        return this.lastCollisionHits;
    }

    public int getLastOccupiedCells() {
        return this.lastOccupiedCells;
    }

    public int getLastMaxBucketSize() {
        return this.lastMaxBucketSize;
    }

    @Override
    public void step(PhysicsSpace space, PhysicsScene scene, double deltaSeconds) {
        this.lastReports.clear();
        this.lastActiveBodies = 0;
        this.lastNaivePairs = 0L;
        this.lastCandidatePairs = 0;
        this.lastCollisionChecks = 0;
        this.lastCollisionHits = 0;
        this.lastOccupiedCells = 0;
        this.lastMaxBucketSize = 0;
        if (!scene.isRunning()) {
            return;
        }

        scene.collectBodies(this.workingBodies);
        for (PhysicsBody body : this.workingBodies) {
            if (body != null && body.isEnabled()) {
                this.lastActiveBodies++;
            }
            integrateBody(body, scene.getGravity(), deltaSeconds);
            body.setLastCollision(CollisionReport.noHit());
        }
        this.lastNaivePairs = this.lastActiveBodies < 2 ? 0L : ((long) this.lastActiveBodies * (this.lastActiveBodies - 1L)) / 2L;

        this.broadphase.rebuild(this.workingBodies);
        this.lastOccupiedCells = this.broadphase.getOccupiedCellCount();
        this.lastMaxBucketSize = this.broadphase.getMaxBucketSize();
        this.emittedPairs.clear();

        for (PhysicsBody bodyA : this.workingBodies) {
            if (!bodyA.isEnabled()) {
                continue;
            }

            this.broadphase.collectCandidates(bodyA, this.candidateBodies, this.emittedPairs);
            this.lastCandidatePairs += this.candidateBodies.size();
            for (PhysicsBody bodyB : this.candidateBodies) {
                if (!shouldCollide(bodyA, bodyB)) {
                    continue;
                }

                this.lastCollisionChecks++;
                CollisionReport report = collide(bodyA, bodyB);
                if (!report.isHit()) {
                    continue;
                }

                this.lastCollisionHits++;
                bodyA.setLastCollision(report);
                bodyB.setLastCollision(report);
                this.lastReports.add(report);

                if (!report.isSensorOnly()) {
                    resolve(report);
                }
            }
        }
    }

    @Override
    public CollisionReport collide(PhysicsBody a, PhysicsBody b) {
        return CollisionDetector2.detect(a, b, this.subdivisionHint);
    }

    @Override
    public RaycastHit raycast(PhysicsScene scene, Vector2 origin, Vector2 direction, double maxDistance) {
        RaycastHit best = RaycastHit.noHit();
        scene.collectBodies(this.workingBodies);
        for (PhysicsBody body : this.workingBodies) {
            RaycastHit hit = CollisionDetector2.raycastBody(body, origin, direction, maxDistance, this.subdivisionHint);
            if (hit.isHit() && hit.getDistance() < best.getDistance()) {
                best = hit;
            }
        }
        return best;
    }

    @Override
    public CollisionReport sweep(PhysicsScene scene, PhysicsBody body, Vector2 delta) {
        double length = delta.length();
        if (length == 0.0) {
            return CollisionReport.noHit();
        }

        int steps = Math.max(8, Math.min(64, (int) Math.ceil(length / 4.0)));
        Transform2 originalTransform = body.getTransform();
        scene.collectBodies(this.workingBodies);

        for (int step = 1; step <= steps; step++) {
            double alpha = step / (double) steps;
            Transform2 sweptTransform = originalTransform.translate(delta.multiply(alpha));

            for (PhysicsBody other : this.workingBodies) {
                if (other == body || !shouldCollide(body, other)) {
                    continue;
                }

                CollisionReport report = CollisionDetector2.detect(
                    body,
                    sweptTransform,
                    other,
                    other.getTransform(),
                    this.subdivisionHint
                );
                if (report.isHit()) {
                    return new CollisionReport(
                        true,
                        report.getBodyA(),
                        report.getBodyB(),
                        report.getManifold(),
                        report.isSensorOnly(),
                        alpha
                    );
                }
            }
        }

        return CollisionReport.noHit();
    }

    private void integrateBody(PhysicsBody body, Vector2 gravity, double deltaSeconds) {
        if (!body.isEnabled() || !body.isDynamic() || body.getInverseMass() == 0.0) {
            body.getMotionState().clearForces();
            return;
        }

        Vector2 totalAcceleration = gravity.add(body.getMotionState().getAccumulatedForce().multiply(body.getInverseMass()));
        Vector2 linearVelocity = body.getLinearVelocity().add(totalAcceleration.multiply(deltaSeconds));

        PhysicsMaterial material = body.getMaterial() == null ? PhysicsMaterial.DEFAULT : body.getMaterial();
        double damping = Math.max(0.0, 1.0 - (material.getLinearDamping() * deltaSeconds));
        linearVelocity = linearVelocity.multiply(damping);
        body.setLinearVelocity(linearVelocity);

        Vector2 nextPosition = body.getPosition().add(linearVelocity.multiply(deltaSeconds));
        body.setPosition(nextPosition);

        double nextRotation = body.getRotationRadians() + (body.getMotionState().getAngularVelocity() * deltaSeconds);
        body.setRotationRadians(nextRotation);
        double angularDamping = Math.max(0.0, 1.0 - (material.getAngularDamping() * deltaSeconds));
        body.getMotionState().setAngularVelocity(body.getMotionState().getAngularVelocity() * angularDamping);
        body.getMotionState().clearForces();
    }

    private boolean shouldCollide(PhysicsBody a, PhysicsBody b) {
        if (a == null || b == null || !a.isEnabled() || !b.isEnabled()) {
            return false;
        }
        return (a.getCollisionMask() & b.getCollisionLayer()) != 0
            && (b.getCollisionMask() & a.getCollisionLayer()) != 0;
    }

    private void resolve(CollisionReport report) {
        PhysicsBody bodyA = report.getBodyA();
        PhysicsBody bodyB = report.getBodyB();
        if (bodyA == null || bodyB == null) {
            return;
        }

        double invMassA = bodyA.isDynamic() ? bodyA.getInverseMass() : 0.0;
        double invMassB = bodyB.isDynamic() ? bodyB.getInverseMass() : 0.0;
        double totalInvMass = invMassA + invMassB;
        if (totalInvMass == 0.0) {
            return;
        }

        Vector2 normal = report.getNormal().normalized();
        double depth = report.getPenetrationDepth();
        Vector2 correction = normal.multiply(depth / totalInvMass);

        if (invMassA > 0.0) {
            bodyA.translate(correction.multiply(-invMassA));
        }
        if (invMassB > 0.0) {
            bodyB.translate(correction.multiply(invMassB));
        }

        Vector2 relativeVelocity = bodyB.getLinearVelocity().subtract(bodyA.getLinearVelocity());
        double velocityAlongNormal = relativeVelocity.dot(normal);
        if (velocityAlongNormal > 0.0) {
            return;
        }

        double restitution = Math.min(
            bodyA.getMaterial().getRestitution(),
            bodyB.getMaterial().getRestitution()
        );
        double impulseMagnitude = -(1.0 + restitution) * velocityAlongNormal / totalInvMass;
        Vector2 impulse = normal.multiply(impulseMagnitude);

        if (invMassA > 0.0) {
            bodyA.setLinearVelocity(bodyA.getLinearVelocity().subtract(impulse.multiply(invMassA)));
        }
        if (invMassB > 0.0) {
            bodyB.setLinearVelocity(bodyB.getLinearVelocity().add(impulse.multiply(invMassB)));
        }

        Vector2 tangent = relativeVelocity.subtract(normal.multiply(relativeVelocity.dot(normal))).normalized();
        if (tangent.isZero()) {
            return;
        }

        double tangentVelocity = relativeVelocity.dot(tangent);
        double frictionCoefficient = (bodyA.getMaterial().getFriction() + bodyB.getMaterial().getFriction()) * 0.5;
        double frictionImpulseMagnitude = -tangentVelocity / totalInvMass;
        frictionImpulseMagnitude = GeometryClamp.clamp(
            frictionImpulseMagnitude,
            -impulseMagnitude * frictionCoefficient,
            impulseMagnitude * frictionCoefficient
        );
        Vector2 frictionImpulse = tangent.multiply(frictionImpulseMagnitude);

        if (invMassA > 0.0) {
            bodyA.setLinearVelocity(bodyA.getLinearVelocity().subtract(frictionImpulse.multiply(invMassA)));
        }
        if (invMassB > 0.0) {
            bodyB.setLinearVelocity(bodyB.getLinearVelocity().add(frictionImpulse.multiply(invMassB)));
        }
    }

    private static final class GeometryClamp {

        private GeometryClamp() {
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
