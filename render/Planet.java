package render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import core.Entity;
import core.Relation;

/*
A planet is the meaningful main part of the entities in the universe.
The real agreement it provides is the core deal of how each world or planetory system gets created and rendered accordingly.
It is the builder of the universe.
*/

public abstract class Planet extends Entity<Relation> {

    public enum LifecycleState {
        CREATED,
        RUNNING,
        PAUSED,
        CLOSED,
        EXITED
    }

    private Universe universe = null;
    private LifecycleState lifecycleState = LifecycleState.CREATED;
    private final List<Matter> matters = new ArrayList<>();

    public Planet(int planet_id, String planet_label) {
        super(planet_id, planet_label);
    }

    public Universe getUniverse() {
        if(this.universe == null){
            throw new IllegalStateException("The planet is not assigned to any universe yet.");
        }
        return this.universe;
    }

    public void setPlanetId(int id) {
        this.setId(id);
    }
    public void setPlanetLabel(String label) {
        this.setLabel(label);
    }

    public void setUniverse(Universe universe) {
        this.universe = universe;
    }

    public void addMatter(Matter matter) {
        if (matter == null) {
            throw new IllegalArgumentException("A planet cannot register a null matter.");
        }
        this.matters.add(matter);
    }

    public void removeMatter(Matter matter) {
        this.matters.remove(matter);
    }

    public void clearMatters() {
        this.matters.clear();
    }

    public List<Matter> getMatters() {
        return Collections.unmodifiableList(this.matters);
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
        return this.lifecycleState == LifecycleState.RUNNING
            || this.lifecycleState == LifecycleState.PAUSED;
    }

    public void startPlanet() {
        if (isExited()) {
            throw new IllegalStateException("An exited planet cannot be started again.");
        }
        this.lifecycleState = LifecycleState.RUNNING;
        onStart();
    }

    public void pausePlanet() {
        if (!isRunning()) {
            return;
        }
        this.lifecycleState = LifecycleState.PAUSED;
        onPause();
    }

    public void resumePlanet() {
        if (!isPaused()) {
            return;
        }
        this.lifecycleState = LifecycleState.RUNNING;
        onResume();
    }

    public void closePlanet() {
        if (isExited()) {
            return;
        }
        this.lifecycleState = LifecycleState.CLOSED;
        onClose();
    }

    public void restartPlanet() {
        if (isExited()) {
            throw new IllegalStateException("An exited planet cannot be restarted.");
        }
        onRestart();
        this.lifecycleState = LifecycleState.RUNNING;
    }

    public void exitPlanet() {
        if (isExited()) {
            return;
        }
        this.lifecycleState = LifecycleState.EXITED;
        onExit();
    }

    protected boolean canExecutePlanet() {
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
