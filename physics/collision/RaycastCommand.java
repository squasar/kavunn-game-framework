package physics.collision;

import core.Relation;
import physics.backend.PhysicsBackend;
import physics.geometry.Vector2;
import physics.scene.PhysicsScene;

public final class RaycastCommand implements Relation {

    private final PhysicsBackend backend;
    private final PhysicsScene scene;
    private final Vector2 origin;
    private final Vector2 direction;
    private final double maxDistance;
    private RaycastHit result = RaycastHit.noHit();

    public RaycastCommand(
        PhysicsBackend backend,
        PhysicsScene scene,
        Vector2 origin,
        Vector2 direction,
        double maxDistance
    ) {
        this.backend = backend;
        this.scene = scene;
        this.origin = origin;
        this.direction = direction;
        this.maxDistance = maxDistance;
    }

    @Override
    public void execute() {
        this.result = this.backend.raycast(this.scene, this.origin, this.direction, this.maxDistance);
    }

    public RaycastHit getResult() {
        return this.result;
    }
}
