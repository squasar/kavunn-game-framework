package physics.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.backend.PhysicsBackend;
import physics.body.PhysicsBody;
import physics.scene.PhysicsMatter;
import physics.scene.PhysicsScene;
import physics.scene.PhysicsSpace;

public class DefaultPhysicsPipeline extends PhysicsPipeline {

    private final List<PhysicsTask> tasks = new ArrayList<>();

    public void addTask(PhysicsTask task) {
        if (task == null) {
            throw new IllegalArgumentException("A physics pipeline task cannot be null.");
        }
        this.tasks.add(task);
    }

    public void removeTask(PhysicsTask task) {
        this.tasks.remove(task);
    }

    public void clearTasks() {
        this.tasks.clear();
    }

    public List<PhysicsTask> getTasks() {
        return Collections.unmodifiableList(this.tasks);
    }

    @Override
    protected List<PhysicsTask> resolveTasks(
        PhysicsSpace space,
        PhysicsScene scene,
        PhysicsMatter matter,
        PhysicsBody body,
        PhysicsBackend backend,
        double deltaSeconds
    ) {
        return this.tasks;
    }
}
