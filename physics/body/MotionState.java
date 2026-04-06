package physics.body;

import physics.geometry.Transform2;
import physics.geometry.Vector2;

public final class MotionState {

    private Transform2 transform = Transform2.identity();
    private Vector2 linearVelocity = Vector2.ZERO;
    private double angularVelocity = 0.0;
    private Vector2 accumulatedForce = Vector2.ZERO;
    private boolean sleeping = false;

    public Transform2 getTransform() {
        return this.transform;
    }

    public void setTransform(Transform2 transform) {
        this.transform = transform;
    }

    public Vector2 getLinearVelocity() {
        return this.linearVelocity;
    }

    public void setLinearVelocity(Vector2 linearVelocity) {
        this.linearVelocity = linearVelocity;
    }

    public double getAngularVelocity() {
        return this.angularVelocity;
    }

    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public Vector2 getAccumulatedForce() {
        return this.accumulatedForce;
    }

    public void applyForce(Vector2 force) {
        this.accumulatedForce = this.accumulatedForce.add(force);
    }

    public void clearForces() {
        this.accumulatedForce = Vector2.ZERO;
    }

    public boolean isSleeping() {
        return this.sleeping;
    }

    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
    }
}
