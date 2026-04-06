package ashwake;

import core.Relation;

final class AshwakeMoveCommand implements Relation {

    private final AshwakeRunWorld world;
    private final double dx;
    private final double dy;

    AshwakeMoveCommand(AshwakeRunWorld world, double dx, double dy) {
        this.world = world;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        this.world.setMoveIntent(this.dx, this.dy);
    }
}

final class AshwakeFireCommand implements Relation {

    private final AshwakeRunWorld world;
    private final double aimX;
    private final double aimY;

    AshwakeFireCommand(AshwakeRunWorld world, double aimX, double aimY) {
        this.world = world;
        this.aimX = aimX;
        this.aimY = aimY;
    }

    @Override
    public void execute() {
        this.world.firePrimary(this.aimX, this.aimY);
    }
}

final class AshwakeDashCommand implements Relation {

    private final AshwakeRunWorld world;

    AshwakeDashCommand(AshwakeRunWorld world) {
        this.world = world;
    }

    @Override
    public void execute() {
        this.world.tryDash();
    }
}

final class AshwakeSecondaryCommand implements Relation {

    private final AshwakeRunWorld world;

    AshwakeSecondaryCommand(AshwakeRunWorld world) {
        this.world = world;
    }

    @Override
    public void execute() {
        this.world.useSecondary();
    }
}

final class AshwakeTogglePauseCommand implements Relation {

    private final AshwakeRunWorld world;

    AshwakeTogglePauseCommand(AshwakeRunWorld world) {
        this.world = world;
    }

    @Override
    public void execute() {
        this.world.togglePause();
    }
}

final class AshwakeRestartCommand implements Relation {

    private final AshwakeRunWorld world;

    AshwakeRestartCommand(AshwakeRunWorld world) {
        this.world = world;
    }

    @Override
    public void execute() {
        this.world.restartRun();
    }
}

final class AshwakeAdvanceRoomCommand implements Relation {

    private final AshwakeRunWorld world;

    AshwakeAdvanceRoomCommand(AshwakeRunWorld world) {
        this.world = world;
    }

    @Override
    public void execute() {
        this.world.advanceRoomIfPossible();
    }
}

final class AshwakeSelectModifierCommand implements Relation {

    private final AshwakeRunWorld world;
    private final int index;

    AshwakeSelectModifierCommand(AshwakeRunWorld world, int index) {
        this.world = world;
        this.index = index;
    }

    @Override
    public void execute() {
        this.world.selectModifier(this.index);
    }
}
