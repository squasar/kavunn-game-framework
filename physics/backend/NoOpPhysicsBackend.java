package physics.backend;

import physics.body.PhysicsBody;
import physics.collision.CollisionReport;
import physics.collision.RaycastHit;
import physics.geometry.Vector2;
import physics.scene.PhysicsScene;
import physics.scene.PhysicsSpace;

public final class NoOpPhysicsBackend implements PhysicsBackend {

    public static final NoOpPhysicsBackend INSTANCE = new NoOpPhysicsBackend();

    private NoOpPhysicsBackend() {
    }

    @Override
    public String getBackendName() {
        return "noop-physics";
    }

    @Override
    public void step(PhysicsSpace space, PhysicsScene scene, double deltaSeconds) {
    }

    @Override
    public CollisionReport collide(PhysicsBody a, PhysicsBody b) {
        return CollisionReport.noHit();
    }

    @Override
    public RaycastHit raycast(PhysicsScene scene, Vector2 origin, Vector2 direction, double maxDistance) {
        return RaycastHit.noHit();
    }

    @Override
    public CollisionReport sweep(PhysicsScene scene, PhysicsBody body, Vector2 delta) {
        return CollisionReport.noHit();
    }
}
