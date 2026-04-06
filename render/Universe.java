package render;

import core.Context;

/*
The universe has a context which can explain all the entities and their relationships.
Also, the universe itself is a materialistic component. 
The universe is the guidance people look for when they define subconciously how do they look things.
--The context defined here is the real valid context.
*/

public class Universe extends Matter {

    private Context renderingContext = new Context();
    private Planet activePlanet = null;

    public Universe(int universe_id, String universe_label) {
        super(universe_id, universe_label);
    }

    public void setRenderingContext(Context context) {
        this.renderingContext = context;
    }

    public Context getRenderingContext() {
        return this.renderingContext;
    }

    public void mountPlanet(Planet planet) {
        if (planet == null) {
            throw new IllegalArgumentException("A universe cannot mount a null planet.");
        }

        this.activePlanet = planet;
        planet.setUniverse(this);

        if (!this.renderingContext.getEntities().contains(planet)) {
            this.renderingContext.getEntities().add(planet);
        }

        this.renderingContext.setState(planet);
    }

    public Planet getActivePlanet() {
        return this.activePlanet;
    }
}
