package ashwake;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import core.PrimaryTypeValue;
import core.Relation;
import core.Params;
import render.Matter;
import render.Planet;

public class AshwakeRunWorld extends Planet {

    public static final int RUN_ID = 9100;
    private static final double TICK_SECONDS = 1.0 / 60.0;

    private final Random random;
    private final AshwakePlayer player;
    private final AshwakeRoomWorld roomWorld;
    private final List<AshwakeRoomKind> roomSequence = List.of(
        AshwakeRoomKind.COMBAT,
        AshwakeRoomKind.REWARD,
        AshwakeRoomKind.EVENT,
        AshwakeRoomKind.ELITE,
        AshwakeRoomKind.BOSS
    );
    private final EnumSet<AshwakeModifier> activeModifiers = EnumSet.noneOf(AshwakeModifier.class);
    private final List<AshwakeModifier> offeredModifiers = new ArrayList<>();

    private int nextRuntimeId = 12000;
    private int currentRoomIndex = 0;
    private int kills = 0;
    private int essence = 0;
    private int roomsCleared = 0;
    private int score = 0;

    private boolean choosingModifier = false;
    private boolean awaitingRoomAdvance = false;
    private boolean runOver = false;
    private boolean victory = false;
    private double elapsedTime = 0.0;
    private double bannerTime = 0.0;
    private String statusLine = "";

    public AshwakeRunWorld() {
        this(new Random());
    }

    public AshwakeRunWorld(Random random) {
        super(RUN_ID, "ashwake-run");
        this.random = random;
        this.player = new AshwakePlayer();
        this.roomWorld = new AshwakeRoomWorld();
        addChildEntity(this.player);
        addChildEntity(this.roomWorld);
        initializeMatters();
        restartRun();
    }

    @Override
    public void execute() {
        if (!canExecutePlanet()) {
            return;
        }

        update(TICK_SECONDS);
    }

    public void update(double deltaSeconds) {
        this.elapsedTime += deltaSeconds;
        this.bannerTime = Math.max(0.0, this.bannerTime - deltaSeconds);

        if (this.runOver || this.victory) {
            this.roomWorld.update(deltaSeconds);
            return;
        }

        this.player.update(deltaSeconds, this.roomWorld);
        for (AshwakeEnemy enemy : new ArrayList<>(this.roomWorld.mutableEnemies())) {
            enemy.update(deltaSeconds, this);
        }
        this.roomWorld.update(deltaSeconds);

        resolveProjectileCollisions();
        resolveHazards(deltaSeconds);
        resolveEnemyContact(deltaSeconds);
        resolvePickups();
        applyWardAura(deltaSeconds);
        this.roomWorld.cullInactive();

        if (this.player.isDead()) {
            this.runOver = true;
            this.statusLine = "The ritual survivor fell. Press R to begin a new run.";
            this.bannerTime = 999.0;
            return;
        }

        if (this.roomWorld.getRoomKind().isCombatRoom()
            && !this.roomWorld.isEncounterCleared()
            && this.roomWorld.getEnemies().isEmpty()) {
            this.roomWorld.markEncounterCleared();
            this.roomsCleared++;
            onEncounterCleared();
        }

        if (this.roomWorld.getRoomKind() == AshwakeRoomKind.EVENT
            && this.roomWorld.getPickups().isEmpty()
            && this.roomWorld.getHazards().isEmpty()) {
            this.awaitingRoomAdvance = true;
            this.statusLine = "The shrine is quiet. Press E to move on.";
        }
    }

    public void restartRun() {
        this.activeModifiers.clear();
        this.offeredModifiers.clear();
        this.currentRoomIndex = 0;
        this.nextRuntimeId = 12000;
        this.kills = 0;
        this.essence = 0;
        this.roomsCleared = 0;
        this.score = 0;
        this.choosingModifier = false;
        this.awaitingRoomAdvance = false;
        this.runOver = false;
        this.victory = false;
        this.elapsedTime = 0.0;
        this.bannerTime = 2.0;
        this.statusLine = "Wake through the ash and clear the chambers.";
        this.player.resetForNewRun(AshwakeRoomWorld.ROOM_WIDTH * 0.5, AshwakeRoomWorld.ROOM_HEIGHT * 0.5);
        if (!isRunning()) {
            startPlanet();
        }
        enterRoom(0);
    }

    public void togglePause() {
        if (this.runOver || this.victory) {
            return;
        }
        if (isRunning()) {
            pausePlanet();
        } else if (isPaused()) {
            resumePlanet();
        }
    }

    public void setMoveIntent(double dx, double dy) {
        this.player.setMoveIntent(dx, dy);
    }

    public void firePrimary(double directionX, double directionY) {
        if (this.runOver || this.victory || this.choosingModifier || !this.player.canFire()) {
            return;
        }

        double[] normalized = normalize(directionX, directionY, this.player.getFacingX(), this.player.getFacingY());
        double baseDamage = this.player.rollCritical(this.random.nextDouble()) ? 20.0 : 12.0;
        spawnPlayerProjectile(this.player.getX(), this.player.getY(), normalized[0], normalized[1], baseDamage);

        int splitShots = this.player.getSplitShots();
        if (splitShots > 0) {
            double spreadBase = 0.18;
            for (int index = 0; index < splitShots; index++) {
                double sign = index % 2 == 0 ? -1.0 : 1.0;
                double angle = spreadBase * (1 + (index / 2)) * sign;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double rx = normalized[0] * cos - normalized[1] * sin;
                double ry = normalized[0] * sin + normalized[1] * cos;
                spawnPlayerProjectile(this.player.getX(), this.player.getY(), rx, ry, baseDamage * 0.72);
            }
        }

        this.player.consumeAttack();

        if (this.player.shouldTriggerChainburst()) {
            spawnProjectileSpread(
                this.player.getX(),
                this.player.getY(),
                normalized[0],
                normalized[1],
                8,
                Math.PI / 4.0,
                true,
                AshwakeProjectile.Kind.PULSE_SPARK
            );
        }
    }

    public void useSecondary() {
        if (this.runOver || this.victory || this.choosingModifier || !this.player.canUseSecondary()) {
            return;
        }

        this.player.consumeSecondary();
        for (int index = 0; index < 12; index++) {
            double angle = (Math.PI * 2.0 * index) / 12.0;
            spawnPlayerProjectile(
                this.player.getX(),
                this.player.getY(),
                Math.cos(angle),
                Math.sin(angle),
                10.0
            );
        }
        this.statusLine = "A pulse of ashfire erupts outward.";
        this.bannerTime = 0.9;
    }

    public void tryDash() {
        if (this.runOver || this.victory || this.choosingModifier) {
            return;
        }

        if (this.player.activateDash(this.player.getFacingX(), this.player.getFacingY()) && this.player.hasShockDash()) {
            spawnFriendlyHazard(this.player.getX(), this.player.getY(), 54.0, 1.15, 26.0);
        }
    }

    public void advanceRoomIfPossible() {
        if (!this.awaitingRoomAdvance || this.choosingModifier) {
            return;
        }

        if (this.currentRoomIndex >= this.roomSequence.size() - 1) {
            return;
        }

        enterRoom(this.currentRoomIndex + 1);
    }

    public void selectModifier(int optionIndex) {
        if (!this.choosingModifier || optionIndex < 0 || optionIndex >= this.offeredModifiers.size()) {
            return;
        }

        AshwakeModifier modifier = this.offeredModifiers.get(optionIndex);
        if (this.activeModifiers.add(modifier)) {
            modifier.apply(this.player);
            this.score += 45;
            this.statusLine = modifier.title() + " is now bound to the survivor.";
        }

        this.bannerTime = 1.4;
        this.offeredModifiers.clear();
        this.choosingModifier = false;
        this.awaitingRoomAdvance = true;
    }

    public void spawnEnemyProjectile(
        double x,
        double y,
        double directionX,
        double directionY,
        AshwakeProjectile.Kind kind,
        boolean empowered
    ) {
        double[] normalized = normalize(directionX, directionY, 1.0, 0.0);
        double speed = empowered ? 280.0 : 240.0;
        double damage = switch (kind) {
            case CURSE_ORB -> empowered ? 20.0 : 12.0;
            case SHADOW_NEEDLE -> 8.0;
            case BOSS_COMET -> 22.0;
            default -> 10.0;
        };
        double lifetime = kind == AshwakeProjectile.Kind.BOSS_COMET ? 4.0 : 3.2;
        this.roomWorld.addProjectile(new AshwakeProjectile(
            nextId(),
            kind,
            false,
            x,
            y,
            normalized[0] * speed,
            normalized[1] * speed,
            damage,
            lifetime,
            0
        ));
    }

    public void spawnProjectileSpread(
        double x,
        double y,
        double directionX,
        double directionY,
        int count,
        double angleStep,
        boolean friendly,
        AshwakeProjectile.Kind kind
    ) {
        if (count <= 0) {
            return;
        }

        double[] base = normalize(directionX, directionY, 1.0, 0.0);
        double start = -angleStep * (count - 1) * 0.5;
        for (int index = 0; index < count; index++) {
            double angle = start + (angleStep * index);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double rx = base[0] * cos - base[1] * sin;
            double ry = base[0] * sin + base[1] * cos;
            if (friendly) {
                spawnPlayerProjectile(x, y, rx, ry, 8.0);
            } else {
                spawnEnemyProjectile(x, y, rx, ry, kind, kind == AshwakeProjectile.Kind.BOSS_COMET);
            }
        }
    }

    public void spawnEnemyHazard(double x, double y, double radius, double lifetime, double damagePerSecond) {
        this.roomWorld.addHazard(new AshwakeHazard(nextId(), false, x, y, radius, lifetime, damagePerSecond));
    }

    public void spawnFriendlyHazard(double x, double y, double radius, double lifetime, double damagePerSecond) {
        this.roomWorld.addHazard(new AshwakeHazard(nextId(), true, x, y, radius, lifetime, damagePerSecond));
    }

    public void spawnMinion(double x, double y) {
        if (this.roomWorld.getEnemies().size() >= 24) {
            return;
        }
        AshwakeEnemy minion = new AshwakeEnemy(nextId(), AshwakeEnemy.Kind.CHASER, x, y, false);
        this.roomWorld.addEnemy(minion);
    }

    public void damagePlayer(double damage) {
        if (this.player.takeDamage(damage)) {
            this.statusLine = "The ash bites back.";
            this.bannerTime = 0.6;
        }
    }

    public double distanceToPlayer(double x, double y) {
        return Math.hypot(this.player.getX() - x, this.player.getY() - y);
    }

    public AshwakePlayer getPlayer() {
        return this.player;
    }

    public AshwakeRoomWorld getRoomWorld() {
        return this.roomWorld;
    }

    public List<AshwakeModifier> getOfferedModifiers() {
        return List.copyOf(this.offeredModifiers);
    }

    public EnumSet<AshwakeModifier> getActiveModifiers() {
        return this.activeModifiers.clone();
    }

    public int getCurrentRoomNumber() {
        return this.currentRoomIndex + 1;
    }

    public int getTotalRooms() {
        return this.roomSequence.size();
    }

    public int getKills() {
        return this.kills;
    }

    public int getEssence() {
        return this.essence;
    }

    public int getRoomsCleared() {
        return this.roomsCleared;
    }

    public int getScore() {
        return this.score;
    }

    public boolean isChoosingModifier() {
        return this.choosingModifier;
    }

    public boolean isAwaitingRoomAdvance() {
        return this.awaitingRoomAdvance;
    }

    public boolean isRunOver() {
        return this.runOver;
    }

    public boolean isVictory() {
        return this.victory;
    }

    public double getElapsedTime() {
        return this.elapsedTime;
    }

    public double getBannerTime() {
        return this.bannerTime;
    }

    public String getStatusLine() {
        return this.statusLine;
    }

    public String getRoomTitle() {
        return this.roomSequence.get(this.currentRoomIndex).title();
    }

    public String getRoomDescription() {
        return this.roomSequence.get(this.currentRoomIndex).description();
    }

    void prepareBenchmarkScenario(AshwakeBenchmarkScenario scenario) {
        this.activeModifiers.clear();
        this.offeredModifiers.clear();
        this.nextRuntimeId = 22000;
        this.kills = 0;
        this.essence = 0;
        this.roomsCleared = 0;
        this.score = 0;
        this.choosingModifier = false;
        this.awaitingRoomAdvance = false;
        this.runOver = false;
        this.victory = false;
        this.elapsedTime = 0.0;
        this.bannerTime = 999.0;
        this.statusLine = "Benchmark scenario: " + scenario.title();

        if (!isRunning()) {
            startPlanet();
        } else {
            resumePlanet();
        }

        switch (scenario) {
            case PROJECTILE_HELL -> setupProjectileHellBenchmark();
            case BOSS_ARENA -> setupBossArenaBenchmark();
            case REWARD_CHAMBER -> setupRewardChamberBenchmark();
        }
    }

    void sustainBenchmarkScenario(AshwakeBenchmarkScenario scenario, int frame) {
        switch (scenario) {
            case PROJECTILE_HELL -> sustainProjectileHell(frame);
            case BOSS_ARENA -> sustainBossArena(frame);
            case REWARD_CHAMBER -> sustainRewardChamber(frame);
        }
    }

    private void initializeMatters() {
        addMatter(createMatter(30001, "ashwake-backdrop", "backdrop", "void-sky", "gradient", 0));
        addMatter(createMatter(30002, "ashwake-arena", "arena", "ritual-floor", "shape", 10));
        addMatter(createMatter(30003, "ashwake-hazards", "hazards", "pulse-field", "shape", 20));
        addMatter(createMatter(30004, "ashwake-pickups", "pickups", "ritual-loot", "shape", 30));
        addMatter(createMatter(30005, "ashwake-actors", "actors", "entity-pass", "shape", 40));
        addMatter(createMatter(30006, "ashwake-projectiles", "projectiles", "ember-shot", "shape", 50));
        addMatter(createMatter(30007, "ashwake-overlay", "overlay", "screen-space", "ui", 60));
        addMatter(createMatter(30008, "ashwake-hud", "hud", "screen-space", "ui", 70));
    }

    private Matter createMatter(int id, String label, String layerHint, String materialKey, String primitiveType, int sortBias) {
        Matter matter = new Matter(id, label);
        matter.addRenderParam("layerHint", Params.of("value", layerHint));
        matter.addRenderParam("materialKey", Params.of("value", materialKey));
        matter.addRenderParam("primitiveType", Params.of("value", primitiveType));
        matter.addRenderParam("sortBias", Params.of("value", sortBias));
        matter.addRenderParam("visible", Params.of("value", true));
        return matter;
    }

    private void setupProjectileHellBenchmark() {
        configureBenchmarkRoom(AshwakeRoomKind.COMBAT, 0, 480.0, 520.0);
        grantBenchmarkModifier(AshwakeModifier.SPLIT_SHOT);
        grantBenchmarkModifier(AshwakeModifier.PIERCING_SIGIL);
        grantBenchmarkModifier(AshwakeModifier.WARD_AURA);
        grantBenchmarkModifier(AshwakeModifier.RITUAL_VELOCITY);
        grantBenchmarkModifier(AshwakeModifier.CHAINBURST);
        grantBenchmarkModifier(AshwakeModifier.SHOCK_DASH);

        for (int index = 0; index < 18; index++) {
            this.roomWorld.addEnemy(new AshwakeEnemy(
                nextId(),
                switch (index % 5) {
                    case 0 -> AshwakeEnemy.Kind.CHASER;
                    case 1 -> AshwakeEnemy.Kind.CASTER;
                    case 2 -> AshwakeEnemy.Kind.DASH_STRIKER;
                    case 3 -> AshwakeEnemy.Kind.AREA_SEEDER;
                    default -> AshwakeEnemy.Kind.SUMMONER;
                },
                randomRange(96.0, AshwakeRoomWorld.ROOM_WIDTH - 96.0),
                randomRange(82.0, 280.0),
                index > 11
            ));
        }

        for (int index = 0; index < 10; index++) {
            spawnEnemyHazard(
                90.0 + (index % 5) * 180.0,
                170.0 + (index / 5) * 220.0,
                34.0 + (index % 3) * 8.0,
                8.0,
                10.0 + (index % 2) * 3.0
            );
        }

        for (int index = 0; index < 6; index++) {
            this.roomWorld.addPickup(new AshwakePickup(
                nextId(),
                index % 3 == 0 ? AshwakePickup.Type.HEALTH_ORB : index % 3 == 1 ? AshwakePickup.Type.EMBER_SHARD : AshwakePickup.Type.ENERGY_BLOOM,
                180.0 + index * 100.0,
                430.0 + (index % 2) * 44.0,
                18.0 + index
            ));
        }

        for (int index = 0; index < 60; index++) {
            double angle = (Math.PI * 2.0 * index) / 60.0;
            spawnEnemyProjectile(
                480.0 + Math.cos(angle) * 120.0,
                240.0 + Math.sin(angle) * 90.0,
                Math.cos(angle),
                Math.sin(angle),
                index % 4 == 0 ? AshwakeProjectile.Kind.BOSS_COMET : AshwakeProjectile.Kind.CURSE_ORB,
                index % 5 == 0
            );
        }
    }

    private void setupBossArenaBenchmark() {
        configureBenchmarkRoom(AshwakeRoomKind.BOSS, 4, 480.0, 554.0);
        grantBenchmarkModifier(AshwakeModifier.WARD_AURA);
        grantBenchmarkModifier(AshwakeModifier.RITUAL_VELOCITY);
        grantBenchmarkModifier(AshwakeModifier.CRITICAL_ASH);
        grantBenchmarkModifier(AshwakeModifier.SHOCK_DASH);

        this.roomWorld.addEnemy(new AshwakeEnemy(nextId(), AshwakeEnemy.Kind.BOSS, 480.0, 154.0, false));
        for (int index = 0; index < 6; index++) {
            this.roomWorld.addEnemy(new AshwakeEnemy(
                nextId(),
                index % 2 == 0 ? AshwakeEnemy.Kind.CASTER : AshwakeEnemy.Kind.DASH_STRIKER,
                150.0 + (index * 120.0),
                220.0 + (index % 2) * 80.0,
                true
            ));
        }

        for (int index = 0; index < 6; index++) {
            spawnEnemyHazard(
                150.0 + (index * 120.0),
                420.0 + ((index + 1) % 2) * 42.0,
                42.0 + (index % 2) * 16.0,
                10.0,
                15.0
            );
        }

        for (int index = 0; index < 24; index++) {
            double angle = (Math.PI * 2.0 * index) / 24.0;
            spawnEnemyProjectile(480.0, 180.0, Math.cos(angle), Math.sin(angle), AshwakeProjectile.Kind.BOSS_COMET, true);
        }
    }

    private void setupRewardChamberBenchmark() {
        configureBenchmarkRoom(AshwakeRoomKind.REWARD, 1, 480.0, 520.0);
        this.choosingModifier = true;
        rollModifierOptions();

        for (int index = 0; index < 12; index++) {
            this.roomWorld.addPickup(new AshwakePickup(
                nextId(),
                index % 3 == 0 ? AshwakePickup.Type.HEALTH_ORB : index % 3 == 1 ? AshwakePickup.Type.EMBER_SHARD : AshwakePickup.Type.ENERGY_BLOOM,
                150.0 + (index % 4) * 180.0,
                210.0 + (index / 4) * 120.0,
                18.0 + index
            ));
        }

        for (int index = 0; index < 4; index++) {
            spawnFriendlyHazard(200.0 + index * 180.0, 360.0, 36.0 + index * 8.0, 8.0, 12.0);
        }
    }

    private void sustainProjectileHell(int frame) {
        while (this.roomWorld.getEnemies().size() < 18) {
            int seed = this.roomWorld.getEnemies().size();
            this.roomWorld.addEnemy(new AshwakeEnemy(
                nextId(),
                switch (seed % 5) {
                    case 0 -> AshwakeEnemy.Kind.CHASER;
                    case 1 -> AshwakeEnemy.Kind.CASTER;
                    case 2 -> AshwakeEnemy.Kind.DASH_STRIKER;
                    case 3 -> AshwakeEnemy.Kind.AREA_SEEDER;
                    default -> AshwakeEnemy.Kind.SUMMONER;
                },
                randomRange(86.0, AshwakeRoomWorld.ROOM_WIDTH - 86.0),
                randomRange(72.0, 260.0),
                seed > 10
            ));
        }

        if (this.roomWorld.getHazards().size() < 10 && frame % 15 == 0) {
            spawnEnemyHazard(
                randomRange(100.0, AshwakeRoomWorld.ROOM_WIDTH - 100.0),
                randomRange(120.0, AshwakeRoomWorld.ROOM_HEIGHT - 140.0),
                randomRange(34.0, 56.0),
                6.5,
                10.0
            );
        }

        if (this.roomWorld.getProjectiles().size() < 60 && frame % 3 == 0) {
            for (int index = 0; index < 8; index++) {
                double angle = ((frame * 0.11) + ((Math.PI * 2.0 * index) / 8.0));
                spawnEnemyProjectile(
                    480.0 + Math.cos(angle * 0.67) * 140.0,
                    200.0 + Math.sin(angle * 0.91) * 88.0,
                    Math.cos(angle),
                    Math.sin(angle),
                    index % 3 == 0 ? AshwakeProjectile.Kind.BOSS_COMET : AshwakeProjectile.Kind.CURSE_ORB,
                    index % 4 == 0
                );
            }
        }
    }

    private void sustainBossArena(int frame) {
        boolean bossAlive = false;
        for (AshwakeEnemy enemy : this.roomWorld.getEnemies()) {
            if (enemy.getKind() == AshwakeEnemy.Kind.BOSS && enemy.isAlive()) {
                bossAlive = true;
                break;
            }
        }

        if (!bossAlive) {
            this.roomWorld.addEnemy(new AshwakeEnemy(nextId(), AshwakeEnemy.Kind.BOSS, 480.0, 154.0, false));
        }

        while (this.roomWorld.getEnemies().size() < 7) {
            int seed = this.roomWorld.getEnemies().size();
            this.roomWorld.addEnemy(new AshwakeEnemy(
                nextId(),
                seed % 2 == 0 ? AshwakeEnemy.Kind.CASTER : AshwakeEnemy.Kind.DASH_STRIKER,
                randomRange(130.0, AshwakeRoomWorld.ROOM_WIDTH - 130.0),
                randomRange(180.0, 320.0),
                true
            ));
        }

        if (this.roomWorld.getProjectiles().size() < 42 && frame % 5 == 0) {
            spawnProjectileSpread(
                480.0,
                180.0,
                Math.cos(frame * 0.07),
                Math.sin(frame * 0.07),
                10,
                0.24,
                false,
                AshwakeProjectile.Kind.BOSS_COMET
            );
        }

        if (this.roomWorld.getHazards().size() < 6 && frame % 20 == 0) {
            spawnEnemyHazard(
                randomRange(120.0, AshwakeRoomWorld.ROOM_WIDTH - 120.0),
                randomRange(180.0, AshwakeRoomWorld.ROOM_HEIGHT - 120.0),
                randomRange(44.0, 72.0),
                7.5,
                16.0
            );
        }
    }

    private void sustainRewardChamber(int frame) {
        while (this.roomWorld.getPickups().size() < 10) {
            int seed = this.roomWorld.getPickups().size();
            this.roomWorld.addPickup(new AshwakePickup(
                nextId(),
                seed % 3 == 0 ? AshwakePickup.Type.HEALTH_ORB : seed % 3 == 1 ? AshwakePickup.Type.EMBER_SHARD : AshwakePickup.Type.ENERGY_BLOOM,
                randomRange(160.0, AshwakeRoomWorld.ROOM_WIDTH - 160.0),
                randomRange(160.0, AshwakeRoomWorld.ROOM_HEIGHT - 160.0),
                18.0 + seed
            ));
        }

        if (this.roomWorld.getHazards().size() < 4 && frame % 24 == 0) {
            spawnFriendlyHazard(
                randomRange(180.0, AshwakeRoomWorld.ROOM_WIDTH - 180.0),
                randomRange(180.0, AshwakeRoomWorld.ROOM_HEIGHT - 180.0),
                randomRange(32.0, 52.0),
                6.0,
                8.0
            );
        }
    }

    private void configureBenchmarkRoom(AshwakeRoomKind roomKind, int roomIndex, double playerX, double playerY) {
        this.currentRoomIndex = roomIndex;
        this.roomWorld.reset(roomIndex, roomKind);
        this.roomWorld.markEncounterCleared();
        this.player.resetForNewRun(playerX, playerY);
        this.player.relocate(playerX, playerY);
        this.statusLine = "Benchmark scenario: " + roomKind.title();
    }

    private void grantBenchmarkModifier(AshwakeModifier modifier) {
        if (this.activeModifiers.add(modifier)) {
            modifier.apply(this.player);
        }
    }

    private void enterRoom(int roomIndex) {
        this.currentRoomIndex = roomIndex;
        this.awaitingRoomAdvance = false;
        this.choosingModifier = false;
        this.offeredModifiers.clear();
        AshwakeRoomKind roomKind = this.roomSequence.get(roomIndex);
        this.roomWorld.reset(roomIndex, roomKind);
        this.player.setMoveIntent(0.0, 0.0);

        double spawnX = AshwakeRoomWorld.ROOM_WIDTH * 0.5;
        double spawnY = AshwakeRoomWorld.ROOM_HEIGHT * 0.5;
        if (roomKind == AshwakeRoomKind.ELITE || roomKind == AshwakeRoomKind.BOSS) {
            spawnY = AshwakeRoomWorld.ROOM_HEIGHT * 0.76;
        }
        this.player.relocate(spawnX, spawnY);

        switch (roomKind) {
            case COMBAT -> populateCombatRoom();
            case REWARD -> prepareRewardRoom();
            case EVENT -> prepareEventRoom();
            case ELITE -> populateEliteRoom();
            case BOSS -> populateBossRoom();
        }

        this.statusLine = roomKind.title() + ": " + roomKind.description();
        this.bannerTime = 2.4;
    }

    private void populateCombatRoom() {
        int enemyCount = 9 + (this.currentRoomIndex * 2);
        for (int index = 0; index < enemyCount; index++) {
            this.roomWorld.addEnemy(new AshwakeEnemy(
                nextId(),
                rollCombatEnemy(index),
                randomRange(86.0, AshwakeRoomWorld.ROOM_WIDTH - 86.0),
                randomRange(82.0, AshwakeRoomWorld.ROOM_HEIGHT - 160.0),
                false
            ));
        }
        spawnEnemyHazard(AshwakeRoomWorld.ROOM_WIDTH * 0.5, 126.0, 42.0, 5.5, 9.0);
    }

    private void prepareRewardRoom() {
        this.choosingModifier = true;
        rollModifierOptions();
        this.roomWorld.markEncounterCleared();
    }

    private void prepareEventRoom() {
        this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.HEALTH_ORB, 410.0, 246.0, 24.0));
        this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.ENERGY_BLOOM, 550.0, 246.0, 34.0));
        this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.EMBER_SHARD, 480.0, 372.0, 20.0));
        spawnEnemyHazard(480.0, 318.0, 58.0, 6.0, 8.0);
    }

    private void populateEliteRoom() {
        this.roomWorld.addEnemy(new AshwakeEnemy(nextId(), AshwakeEnemy.Kind.SUMMONER, 480.0, 146.0, true));
        for (int index = 0; index < 4; index++) {
            this.roomWorld.addEnemy(new AshwakeEnemy(
                nextId(),
                index % 2 == 0 ? AshwakeEnemy.Kind.DASH_STRIKER : AshwakeEnemy.Kind.CASTER,
                220.0 + (index * 150.0),
                190.0 + ((index % 2) * 96.0),
                true
            ));
        }
        spawnEnemyHazard(244.0, 438.0, 44.0, 8.0, 11.0);
        spawnEnemyHazard(714.0, 438.0, 44.0, 8.0, 11.0);
    }

    private void populateBossRoom() {
        this.roomWorld.addEnemy(new AshwakeEnemy(nextId(), AshwakeEnemy.Kind.BOSS, 480.0, 152.0, false));
        this.roomWorld.addEnemy(new AshwakeEnemy(nextId(), AshwakeEnemy.Kind.CASTER, 260.0, 214.0, true));
        this.roomWorld.addEnemy(new AshwakeEnemy(nextId(), AshwakeEnemy.Kind.AREA_SEEDER, 700.0, 214.0, true));
    }

    private void onEncounterCleared() {
        AshwakeRoomKind roomKind = this.roomWorld.getRoomKind();
        if (roomKind == AshwakeRoomKind.BOSS) {
            this.victory = true;
            this.statusLine = "The Cinder Heart collapses. The run is yours.";
            this.bannerTime = 999.0;
            this.score += 600;
            return;
        }

        this.awaitingRoomAdvance = true;
        this.statusLine = "Chamber cleared. Press E to step into the next room.";
        this.bannerTime = 2.2;
        this.score += 120;
        this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.EMBER_SHARD, 430.0, 304.0, 16.0));
        this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.EMBER_SHARD, 530.0, 324.0, 16.0));
        if (this.player.getHealth() < this.player.getMaxHealth() * 0.65) {
            this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.HEALTH_ORB, 480.0, 280.0, 18.0));
        }
    }

    private void resolveProjectileCollisions() {
        for (AshwakeProjectile projectile : new ArrayList<>(this.roomWorld.mutableProjectiles())) {
            if (!projectile.isAlive()) {
                continue;
            }

            if (projectile.isFriendly()) {
                for (AshwakeEnemy enemy : new ArrayList<>(this.roomWorld.mutableEnemies())) {
                    if (!enemy.isAlive()) {
                        continue;
                    }
                    if (circlesOverlap(projectile.getX(), projectile.getY(), projectile.getRadius(), enemy.getX(), enemy.getY(), enemy.getRadius())) {
                        boolean defeated = enemy.takeDamage(projectile.getDamage());
                        projectile.onHit();
                        if (defeated) {
                            onEnemyDefeated(enemy);
                        }
                        if (!projectile.isAlive()) {
                            break;
                        }
                    }
                }
            } else if (circlesOverlap(
                projectile.getX(),
                projectile.getY(),
                projectile.getRadius(),
                this.player.getX(),
                this.player.getY(),
                this.player.getRadius()
            )) {
                damagePlayer(projectile.getDamage());
                projectile.onHit();
            }
        }
    }

    private void resolveHazards(double deltaSeconds) {
        for (AshwakeHazard hazard : new ArrayList<>(this.roomWorld.mutableHazards())) {
            if (!hazard.isActive()) {
                continue;
            }

            if (hazard.isFriendly()) {
                for (AshwakeEnemy enemy : new ArrayList<>(this.roomWorld.mutableEnemies())) {
                    if (enemy.isAlive()
                        && circlesOverlap(hazard.getX(), hazard.getY(), hazard.getRadius(), enemy.getX(), enemy.getY(), enemy.getRadius())) {
                        boolean defeated = enemy.takeDamage(hazard.getDamagePerSecond() * deltaSeconds);
                        if (defeated) {
                            onEnemyDefeated(enemy);
                        }
                    }
                }
            } else if (circlesOverlap(
                hazard.getX(),
                hazard.getY(),
                hazard.getRadius(),
                this.player.getX(),
                this.player.getY(),
                this.player.getRadius()
            )) {
                damagePlayer(hazard.getDamagePerSecond() * deltaSeconds);
            }
        }
    }

    private void resolveEnemyContact(double deltaSeconds) {
        for (AshwakeEnemy enemy : new ArrayList<>(this.roomWorld.mutableEnemies())) {
            if (!enemy.isAlive()) {
                continue;
            }

            if (circlesOverlap(enemy.getX(), enemy.getY(), enemy.getRadius(), this.player.getX(), this.player.getY(), this.player.getRadius())) {
                damagePlayer(enemy.getContactDamage() * 0.45 * deltaSeconds * 4.0);
                double pushX = enemy.getX() - this.player.getX();
                double pushY = enemy.getY() - this.player.getY();
                double[] norm = normalize(pushX, pushY, 1.0, 0.0);
                enemy.addImpulse(norm[0] * 36.0, norm[1] * 36.0);
            }
        }
    }

    private void resolvePickups() {
        for (AshwakePickup pickup : new ArrayList<>(this.roomWorld.mutablePickups())) {
            if (!pickup.isActive()) {
                continue;
            }

            if (circlesOverlap(pickup.getX(), pickup.getY() + pickup.getBobOffset(), pickup.getRadius(), this.player.getX(), this.player.getY(), this.player.getRadius())) {
                switch (pickup.getType()) {
                    case HEALTH_ORB -> this.player.heal(pickup.getValue());
                    case EMBER_SHARD -> {
                        this.essence += (int) pickup.getValue();
                        this.score += (int) pickup.getValue() * 3;
                    }
                    case ENERGY_BLOOM -> this.player.restoreEnergy(pickup.getValue());
                }
                pickup.collect();
            }
        }
    }

    private void applyWardAura(double deltaSeconds) {
        if (!this.player.hasWardAura()) {
            return;
        }

        for (AshwakeEnemy enemy : new ArrayList<>(this.roomWorld.mutableEnemies())) {
            if (!enemy.isAlive()) {
                continue;
            }
            if (distance(this.player.getX(), this.player.getY(), enemy.getX(), enemy.getY()) <= this.player.getAuraRadius()) {
                boolean defeated = enemy.takeDamage(this.player.getAuraDamagePerSecond() * deltaSeconds);
                if (defeated) {
                    onEnemyDefeated(enemy);
                }
            }
        }
    }

    private void onEnemyDefeated(AshwakeEnemy enemy) {
        this.kills++;
        this.score += enemy.getKind() == AshwakeEnemy.Kind.BOSS ? 500 : 35;
        this.roomWorld.removeEnemy(enemy);

        double shardValue = this.player.hasEmberBloom() ? 14.0 : 8.0;
        if (enemy.getKind() == AshwakeEnemy.Kind.BOSS) {
            this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.EMBER_SHARD, enemy.getX(), enemy.getY(), 48.0));
            this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.HEALTH_ORB, enemy.getX() - 24.0, enemy.getY() + 16.0, 24.0));
            return;
        }

        if (this.random.nextDouble() < 0.58) {
            this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.EMBER_SHARD, enemy.getX(), enemy.getY(), shardValue));
        }
        if (this.random.nextDouble() < 0.14) {
            this.roomWorld.addPickup(new AshwakePickup(nextId(), AshwakePickup.Type.ENERGY_BLOOM, enemy.getX(), enemy.getY(), 18.0));
        }
    }

    private void rollModifierOptions() {
        List<AshwakeModifier> pool = new ArrayList<>();
        for (AshwakeModifier modifier : AshwakeModifier.all()) {
            if (!this.activeModifiers.contains(modifier)) {
                pool.add(modifier);
            }
        }

        while (!pool.isEmpty() && this.offeredModifiers.size() < 3) {
            int pick = this.random.nextInt(pool.size());
            this.offeredModifiers.add(pool.remove(pick));
        }
    }

    private void spawnPlayerProjectile(double x, double y, double directionX, double directionY, double damage) {
        double[] normalized = normalize(directionX, directionY, this.player.getFacingX(), this.player.getFacingY());
        this.roomWorld.addProjectile(new AshwakeProjectile(
            nextId(),
            AshwakeProjectile.Kind.EMBER_BOLT,
            true,
            x,
            y,
            normalized[0] * this.player.getProjectileSpeed(),
            normalized[1] * this.player.getProjectileSpeed(),
            damage,
            2.4,
            this.player.getProjectilePierce()
        ));
    }

    private AshwakeEnemy.Kind rollCombatEnemy(int spawnIndex) {
        double roll = this.random.nextDouble();
        if (spawnIndex > 6 && roll < 0.12) {
            return AshwakeEnemy.Kind.AREA_SEEDER;
        }
        if (roll < 0.34) {
            return AshwakeEnemy.Kind.CHASER;
        }
        if (roll < 0.60) {
            return AshwakeEnemy.Kind.CASTER;
        }
        if (roll < 0.82) {
            return AshwakeEnemy.Kind.DASH_STRIKER;
        }
        return AshwakeEnemy.Kind.AREA_SEEDER;
    }

    private int nextId() {
        return this.nextRuntimeId++;
    }

    private double randomRange(double min, double max) {
        return min + (this.random.nextDouble() * (max - min));
    }

    private static boolean circlesOverlap(double ax, double ay, double ar, double bx, double by, double br) {
        return distance(ax, ay, bx, by) <= ar + br;
    }

    private static double distance(double ax, double ay, double bx, double by) {
        return Math.hypot(ax - bx, ay - by);
    }

    private static double[] normalize(double x, double y, double fallbackX, double fallbackY) {
        double length = Math.hypot(x, y);
        if (length == 0.0) {
            length = Math.hypot(fallbackX, fallbackY);
            if (length == 0.0) {
                return new double[] {1.0, 0.0};
            }
            return new double[] {fallbackX / length, fallbackY / length};
        }
        return new double[] {x / length, y / length};
    }
}
