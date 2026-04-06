import core.Relation;

public class DirectionCommand implements Relation {

    private final Snake snake;
    private final Direction direction;

    public DirectionCommand(Snake snake, Direction direction) {
        this.snake = snake;
        this.direction = direction;
    }

    @Override
    public void execute() {
        snake.queueDirection(direction);
    }
}
