package ashwake;

import core.Entity;
import core.PrimaryTypeValue;
import core.Relation;

class AshwakeProjectile extends Entity<Relation> {

    enum Kind {
        EMBER_BOLT,
        CURSE_ORB,
        SHADOW_NEEDLE,
        PULSE_SPARK,
        BOSS_COMET
    }

    private final Kind kind;
    private final boolean friendly;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double radius;
    private double damage;
    private double lifetime;
    private int pierceRemaining;
    private boolean alive = true;

    AshwakeProjectile(
        int id,
        Kind kind,
        boolean friendly,
        double x,
        double y,
        double velocityX,
        double velocityY,
        double damage,
        double lifetime,
        int pierceRemaining
    ) {
        super(id, "ashwake-projectile-" + kind.name().toLowerCase());
        this.kind = kind;
        this.friendly = friendly;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.lifetime = lifetime;
        this.pierceRemaining = pierceRemaining;
        configureRadius();
        updateRenderMetadata();
    }

    public void update(double deltaSeconds, AshwakeRoomWorld room) {
        if (!this.alive) {
            return;
        }

        this.x += this.velocityX * deltaSeconds;
        this.y += this.velocityY * deltaSeconds;
        this.lifetime -= deltaSeconds;
        add("sortBias", PrimaryTypeValue.doubleVal(this.y));

        if (this.lifetime <= 0.0
            || this.x < -40.0
            || this.y < -40.0
            || this.x > room.getWidth() + 40.0
            || this.y > room.getHeight() + 40.0) {
            destroy();
        }
    }

    public void onHit() {
        if (this.pierceRemaining > 0) {
            this.pierceRemaining--;
            this.damage *= 0.88;
            return;
        }
        destroy();
    }

    public void destroy() {
        this.alive = false;
        add("visible", PrimaryTypeValue.bool(false));
    }

    public Kind getKind() {
        return this.kind;
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

    public double getDamage() {
        return this.damage;
    }

    public boolean isAlive() {
        return this.alive;
    }

    private void configureRadius() {
        switch (this.kind) {
            case EMBER_BOLT -> this.radius = 6.0;
            case CURSE_ORB -> this.radius = 7.0;
            case SHADOW_NEEDLE -> this.radius = 4.5;
            case PULSE_SPARK -> this.radius = 5.5;
            case BOSS_COMET -> this.radius = 10.0;
        }
    }

    private void updateRenderMetadata() {
        add("renderCategory", PrimaryTypeValue.string("projectile"));
        add("layerHint", PrimaryTypeValue.string("projectiles"));
        add("projectileType", PrimaryTypeValue.string(this.kind.name().toLowerCase()));
        add("materialKey", PrimaryTypeValue.string(this.friendly ? "ember-shot" : "void-shot"));
        add("visible", PrimaryTypeValue.bool(this.alive));
        add("opacity", PrimaryTypeValue.doubleVal(0.96));
        add("glowLevel", PrimaryTypeValue.doubleVal(this.kind == Kind.BOSS_COMET ? 0.92 : 0.55));
    }
}
