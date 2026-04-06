package physics.backend;

import physics.body.PhysicsBody;
import physics.collision.CollisionReport;
import physics.collision.RaycastHit;
import physics.scene.PhysicsMatter;
import physics.scene.PhysicsScene;
import physics.scene.PhysicsSpace;
import physics.geometry.Vector2;

public interface PhysicsBackend {

    String getBackendName();

    default void beginStep(PhysicsSpace space, PhysicsScene scene, double deltaSeconds) {
    }

    default void beforeMatter(PhysicsSpace space, PhysicsScene scene, PhysicsMatter matter, double deltaSeconds) {
    }

    default void beforeBody(PhysicsSpace space, PhysicsScene scene, PhysicsMatter matter, PhysicsBody body, double deltaSeconds) {
    }

    default void afterBody(PhysicsSpace space, PhysicsScene scene, PhysicsMatter matter, PhysicsBody body, double deltaSeconds) {
    }

    default void afterMatter(PhysicsSpace space, PhysicsScene scene, PhysicsMatter matter, double deltaSeconds) {
    }

    void step(PhysicsSpace space, PhysicsScene scene, double deltaSeconds);

    CollisionReport collide(PhysicsBody a, PhysicsBody b);

    RaycastHit raycast(PhysicsScene scene, Vector2 origin, Vector2 direction, double maxDistance);

    CollisionReport sweep(PhysicsScene scene, PhysicsBody body, Vector2 delta);

    default void endStep(PhysicsSpace space, PhysicsScene scene, double deltaSeconds) {
    }
}
