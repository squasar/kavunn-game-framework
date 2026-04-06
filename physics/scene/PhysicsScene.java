package physics.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import core.Entity;
import core.Relation;
import physics.body.PhysicsBody;
import physics.geometry.Vector2;

public abstract class PhysicsScene extends Entity<Relation> {

    public enum LifecycleState {
        CREATED,
        RUNNING,
        PAUSED,
        CLOSED,
        EXITED
    }

    private PhysicsSpace space;
    private LifecycleState lifecycleState = LifecycleState.CREATED;
    private final List<PhysicsMatter> matters = new ArrayList<>();
    private final List<PhysicsMatter> mattersView = Collections.unmodifiableList(this.matters);
    private Vector2 gravity = new Vector2(0.0, 9.81);

    protected PhysicsScene(int id, String label) {
        super(id, label);
    }

    public PhysicsSpace getSpace() {
        if (this.space == null) {
            throw new IllegalStateException("The physics scene is not mounted in a physics space yet.");
        }
        return this.space;
    }

    public void setSpace(PhysicsSpace space) {
        this.space = space;
    }

    public void addMatter(PhysicsMatter matter) {
        this.matters.add(matter);
    }

    public void removeMatter(PhysicsMatter matter) {
        this.matters.remove(matter);
    }

    public void clearMatters() {
        this.matters.clear();
    }

    public List<PhysicsMatter> getMatters() {
        return this.mattersView;
    }

    public List<PhysicsBody> getAllBodies() {
        List<PhysicsBody> bodies = new ArrayList<>();
        collectBodies(bodies);
        return bodies;
    }

    public void collectBodies(List<PhysicsBody> target) {
        target.clear();
        for (PhysicsMatter matter : this.matters) {
            matter.appendBodiesTo(target);
        }
    }

    public Vector2 getGravity() {
        return this.gravity;
    }

    public void setGravity(Vector2 gravity) {
        this.gravity = gravity;
    }

    public LifecycleState getLifecycleState() {
        return this.lifecycleState;
    }

    public boolean isCreated() {
        return this.lifecycleState == LifecycleState.CREATED;
    }

    public boolean isRunning() {
        return this.lifecycleState == LifecycleState.RUNNING;
    }

    public boolean isPaused() {
        return this.lifecycleState == LifecycleState.PAUSED;
    }

    public boolean isClosed() {
        return this.lifecycleState == LifecycleState.CLOSED;
    }

    public boolean isExited() {
        return this.lifecycleState == LifecycleState.EXITED;
    }

    public boolean isActive() {
        return isRunning() || isPaused();
    }

    public void startScene() {
        if (isExited()) {
            throw new IllegalStateException("An exited physics scene cannot be started again.");
        }
        this.lifecycleState = LifecycleState.RUNNING;
        onStart();
    }

    public void pauseScene() {
        if (!isRunning()) {
            return;
        }
        this.lifecycleState = LifecycleState.PAUSED;
        onPause();
    }

    public void resumeScene() {
        if (!isPaused()) {
            return;
        }
        this.lifecycleState = LifecycleState.RUNNING;
        onResume();
    }

    public void closeScene() {
        if (isExited()) {
            return;
        }
        this.lifecycleState = LifecycleState.CLOSED;
        onClose();
    }

    public void restartScene() {
        if (isExited()) {
            throw new IllegalStateException("An exited physics scene cannot be restarted.");
        }
        onRestart();
        this.lifecycleState = LifecycleState.RUNNING;
    }

    public void exitScene() {
        if (isExited()) {
            return;
        }
        this.lifecycleState = LifecycleState.EXITED;
        onExit();
    }

    protected boolean canSimulateScene() {
        return isRunning();
    }

    protected void onStart() {
    }

    protected void onPause() {
    }

    protected void onResume() {
    }

    protected void onClose() {
    }

    protected void onRestart() {
    }

    protected void onExit() {
    }
}
