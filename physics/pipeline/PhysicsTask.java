package physics.pipeline;

import physics.backend.PhysicsBackend;
import physics.body.PhysicsBody;
import physics.scene.PhysicsMatter;
import physics.scene.PhysicsScene;
import physics.scene.PhysicsSpace;

@FunctionalInterface
public interface PhysicsTask {

    void execute(
        PhysicsBackend backend,
        PhysicsSpace space,
        PhysicsScene scene,
        PhysicsMatter matter,
        PhysicsBody body,
        double deltaSeconds
    );

    default boolean supports(PhysicsBackend backend) {
        return true;
    }

    default String getTaskName() {
        return this.getClass().getSimpleName();
    }
}
