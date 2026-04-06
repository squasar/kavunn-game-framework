package physics.collision;

import core.Relation;
import physics.backend.PhysicsBackend;
import physics.body.PhysicsBody;

public final class OverlapCommand implements Relation {

    private final PhysicsBackend backend;
    private final PhysicsBody bodyA;
    private final PhysicsBody bodyB;
    private CollisionReport result = CollisionReport.noHit();

    public OverlapCommand(PhysicsBackend backend, PhysicsBody bodyA, PhysicsBody bodyB) {
        this.backend = backend;
        this.bodyA = bodyA;
        this.bodyB = bodyB;
    }

    @Override
    public void execute() {
        this.result = this.backend.collide(this.bodyA, this.bodyB);
    }

    public CollisionReport getResult() {
        return this.result;
    }
}
