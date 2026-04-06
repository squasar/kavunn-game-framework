package ashwake;

import core.Entity;
import core.PrimaryTypeValue;
import core.Relation;

class AshwakeEnemy extends Entity<Relation> {

    enum Kind {
        CHASER,
        CASTER,
        DASH_STRIKER,
        AREA_SEEDER,
        SUMMONER,
        BOSS
    }

    private final Kind kind;
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double radius;
    private double health;
    private double maxHealth;
    private double moveSpeed;
    private double contactDamage;
    private double attackCooldown;
    private double specialCooldown;
    private double telegraphTimer;
    private double lifetime = 0.0;
    private boolean alive = true;

    AshwakeEnemy(int id, Kind kind, double x, double y, boolean eliteScale) {
        super(id, "ashwake-enemy-" + kind.name().toLowerCase());
        this.kind = kind;
        this.x = x;
        this.y = y;
        configureStats(eliteScale);
        updateRenderMetadata("idle");
    }

    public void update(double deltaSeconds, AshwakeRunWorld world) {
        if (!this.alive) {
            return;
        }

        this.lifetime += deltaSeconds;
        this.attackCooldown = Math.max(0.0, this.attackCooldown - deltaSeconds);
        this.specialCooldown = Math.max(0.0, this.specialCooldown - deltaSeconds);
        this.telegraphTimer = Math.max(0.0, this.telegraphTimer - deltaSeconds);

        AshwakePlayer player = world.getPlayer();
        double toPlayerX = player.getX() - this.x;
        double toPlayerY = player.getY() - this.y;
        double distance = Math.max(0.001, Math.hypot(toPlayerX, toPlayerY));
        double dirX = toPlayerX / distance;
        double dirY = toPlayerY / distance;

        switch (this.kind) {
            case CHASER -> chase(deltaSeconds, dirX, dirY, world);
            case CASTER -> cast(deltaSeconds, dirX, dirY, distance, world);
            case DASH_STRIKER -> dashStrike(deltaSeconds, dirX, dirY, world);
            case AREA_SEEDER -> seedHazards(deltaSeconds, dirX, dirY, distance, world);
            case SUMMONER -> summon(deltaSeconds, dirX, dirY, distance, world);
            case BOSS -> bossLogic(deltaSeconds, dirX, dirY, distance, world);
        }

        this.x = clamp(this.x + (this.velocityX * deltaSeconds), this.radius, world.getRoomWorld().getWidth() - this.radius);
        this.y = clamp(this.y + (this.velocityY * deltaSeconds), this.radius, world.getRoomWorld().getHeight() - this.radius);
        this.velocityX *= 0.86;
        this.velocityY *= 0.86;

        add("sortBias", PrimaryTypeValue.doubleVal(this.y));
        updateRenderMetadata(this.telegraphTimer > 0.0 ? "telegraph" : "move");
    }

    private void chase(double deltaSeconds, double dirX, double dirY, AshwakeRunWorld world) {
        addImpulse(dirX * this.moveSpeed * deltaSeconds * 5.2, dirY * this.moveSpeed * deltaSeconds * 5.2);
        if (this.attackCooldown <= 0.0 && world.distanceToPlayer(this.x, this.y) < this.radius + world.getPlayer().getRadius() + 8.0) {
            world.damagePlayer(this.contactDamage);
            this.attackCooldown = 0.75;
        }
    }

    private void cast(double deltaSeconds, double dirX, double dirY, double distance, AshwakeRunWorld world) {
        double desiredSpeed = distance > 250.0 ? this.moveSpeed : (distance < 170.0 ? -this.moveSpeed * 0.8 : 0.0);
        addImpulse(dirX * desiredSpeed * deltaSeconds * 4.6, dirY * desiredSpeed * deltaSeconds * 4.6);

        if (this.attackCooldown <= 0.0) {
            world.spawnEnemyProjectile(this.x, this.y, dirX, dirY, AshwakeProjectile.Kind.CURSE_ORB, this.kind == Kind.BOSS);
            this.attackCooldown = this.kind == Kind.BOSS ? 0.34 : 1.05;
        }
    }

    private void dashStrike(double deltaSeconds, double dirX, double dirY, AshwakeRunWorld world) {
        if (this.telegraphTimer > 0.0) {
            if (this.telegraphTimer <= deltaSeconds) {
                addImpulse(dirX * this.moveSpeed * 6.8, dirY * this.moveSpeed * 6.8);
            }
            return;
        }

        if (this.specialCooldown <= 0.0) {
            this.specialCooldown = 2.2;
            this.telegraphTimer = 0.34;
            updateRenderMetadata("telegraph");
            return;
        }

        addImpulse(dirX * this.moveSpeed * deltaSeconds * 4.4, dirY * this.moveSpeed * deltaSeconds * 4.4);
        if (this.attackCooldown <= 0.0 && world.distanceToPlayer(this.x, this.y) < this.radius + world.getPlayer().getRadius() + 14.0) {
            world.damagePlayer(this.contactDamage * 1.15);
            this.attackCooldown = 0.85;
        }
    }

    private void seedHazards(double deltaSeconds, double dirX, double dirY, double distance, AshwakeRunWorld world) {
        double orbitX = -dirY;
        double orbitY = dirX;
        double forward = distance > 160.0 ? this.moveSpeed * 0.55 : -this.moveSpeed * 0.35;
        addImpulse((dirX * forward + orbitX * 50.0) * deltaSeconds * 4.0, (dirY * forward + orbitY * 50.0) * deltaSeconds * 4.0);

        if (this.specialCooldown <= 0.0) {
            world.spawnEnemyHazard(this.x, this.y, 44.0, 3.8, 11.0);
            this.specialCooldown = 1.9;
        }
    }

    private void summon(double deltaSeconds, double dirX, double dirY, double distance, AshwakeRunWorld world) {
        double desiredSpeed = distance > 260.0 ? this.moveSpeed * 0.5 : (distance < 170.0 ? -this.moveSpeed * 0.65 : 0.0);
        addImpulse(dirX * desiredSpeed * deltaSeconds * 3.4, dirY * desiredSpeed * deltaSeconds * 3.4);

        if (this.specialCooldown <= 0.0) {
            this.specialCooldown = 4.2;
            world.spawnMinion(this.x - 28.0, this.y + 24.0);
            world.spawnMinion(this.x + 28.0, this.y - 24.0);
        }

        if (this.attackCooldown <= 0.0) {
            world.spawnEnemyProjectile(this.x, this.y, dirX, dirY, AshwakeProjectile.Kind.SHADOW_NEEDLE, false);
            this.attackCooldown = 0.72;
        }
    }

    private void bossLogic(double deltaSeconds, double dirX, double dirY, double distance, AshwakeRunWorld world) {
        double healthRatio = this.health / this.maxHealth;
        double drift = healthRatio < 0.45 ? 38.0 : 18.0;
        addImpulse((-dirY) * drift * deltaSeconds * 2.4, dirX * drift * deltaSeconds * 2.4);

        if (distance > 310.0) {
            addImpulse(dirX * this.moveSpeed * deltaSeconds * 2.4, dirY * this.moveSpeed * deltaSeconds * 2.4);
        }

        if (this.attackCooldown <= 0.0) {
            double spread = healthRatio < 0.45 ? 0.46 : 0.28;
            world.spawnProjectileSpread(this.x, this.y, dirX, dirY, 3, spread, false, AshwakeProjectile.Kind.BOSS_COMET);
            this.attackCooldown = healthRatio < 0.45 ? 0.55 : 0.92;
        }

        if (this.specialCooldown <= 0.0) {
            world.spawnEnemyHazard(this.x, this.y, healthRatio < 0.45 ? 86.0 : 68.0, 4.8, 16.0);
            world.spawnMinion(this.x - 60.0, this.y + 22.0);
            this.specialCooldown = healthRatio < 0.45 ? 3.0 : 4.4;
        }
    }

    public boolean takeDamage(double damage) {
        if (!this.alive) {
            return false;
        }

        this.health = Math.max(0.0, this.health - damage);
        updateRenderMetadata("hurt");
        if (this.health <= 0.0) {
            this.alive = false;
            add("visible", PrimaryTypeValue.bool(false));
            return true;
        }
        return false;
    }

    public void addImpulse(double dx, double dy) {
        this.velocityX += dx;
        this.velocityY += dy;
    }

    public Kind getKind() {
        return this.kind;
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

    public double getHealth() {
        return this.health;
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }

    public double getContactDamage() {
        return this.contactDamage;
    }

    public boolean isAlive() {
        return this.alive;
    }

    private void configureStats(boolean eliteScale) {
        switch (this.kind) {
            case CHASER -> {
                this.radius = 14.0;
                this.maxHealth = 28.0;
                this.moveSpeed = 128.0;
                this.contactDamage = 12.0;
            }
            case CASTER -> {
                this.radius = 13.0;
                this.maxHealth = 24.0;
                this.moveSpeed = 92.0;
                this.contactDamage = 6.0;
            }
            case DASH_STRIKER -> {
                this.radius = 15.0;
                this.maxHealth = 34.0;
                this.moveSpeed = 154.0;
                this.contactDamage = 17.0;
            }
            case AREA_SEEDER -> {
                this.radius = 16.0;
                this.maxHealth = 42.0;
                this.moveSpeed = 72.0;
                this.contactDamage = 8.0;
            }
            case SUMMONER -> {
                this.radius = 18.0;
                this.maxHealth = 58.0;
                this.moveSpeed = 68.0;
                this.contactDamage = 10.0;
            }
            case BOSS -> {
                this.radius = 34.0;
                this.maxHealth = 360.0;
                this.moveSpeed = 86.0;
                this.contactDamage = 26.0;
            }
        }

        if (eliteScale && this.kind != Kind.BOSS) {
            this.maxHealth *= 1.45;
            this.moveSpeed *= 1.08;
            this.contactDamage *= 1.2;
        }

        this.health = this.maxHealth;
    }

    private void updateRenderMetadata(String animationState) {
        add("renderCategory", PrimaryTypeValue.string("enemy"));
        add("layerHint", PrimaryTypeValue.string("actors"));
        add("enemyType", PrimaryTypeValue.string(this.kind.name().toLowerCase()));
        add("materialKey", PrimaryTypeValue.string(this.kind == Kind.BOSS ? "obsidian-boss" : "ash-enemy"));
        add("animationState", PrimaryTypeValue.string(animationState));
        add("visible", PrimaryTypeValue.bool(this.alive));
        add("opacity", PrimaryTypeValue.doubleVal(this.alive ? 1.0 : 0.0));
        add("glowLevel", PrimaryTypeValue.doubleVal(this.telegraphTimer > 0.0 ? 0.92 : 0.26));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
