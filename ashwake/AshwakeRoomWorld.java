package ashwake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import core.Entity;
import core.PrimaryTypeValue;
import core.Relation;

class AshwakeRoomWorld extends Entity<Relation> {

    static final int ROOM_ID = 9102;
    static final int ROOM_WIDTH = 960;
    static final int ROOM_HEIGHT = 640;

    private final List<AshwakeEnemy> enemies = new ArrayList<>();
    private final List<AshwakeProjectile> projectiles = new ArrayList<>();
    private final List<AshwakeHazard> hazards = new ArrayList<>();
    private final List<AshwakePickup> pickups = new ArrayList<>();

    private AshwakeRoomKind roomKind = AshwakeRoomKind.COMBAT;
    private int roomIndex = 0;
    private boolean encounterCleared = false;
    private double roomTime = 0.0;

    AshwakeRoomWorld() {
        super(ROOM_ID, "ashwake-room");
        updateRenderMetadata();
    }

    public void reset(int roomIndex, AshwakeRoomKind roomKind) {
        this.roomIndex = roomIndex;
        this.roomKind = roomKind;
        this.encounterCleared = !roomKind.isCombatRoom();
        this.roomTime = 0.0;
        this.enemies.clear();
        this.projectiles.clear();
        this.hazards.clear();
        this.pickups.clear();
        getChildren().clear();
        updateRenderMetadata();
    }

    public void update(double deltaSeconds) {
        this.roomTime += deltaSeconds;

        for (AshwakeEnemy enemy : this.enemies) {
            add("sortBias", PrimaryTypeValue.doubleVal(enemy.getY()));
        }

        for (AshwakeProjectile projectile : this.projectiles) {
            projectile.update(deltaSeconds, this);
        }
        for (AshwakeHazard hazard : this.hazards) {
            hazard.update(deltaSeconds);
        }
        for (AshwakePickup pickup : this.pickups) {
            pickup.update(deltaSeconds);
        }

        this.projectiles.removeIf(projectile -> !projectile.isAlive());
        this.hazards.removeIf(hazard -> !hazard.isActive());
        this.pickups.removeIf(pickup -> !pickup.isActive());
        syncChildren();
    }

    public void addEnemy(AshwakeEnemy enemy) {
        this.enemies.add(enemy);
        addChildEntity(enemy);
    }

    public void addProjectile(AshwakeProjectile projectile) {
        this.projectiles.add(projectile);
        addChildEntity(projectile);
    }

    public void addHazard(AshwakeHazard hazard) {
        this.hazards.add(hazard);
        addChildEntity(hazard);
    }

    public void addPickup(AshwakePickup pickup) {
        this.pickups.add(pickup);
        addChildEntity(pickup);
    }

    public void removeEnemy(AshwakeEnemy enemy) {
        this.enemies.remove(enemy);
        removeChildEntity(enemy);
    }

    public void markEncounterCleared() {
        this.encounterCleared = true;
    }

    public boolean isEncounterCleared() {
        return this.encounterCleared;
    }

    public AshwakeRoomKind getRoomKind() {
        return this.roomKind;
    }

    public int getRoomIndex() {
        return this.roomIndex;
    }

    public int getWidth() {
        return ROOM_WIDTH;
    }

    public int getHeight() {
        return ROOM_HEIGHT;
    }

    public double getRoomTime() {
        return this.roomTime;
    }

    public List<AshwakeEnemy> getEnemies() {
        return Collections.unmodifiableList(this.enemies);
    }

    List<AshwakeEnemy> mutableEnemies() {
        return this.enemies;
    }

    public List<AshwakeProjectile> getProjectiles() {
        return Collections.unmodifiableList(this.projectiles);
    }

    List<AshwakeProjectile> mutableProjectiles() {
        return this.projectiles;
    }

    public List<AshwakeHazard> getHazards() {
        return Collections.unmodifiableList(this.hazards);
    }

    List<AshwakeHazard> mutableHazards() {
        return this.hazards;
    }

    public List<AshwakePickup> getPickups() {
        return Collections.unmodifiableList(this.pickups);
    }

    List<AshwakePickup> mutablePickups() {
        return this.pickups;
    }

    public void cullInactive() {
        removeEnemiesIfDead();
        this.projectiles.removeIf(projectile -> !projectile.isAlive());
        this.hazards.removeIf(hazard -> !hazard.isActive());
        this.pickups.removeIf(pickup -> !pickup.isActive());
        syncChildren();
    }

    private void removeEnemiesIfDead() {
        Iterator<AshwakeEnemy> iterator = this.enemies.iterator();
        while (iterator.hasNext()) {
            AshwakeEnemy enemy = iterator.next();
            if (!enemy.isAlive()) {
                iterator.remove();
                removeChildEntity(enemy);
            }
        }
    }

    private void syncChildren() {
        getChildren().removeIf(relation ->
            (relation instanceof AshwakeProjectile projectile && !projectile.isAlive())
                || (relation instanceof AshwakeHazard hazard && !hazard.isActive())
                || (relation instanceof AshwakePickup pickup && !pickup.isActive())
        );
    }

    private void updateRenderMetadata() {
        add("renderCategory", PrimaryTypeValue.string("room"));
        add("layerHint", PrimaryTypeValue.string("arena"));
        add("roomKind", PrimaryTypeValue.string(this.roomKind.name().toLowerCase()));
        add("visible", PrimaryTypeValue.bool(true));
        add("opacity", PrimaryTypeValue.doubleVal(1.0));
    }
}
