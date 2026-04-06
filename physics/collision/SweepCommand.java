package physics.collision;

import core.Relation;
import physics.backend.PhysicsBackend;
import physics.body.PhysicsBody;
import physics.geometry.Vector2;
import physics.scene.PhysicsScene;

public final class SweepCommand implements Relation {

    private final PhysicsBackend backend;
    private final PhysicsScene scene;
    private final PhysicsBody body;
    private final Vector2 delta;
    private CollisionReport result = CollisionReport.noHit();

    public SweepCommand(PhysicsBackend backend, PhysicsScene scene, PhysicsBody body, Vector2 delta) {
        this.backend = backend;
        this.scene = scene;
        this.body = body;
        this.delta = delta;
    }

    @Override
    public void execute() {
        this.result = this.backend.sweep(this.scene, this.body, this.delta);
    }

    public CollisionReport getResult() {
        return this.result;
    }
}
