package render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultRenderingPipeline extends RenderingPipeline {

    private final List<RenderTask> tasks = new ArrayList<>();

    public void addTask(RenderTask task) {
        if (task == null) {
            throw new IllegalArgumentException("A rendering pipeline task cannot be null.");
        }
        this.tasks.add(task);
    }

    public void removeTask(RenderTask task) {
        this.tasks.remove(task);
    }

    public void clearTasks() {
        this.tasks.clear();
    }

    public List<RenderTask> getTasks() {
        return Collections.unmodifiableList(this.tasks);
    }

    @Override
    protected List<RenderTask> resolveTasks(
        Universe universe,
        Planet planet,
        Matter matter,
        RenderBackend backend
    ) {
        return this.tasks;
    }
}
