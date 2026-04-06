package ashwake;

import core.Entity;
import core.PrimaryTypeValue;
import core.Relation;

class AshwakePlayer extends Entity<Relation> {

    static final int PLAYER_ID = 9101;

    private static final double BASE_MOVE_SPEED = 228.0;
    private static final double BASE_PROJECTILE_SPEED = 460.0;
    private static final double BASE_ATTACK_INTERVAL = 0.23;
    private static final double BASE_DASH_COOLDOWN = 1.15;
    private static final double BASE_DASH_SPEED = 720.0;
    private static final double BASE_CRIT_CHANCE = 0.08;
    private static final double BASE_HEALTH = 120.0;
    private static final double BASE_WARD = 40.0;
    private static final double BASE_ENERGY = 100.0;

    private double x;
    private double y;
    private double radius = 16.0;

    private double moveIntentX = 0.0;
    private double moveIntentY = 0.0;
    private double facingX = 1.0;
    private double facingY = 0.0;

    private double maxHealth = BASE_HEALTH;
    private double health = BASE_HEALTH;
    private double maxWard = BASE_WARD;
    private double ward = BASE_WARD;
    private double maxEnergy = BASE_ENERGY;
    private double energy = BASE_ENERGY;

    private double moveSpeed = BASE_MOVE_SPEED;
    private double projectileSpeed = BASE_PROJECTILE_SPEED;
    private double attackInterval = BASE_ATTACK_INTERVAL;
    private double attackCooldown = 0.0;
    private double dashCooldownDuration = BASE_DASH_COOLDOWN;
    private double dashCooldown = 0.0;
    private double dashSpeed = BASE_DASH_SPEED;
    private double dashTimeRemaining = 0.0;
    private double damageGraceTime = 0.0;
    private double critChance = BASE_CRIT_CHANCE;
    private double auraRadius = 0.0;
    private double auraDamagePerSecond = 0.0;
    private int projectilePierce = 0;
    private int splitShots = 0;
    private int shotCounter = 0;

    private boolean shockDash = false;
    private boolean emberBloom = false;
    private boolean chainburst = false;

    AshwakePlayer() {
        super(PLAYER_ID, "ashwake-player");
        updateRenderMetadata("idle");
    }

    public void resetForNewRun(double spawnX, double spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.radius = 16.0;
        this.moveIntentX = 0.0;
        this.moveIntentY = 0.0;
        this.facingX = 1.0;
        this.facingY = 0.0;
        this.maxHealth = BASE_HEALTH;
        this.health = BASE_HEALTH;
        this.maxWard = BASE_WARD;
        this.ward = BASE_WARD;
        this.maxEnergy = BASE_ENERGY;
        this.energy = BASE_ENERGY;
        this.moveSpeed = BASE_MOVE_SPEED;
        this.projectileSpeed = BASE_PROJECTILE_SPEED;
        this.attackInterval = BASE_ATTACK_INTERVAL;
        this.attackCooldown = 0.0;
        this.dashCooldownDuration = BASE_DASH_COOLDOWN;
        this.dashCooldown = 0.0;
        this.dashSpeed = BASE_DASH_SPEED;
        this.dashTimeRemaining = 0.0;
        this.damageGraceTime = 0.0;
        this.critChance = BASE_CRIT_CHANCE;
        this.auraRadius = 0.0;
        this.auraDamagePerSecond = 0.0;
        this.projectilePierce = 0;
        this.splitShots = 0;
        this.shotCounter = 0;
        this.shockDash = false;
        this.emberBloom = false;
        this.chainburst = false;
        updateRenderMetadata("idle");
    }

    public void update(double deltaSeconds, AshwakeRoomWorld room) {
        this.attackCooldown = Math.max(0.0, this.attackCooldown - deltaSeconds);
        this.dashCooldown = Math.max(0.0, this.dashCooldown - deltaSeconds);
        this.damageGraceTime = Math.max(0.0, this.damageGraceTime - deltaSeconds);
        this.energy = Math.min(this.maxEnergy, this.energy + (22.0 * deltaSeconds));
        this.ward = Math.min(this.maxWard, this.ward + (5.5 * deltaSeconds));

        double dx = 0.0;
        double dy = 0.0;

        if (this.dashTimeRemaining > 0.0) {
            this.dashTimeRemaining = Math.max(0.0, this.dashTimeRemaining - deltaSeconds);
            dx = this.facingX * this.dashSpeed * deltaSeconds;
            dy = this.facingY * this.dashSpeed * deltaSeconds;
            updateRenderMetadata("dash");
        } else {
            double length = Math.hypot(this.moveIntentX, this.moveIntentY);
            if (length > 0.0) {
                double normX = this.moveIntentX / length;
                double normY = this.moveIntentY / length;
                this.facingX = normX;
                this.facingY = normY;
                dx = normX * this.moveSpeed * deltaSeconds;
                dy = normY * this.moveSpeed * deltaSeconds;
                updateRenderMetadata("move");
            } else {
                updateRenderMetadata("idle");
            }
        }

        this.x = clamp(this.x + dx, this.radius, room.getWidth() - this.radius);
        this.y = clamp(this.y + dy, this.radius, room.getHeight() - this.radius);
        add("sortBias", PrimaryTypeValue.doubleVal(this.y));
    }

    public void setMoveIntent(double dx, double dy) {
        this.moveIntentX = dx;
        this.moveIntentY = dy;
    }

    public void relocate(double x, double y) {
        this.x = x;
        this.y = y;
        this.moveIntentX = 0.0;
        this.moveIntentY = 0.0;
    }

    public boolean canFire() {
        return this.attackCooldown <= 0.0 && !isDead();
    }

    public void consumeAttack() {
        this.attackCooldown = this.attackInterval;
        this.shotCounter++;
    }

    public boolean canDash() {
        return this.dashCooldown <= 0.0 && this.energy >= 22.0 && !isDead();
    }

    public boolean activateDash(double directionX, double directionY) {
        if (!canDash()) {
            return false;
        }

        double length = Math.hypot(directionX, directionY);
        if (length == 0.0) {
            directionX = this.facingX;
            directionY = this.facingY;
            length = Math.hypot(directionX, directionY);
        }

        if (length == 0.0) {
            directionX = 1.0;
            directionY = 0.0;
            length = 1.0;
        }

        this.facingX = directionX / length;
        this.facingY = directionY / length;
        this.energy = Math.max(0.0, this.energy - 22.0);
        this.dashCooldown = this.dashCooldownDuration;
        this.dashTimeRemaining = 0.15;
        this.damageGraceTime = Math.max(this.damageGraceTime, 0.20);
        updateRenderMetadata("dash");
        return true;
    }

    public boolean canUseSecondary() {
        return this.energy >= 40.0 && !isDead();
    }

    public void consumeSecondary() {
        this.energy = Math.max(0.0, this.energy - 40.0);
    }

    public boolean takeDamage(double damage) {
        if (damage <= 0.0 || this.damageGraceTime > 0.0 || isDead()) {
            return false;
        }

        double remaining = damage;
        if (this.ward > 0.0) {
            double absorbed = Math.min(this.ward, remaining);
            this.ward -= absorbed;
            remaining -= absorbed;
        }

        if (remaining > 0.0) {
            this.health = Math.max(0.0, this.health - remaining);
        }

        this.damageGraceTime = 0.36;
        updateRenderMetadata("hurt");
        return true;
    }

    public void heal(double amount) {
        this.health = Math.min(this.maxHealth, this.health + amount);
    }

    public void restoreEnergy(double amount) {
        this.energy = Math.min(this.maxEnergy, this.energy + amount);
    }

    public void gainWard(double amount) {
        this.ward = Math.min(this.maxWard, this.ward + amount);
    }

    public void addSplitShots(int amount) {
        this.splitShots += Math.max(0, amount);
    }

    public void addProjectilePierce(int amount) {
        this.projectilePierce += Math.max(0, amount);
    }

    public void enableShockDash() {
        this.shockDash = true;
    }

    public void enableEmberBloom() {
        this.emberBloom = true;
    }

    public void enableWardAura(double radius, double dps) {
        this.auraRadius = Math.max(this.auraRadius, radius);
        this.auraDamagePerSecond = Math.max(this.auraDamagePerSecond, dps);
    }

    public void multiplyAttackInterval(double multiplier) {
        this.attackInterval *= multiplier;
    }

    public void multiplyProjectileSpeed(double multiplier) {
        this.projectileSpeed *= multiplier;
    }

    public void multiplyMoveSpeed(double multiplier) {
        this.moveSpeed *= multiplier;
    }

    public void addCritChance(double amount) {
        this.critChance += amount;
    }

    public void enableChainburst() {
        this.chainburst = true;
    }

    public boolean hasShockDash() {
        return this.shockDash;
    }

    public boolean hasEmberBloom() {
        return this.emberBloom;
    }

    public boolean hasWardAura() {
        return this.auraRadius > 0.0 && this.auraDamagePerSecond > 0.0;
    }

    public boolean hasChainburst() {
        return this.chainburst;
    }

    public boolean shouldTriggerChainburst() {
        return this.chainburst && this.shotCounter > 0 && this.shotCounter % 5 == 0;
    }

    public boolean rollCritical(double randomValue) {
        return randomValue < this.critChance;
    }

    public boolean isDashing() {
        return this.dashTimeRemaining > 0.0;
    }

    public boolean isDead() {
        return this.health <= 0.0;
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

    public double getFacingX() {
        return this.facingX;
    }

    public double getFacingY() {
        return this.facingY;
    }

    public double getHealth() {
        return this.health;
    }

    public double getMaxHealth() {
        return this.maxHealth;
    }

    public double getWard() {
        return this.ward;
    }

    public double getMaxWard() {
        return this.maxWard;
    }

    public double getEnergy() {
        return this.energy;
    }

    public double getMaxEnergy() {
        return this.maxEnergy;
    }

    public double getProjectileSpeed() {
        return this.projectileSpeed;
    }

    public int getProjectilePierce() {
        return this.projectilePierce;
    }

    public int getSplitShots() {
        return this.splitShots;
    }

    public double getAuraRadius() {
        return this.auraRadius;
    }

    public double getAuraDamagePerSecond() {
        return this.auraDamagePerSecond;
    }

    private void updateRenderMetadata(String animationState) {
        add("renderCategory", PrimaryTypeValue.string("player"));
        add("layerHint", PrimaryTypeValue.string("actors"));
        add("materialKey", PrimaryTypeValue.string("ember-survivor"));
        add("animationState", PrimaryTypeValue.string(animationState));
        add("visible", PrimaryTypeValue.bool(true));
        add("opacity", PrimaryTypeValue.doubleVal(isDead() ? 0.35 : 1.0));
        add("glowLevel", PrimaryTypeValue.doubleVal(this.damageGraceTime > 0.0 ? 0.85 : 0.45));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
