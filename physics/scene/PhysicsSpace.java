package physics.scene;

import core.Context;

public class PhysicsSpace {

    private final Context simulationContext = new Context();
    private PhysicsScene activeScene;

    public Context getSimulationContext() {
        return this.simulationContext;
    }

    public void mountScene(PhysicsScene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("A physics space cannot mount a null scene.");
        }

        this.activeScene = scene;
        scene.setSpace(this);

        if (!this.simulationContext.getEntities().contains(scene)) {
            this.simulationContext.getEntities().add(scene);
        }

        this.simulationContext.setState(scene);
    }

    public PhysicsScene getActiveScene() {
        return this.activeScene;
    }
}
