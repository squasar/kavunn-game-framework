import core.Relation;

public class RestartSnakeCommand implements Relation {

    private final SnakeWorld world;

    public RestartSnakeCommand(SnakeWorld world) {
        this.world = world;
    }

    @Override
    public void execute() {
        world.restart();
    }
}
