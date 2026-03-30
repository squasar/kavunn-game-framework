package com.example;

import java.awt.Point;
import java.util.Random;

import com.example.core.Entity;
import com.example.core.Relation;

public class SnakeWorld extends Entity<Relation> {

    public static final int WORLD_ID = 100;
    public static final int SNAKE_ID = 101;
    public static final int FOOD_ID = 102;

    private final int width;
    private final int height;
    private final Random random;
    private final Snake snake;
    private final Food food;

    private int score = 0;
    private boolean gameOver = false;
    private boolean victory = false;

    public SnakeWorld(int width, int height) {
        this(width, height, new Random());
    }

    public SnakeWorld(int width, int height, Random random) {
        super(WORLD_ID, "snake-world");
        this.width = width;
        this.height = height;
        this.random = random;
        this.snake = new Snake(SNAKE_ID, "snake", width / 2, height / 2);
        this.food = new Food(FOOD_ID, "food");
        addChildEntity(snake);
        addChildEntity(food);
        restart();
    }

    public void restart() {
        score = 0;
        gameOver = false;
        victory = false;
        snake.reset(width / 2, height / 2);
        spawnFood();
    }

    @Override
    public void execute() {
        step();
    }

    public void step() {
        if (gameOver) {
            return;
        }

        Point nextHead = snake.nextHead();
        boolean eating = nextHead.equals(food.getPosition());

        if (!isInsideBoard(nextHead) || snake.hitsSelf(nextHead, eating)) {
            gameOver = true;
            return;
        }

        snake.advance(nextHead, eating);

        if (eating) {
            score++;
            spawnFood();
        }
    }

    private void spawnFood() {
        if (snake.getLength() >= width * height) {
            victory = true;
            gameOver = true;
            return;
        }

        Point nextPoint;
        do {
            nextPoint = new Point(random.nextInt(width), random.nextInt(height));
        } while (snake.occupies(nextPoint));

        food.relocate(nextPoint);
    }

    private boolean isInsideBoard(Point point) {
        return point.x >= 0 && point.x < width && point.y >= 0 && point.y < height;
    }

    public Snake getSnake() {
        return snake;
    }

    public Food getFood() {
        return food;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isVictory() {
        return victory;
    }

    public int getBoardWidth() {
        return width;
    }

    public int getBoardHeight() {
        return height;
    }
}
