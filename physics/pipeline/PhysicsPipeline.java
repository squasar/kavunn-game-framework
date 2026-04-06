package physics.pipeline;

import java.util.Collections;
import java.util.List;

import physics.backend.NoOpPhysicsBackend;
import physics.backend.PhysicsBackend;
import physics.body.PhysicsBody;
import physics.scene.PhysicsMatter;
import physics.scene.PhysicsScene;
import physics.scene.PhysicsSpace;

public abstract class PhysicsPipeline {

    public final void simulate(PhysicsSpace space, PhysicsScene scene, double deltaSeconds) {
        simulate(space, scene, deltaSeconds, NoOpPhysicsBackend.INSTANCE);
    }

    public final void simulate(PhysicsSpace space, PhysicsScene scene, double deltaSeconds, PhysicsBackend backend) {
        validate(space, scene, backend, deltaSeconds);
        prepareLifecycle(scene);
        bind(space, scene);
        backend.beginStep(space, scene, deltaSeconds);
        beforeSimulation(space, scene, backend, deltaSeconds);

        for (PhysicsMatter matter : resolveMatters(space, scene)) {
            if (matter == null) {
                continue;
            }

            backend.beforeMatter(space, scene, matter, deltaSeconds);
            beforeMatter(space, scene, matter, backend, deltaSeconds);

            for (PhysicsBody body : resolveBodies(space, scene, matter)) {
                if (body == null || !body.isEnabled()) {
                    continue;
                }

                backend.beforeBody(space, scene, matter, body, deltaSeconds);
                beforeBody(space, scene, matter, body, backend, deltaSeconds);

                for (PhysicsTask task : resolveTasks(space, scene, matter, body, backend, deltaSeconds)) {
                    if (task != null && task.supports(backend)) {
                        task.execute(backend, space, scene, matter, body, deltaSeconds);
                    }
                }

                afterBody(space, scene, matter, body, backend, deltaSeconds);
                backend.afterBody(space, scene, matter, body, deltaSeconds);
            }

            afterMatter(space, scene, matter, backend, deltaSeconds);
            backend.afterMatter(space, scene, matter, deltaSeconds);
        }

        backend.step(space, scene, deltaSeconds);
        afterSimulation(space, scene, backend, deltaSeconds);
        backend.endStep(space, scene, deltaSeconds);
    }

    protected void validate(PhysicsSpace space, PhysicsScene scene, PhysicsBackend backend, double deltaSeconds) {
        if (space == null) {
            throw new IllegalArgumentException("Physics simulation requires a space.");
        }
        if (scene == null) {
            throw new IllegalArgumentException("Physics simulation requires a scene.");
        }
        if (backend == null) {
            throw new IllegalArgumentException("Physics simulation requires a backend.");
        }
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("Physics simulation delta cannot be negative.");
        }
    }

    protected void prepareLifecycle(PhysicsScene scene) {
        if (scene.isExited()) {
            throw new IllegalStateException("An exited physics scene cannot be simulated.");
        }

        if (scene.isCreated() || scene.isClosed()) {
            scene.startScene();
        }
    }

    protected void bind(PhysicsSpace space, PhysicsScene scene) {
        space.mountScene(scene);
    }

    protected List<PhysicsMatter> resolveMatters(PhysicsSpace space, PhysicsScene scene) {
        return scene.getMatters();
    }

    protected List<PhysicsBody> resolveBodies(PhysicsSpace space, PhysicsScene scene, PhysicsMatter matter) {
        return matter.getBodies();
    }

    protected List<PhysicsTask> resolveTasks(
        PhysicsSpace space,
        PhysicsScene scene,
        PhysicsMatter matter,
        PhysicsBody body,
        PhysicsBackend backend,
        double deltaSeconds
    ) {
        return Collections.emptyList();
    }

    protected void beforeSimulation(PhysicsSpace space, PhysicsScene scene, PhysicsBackend backend, double deltaSeconds) {
    }

    protected void afterSimulation(PhysicsSpace space, PhysicsScene scene, PhysicsBackend backend, double deltaSeconds) {
    }

    protected void beforeMatter(
        PhysicsSpace space,
        PhysicsScene scene,
        PhysicsMatter matter,
        PhysicsBackend backend,
        double deltaSeconds
    ) {
    }

    protected void afterMatter(
        PhysicsSpace space,
        PhysicsScene scene,
        PhysicsMatter matter,
        PhysicsBackend backend,
        double deltaSeconds
    ) {
    }

    protected void beforeBody(
        PhysicsSpace space,
        PhysicsScene scene,
        PhysicsMatter matter,
        PhysicsBody body,
        PhysicsBackend backend,
        double deltaSeconds
    ) {
    }

    protected void afterBody(
        PhysicsSpace space,
        PhysicsScene scene,
        PhysicsMatter matter,
        PhysicsBody body,
        PhysicsBackend backend,
        double deltaSeconds
    ) {
    }
}
