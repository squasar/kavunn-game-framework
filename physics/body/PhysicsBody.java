package physics.body;

import core.Entity;
import core.PrimaryTypeValue;
import core.Relation;
import physics.collision.CollisionReport;
import physics.form.CircleForm;
import physics.form.Form2;
import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Transform2;
import physics.geometry.Vector2;

public class PhysicsBody extends Entity<Relation> {

    private final MotionState motionState = new MotionState();

    private Form2 form;
    private PhysicsMaterial material = PhysicsMaterial.DEFAULT;
    private double mass = 1.0;
    private double inverseMass = 1.0;
    private boolean dynamic = true;
    private boolean sensor = false;
    private boolean enabled = true;
    private int collisionLayer = 1;
    private int collisionMask = ~0;
    private CollisionReport lastCollision = CollisionReport.noHit();
    private Mesh2 cachedWorldMesh;
    private int cachedWorldMeshSubdivisionHint = Integer.MIN_VALUE;
    private Contour2 cachedWorldContour;
    private int cachedWorldContourSampleCount = Integer.MIN_VALUE;
    private Bounds2 cachedWorldBounds;

    public PhysicsBody(int id, String label, Form2 form) {
        super(id, label);
        this.form = form;
        syncMetadata();
    }

    public Form2 getForm() {
        return this.form;
    }

    public void setForm(Form2 form) {
        this.form = form;
        invalidateGeometryCaches();
        syncMetadata();
    }

    public MotionState getMotionState() {
        return this.motionState;
    }

    public PhysicsMaterial getMaterial() {
        return this.material;
    }

    public void setMaterial(PhysicsMaterial material) {
        this.material = material;
    }

    public double getMass() {
        return this.mass;
    }

    public double getInverseMass() {
        return this.inverseMass;
    }

    public void setMass(double mass) {
        if (mass <= 0.0) {
            this.mass = 0.0;
            this.inverseMass = 0.0;
            this.dynamic = false;
        } else {
            this.mass = mass;
            this.inverseMass = 1.0 / mass;
        }
        syncMetadata();
    }

    public boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        if (!dynamic) {
            this.mass = 0.0;
            this.inverseMass = 0.0;
        } else if (this.mass <= 0.0) {
            this.mass = 1.0;
            this.inverseMass = 1.0;
        }
        syncMetadata();
    }

    public boolean isSensor() {
        return this.sensor;
    }

    public void setSensor(boolean sensor) {
        this.sensor = sensor;
        syncMetadata();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        syncMetadata();
    }

    public int getCollisionLayer() {
        return this.collisionLayer;
    }

    public void setCollisionLayer(int collisionLayer) {
        this.collisionLayer = collisionLayer;
        syncMetadata();
    }

    public int getCollisionMask() {
        return this.collisionMask;
    }

    public void setCollisionMask(int collisionMask) {
        this.collisionMask = collisionMask;
        syncMetadata();
    }

    public Transform2 getTransform() {
        return this.motionState.getTransform();
    }

    public void setTransform(Transform2 transform) {
        this.motionState.setTransform(transform);
        invalidateGeometryCaches();
    }

    public Vector2 getPosition() {
        return this.motionState.getTransform().getTranslation();
    }

    public void setPosition(Vector2 position) {
        this.motionState.setTransform(this.motionState.getTransform().withTranslation(position));
        invalidateGeometryCaches();
    }

    public double getRotationRadians() {
        return this.motionState.getTransform().getRotationRadians();
    }

    public void setRotationRadians(double radians) {
        this.motionState.setTransform(this.motionState.getTransform().withRotation(radians));
        invalidateGeometryCaches();
    }

    public Vector2 getLinearVelocity() {
        return this.motionState.getLinearVelocity();
    }

    public void setLinearVelocity(Vector2 velocity) {
        this.motionState.setLinearVelocity(velocity);
    }

    public void applyForce(Vector2 force) {
        this.motionState.applyForce(force);
    }

    public void applyImpulse(Vector2 impulse) {
        if (this.inverseMass == 0.0) {
            return;
        }
        this.motionState.setLinearVelocity(this.motionState.getLinearVelocity().add(impulse.multiply(this.inverseMass)));
    }

    public void translate(Vector2 offset) {
        this.motionState.setTransform(this.motionState.getTransform().translate(offset));
        invalidateGeometryCaches();
    }

    public Mesh2 getWorldMesh(int subdivisionHint) {
        if (this.cachedWorldMesh == null || this.cachedWorldMeshSubdivisionHint != subdivisionHint) {
            this.cachedWorldMesh = this.form.toWorldMesh(getTransform(), subdivisionHint);
            this.cachedWorldMeshSubdivisionHint = subdivisionHint;
        }
        return this.cachedWorldMesh;
    }

    public Contour2 getWorldContour(int sampleCount) {
        if (this.cachedWorldContour == null || this.cachedWorldContourSampleCount != sampleCount) {
            this.cachedWorldContour = this.form.toWorldContour(getTransform(), sampleCount);
            this.cachedWorldContourSampleCount = sampleCount;
        }
        return this.cachedWorldContour;
    }

    public Bounds2 getWorldBounds() {
        if (this.cachedWorldBounds == null) {
            this.cachedWorldBounds = computeWorldBounds();
        }
        return this.cachedWorldBounds;
    }

    public CollisionReport getLastCollision() {
        return this.lastCollision;
    }

    public void setLastCollision(CollisionReport lastCollision) {
        this.lastCollision = lastCollision;
    }

    private void syncMetadata() {
        add("physicsEnabled", PrimaryTypeValue.bool(this.enabled));
        add("dynamic", PrimaryTypeValue.bool(this.dynamic));
        add("sensor", PrimaryTypeValue.bool(this.sensor));
        add("mass", PrimaryTypeValue.doubleVal(this.mass));
        add("collisionLayer", PrimaryTypeValue.integer(this.collisionLayer));
        add("shapeType", PrimaryTypeValue.string(this.form == null ? "none" : this.form.getFormType()));
    }

    private void invalidateGeometryCaches() {
        this.cachedWorldMesh = null;
        this.cachedWorldMeshSubdivisionHint = Integer.MIN_VALUE;
        this.cachedWorldContour = null;
        this.cachedWorldContourSampleCount = Integer.MIN_VALUE;
        this.cachedWorldBounds = null;
    }

    private Bounds2 computeWorldBounds() {
        Transform2 transform = getTransform();
        if (this.form instanceof CircleForm circleForm) {
            Vector2 center = transform.apply(circleForm.getCircle().getCenter());
            double radius = circleForm.getCircle().getRadius()
                * Math.max(Math.abs(transform.getScaleX()), Math.abs(transform.getScaleY()));
            if (radius == 0.0) {
                return new Bounds2(center.getX(), center.getY(), center.getX(), center.getY());
            }
            return new Bounds2(
                center.getX() - radius,
                center.getY() - radius,
                center.getX() + radius,
                center.getY() + radius
            );
        }

        Bounds2 localBounds = this.form.getLocalBounds();
        Vector2 bottomLeft = transform.apply(new Vector2(localBounds.getMinX(), localBounds.getMinY()));
        Vector2 bottomRight = transform.apply(new Vector2(localBounds.getMaxX(), localBounds.getMinY()));
        Vector2 topRight = transform.apply(new Vector2(localBounds.getMaxX(), localBounds.getMaxY()));
        Vector2 topLeft = transform.apply(new Vector2(localBounds.getMinX(), localBounds.getMaxY()));

        double minX = Math.min(Math.min(bottomLeft.getX(), bottomRight.getX()), Math.min(topRight.getX(), topLeft.getX()));
        double minY = Math.min(Math.min(bottomLeft.getY(), bottomRight.getY()), Math.min(topRight.getY(), topLeft.getY()));
        double maxX = Math.max(Math.max(bottomLeft.getX(), bottomRight.getX()), Math.max(topRight.getX(), topLeft.getX()));
        double maxY = Math.max(Math.max(bottomLeft.getY(), bottomRight.getY()), Math.max(topRight.getY(), topLeft.getY()));
        return new Bounds2(minX, minY, maxX, maxY);
    }
}
