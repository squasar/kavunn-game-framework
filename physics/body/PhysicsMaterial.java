package physics.body;

public final class PhysicsMaterial {

    public static final PhysicsMaterial DEFAULT = new PhysicsMaterial(0.12, 0.35, 1.0, 0.04, 0.04);

    private final double restitution;
    private final double friction;
    private final double density;
    private final double linearDamping;
    private final double angularDamping;

    public PhysicsMaterial(
        double restitution,
        double friction,
        double density,
        double linearDamping,
        double angularDamping
    ) {
        this.restitution = restitution;
        this.friction = friction;
        this.density = density;
        this.linearDamping = linearDamping;
        this.angularDamping = angularDamping;
    }

    public double getRestitution() {
        return this.restitution;
    }

    public double getFriction() {
        return this.friction;
    }

    public double getDensity() {
        return this.density;
    }

    public double getLinearDamping() {
        return this.linearDamping;
    }

    public double getAngularDamping() {
        return this.angularDamping;
    }
}
