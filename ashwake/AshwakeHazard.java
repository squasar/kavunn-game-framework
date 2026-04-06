package ashwake;

import core.Entity;
import core.PrimaryTypeValue;
import core.Relation;

class AshwakeHazard extends Entity<Relation> {

    private final boolean friendly;
    private double x;
    private double y;
    private double radius;
    private double lifetime;
    private double damagePerSecond;
    private double pulsePhase = 0.0;
    private boolean active = true;

    AshwakeHazard(int id, boolean friendly, double x, double y, double radius, double lifetime, double damagePerSecond) {
        super(id, "ashwake-hazard");
        this.friendly = friendly;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.lifetime = lifetime;
        this.damagePerSecond = damagePerSecond;
        updateRenderMetadata();
    }

    public void update(double deltaSeconds) {
        if (!this.active) {
            return;
        }

        this.lifetime -= deltaSeconds;
        this.pulsePhase += deltaSeconds * 2.8;
        add("glowLevel", PrimaryTypeValue.doubleVal(0.45 + (0.22 * Math.sin(this.pulsePhase))));

        if (this.lifetime <= 0.0) {
            this.active = false;
            add("visible", PrimaryTypeValue.bool(false));
        }
    }

    public boolean isFriendly() {
        return this.friendly;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getRadius() {
        return this.radius;
    }

    public double getDamagePerSecond() {
        return this.damagePerSecond;
    }

    public double getPulsePhase() {
        return this.pulsePhase;
    }

    public boolean isActive() {
        return this.active;
    }

    private void updateRenderMetadata() {
        add("renderCategory", PrimaryTypeValue.string("hazard"));
        add("layerHint", PrimaryTypeValue.string("hazards"));
        add("materialKey", PrimaryTypeValue.string(this.friendly ? "ember-hazard" : "cursed-ground"));
        add("visible", PrimaryTypeValue.bool(this.active));
        add("opacity", PrimaryTypeValue.doubleVal(0.72));
        add("glowLevel", PrimaryTypeValue.doubleVal(0.45));
    }
}
