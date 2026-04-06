import core.Relation;

public class TetrisActionCommand implements Relation {

    private final TetrisWorld world;
    private final TetrisAction action;

    public TetrisActionCommand(TetrisWorld world, TetrisAction action) {
        this.world = world;
        this.action = action;
    }

    @Override
    public void execute() {
        switch (action) {
            case MOVE_LEFT -> world.moveHorizontal(-1);
            case MOVE_RIGHT -> world.moveHorizontal(1);
            case ROTATE_CW -> world.rotateClockwise();
            case ROTATE_CCW -> world.rotateCounterClockwise();
            case SOFT_DROP -> world.softDrop();
            case HARD_DROP -> world.hardDrop();
            case TOGGLE_PAUSE -> world.togglePause();
            case RESTART -> world.restart();
        }
    }
}
