package ashwake;

import core.Entity;
import core.PrimaryTypeValue;
import core.Relation;

class AshwakePickup extends Entity<Relation> {

    enum Type {
        HEALTH_ORB,
        EMBER_SHARD,
        ENERGY_BLOOM
    }

    private final Type type;
    private double x;
    private double y;
    private double radius;
    private double value;
    private double bobPhase = 0.0;
    private boolean active = true;

    AshwakePickup(int id, Type type, double x, double y, double value) {
        super(id, "ashwake-pickup-" + type.name().toLowerCase());
        this.type = type;
        this.x = x;
        this.y = y;
        this.value = value;
        this.radius = switch (type) {
            case HEALTH_ORB -> 10.0;
            case EMBER_SHARD -> 8.5;
            case ENERGY_BLOOM -> 9.5;
        };
        updateRenderMetadata();
    }

    public void update(double deltaSeconds) {
        if (!this.active) {
            return;
        }

        this.bobPhase += deltaSeconds * 2.6;
        add("glowLevel", PrimaryTypeValue.doubleVal(0.40 + (0.18 * Math.sin(this.bobPhase))));
    }

    public void collect() {
        this.active = false;
        add("visible", PrimaryTypeValue.bool(false));
    }

    public Type getType() {
        return this.type;
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

    public double getValue() {
        return this.value;
    }

    public double getBobOffset() {
        return Math.sin(this.bobPhase) * 4.0;
    }

    public boolean isActive() {
        return this.active;
    }

    private void updateRenderMetadata() {
        add("renderCategory", PrimaryTypeValue.string("pickup"));
        add("layerHint", PrimaryTypeValue.string("pickups"));
        add("pickupType", PrimaryTypeValue.string(this.type.name().toLowerCase()));
        add("materialKey", PrimaryTypeValue.string("ritual-loot"));
        add("visible", PrimaryTypeValue.bool(this.active));
        add("opacity", PrimaryTypeValue.doubleVal(1.0));
        add("glowLevel", PrimaryTypeValue.doubleVal(0.42));
    }
}
