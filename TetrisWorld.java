<<<<<<< HEAD
package com.example;

=======
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

<<<<<<< HEAD
import com.example.core.Entity;
import com.example.core.Relation;
=======
import core.Entity;
import core.Relation;
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)

public class TetrisWorld extends Entity<Relation> {

    public static final int WORLD_ID = 200;
    public static final int BOARD_ID = 201;
    public static final int ACTIVE_PIECE_ID = 202;

    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static final int SPAWN_ROW = 0;
    private static final int SPAWN_COL = 3;

    private final Random random;
    private final List<TetrominoType> bag = new ArrayList<>();
    private final TetrisBoard board;
    private final FallingPiece activePiece;

    private TetrominoType nextType = TetrominoType.I;
    private int score = 0;
    private int linesCleared = 0;
    private int level = 1;
    private boolean paused = false;
    private boolean gameOver = false;

    public TetrisWorld() {
        this(new Random());
    }

    public TetrisWorld(Random random) {
        super(WORLD_ID, "blockfall-world");
        this.random = random;
        this.board = new TetrisBoard(BOARD_ID, "blockfall-board", ROWS, COLS);
        this.activePiece = new FallingPiece(ACTIVE_PIECE_ID, "active-piece");
        addChildEntity(board);
        addChildEntity(activePiece);
        restart();
    }

    @Override
    public void execute() {
        tick();
    }

    public void restart() {
        board.clear();
        bag.clear();
        score = 0;
        linesCleared = 0;
        level = 1;
        paused = false;
        gameOver = false;
        nextType = drawFromBag();
        spawnPiece();
    }

    public void tick() {
        if (paused || gameOver) {
            return;
        }
        if (!tryMove(0, 1)) {
            lockPiece();
        }
    }

    public void moveHorizontal(int dx) {
        if (paused || gameOver) {
            return;
        }
        tryMove(dx, 0);
    }

    public void softDrop() {
        if (paused || gameOver) {
            return;
        }
        if (tryMove(0, 1)) {
            score += 1;
        } else {
            lockPiece();
        }
    }

    public void hardDrop() {
        if (paused || gameOver) {
            return;
        }

        int droppedRows = 0;
        while (tryMove(0, 1)) {
            droppedRows++;
        }

        score += droppedRows * 2;
        lockPiece();
    }

    public void rotateClockwise() {
        rotate(1);
    }

    public void rotateCounterClockwise() {
        rotate(-1);
    }

    public void togglePause() {
        if (!gameOver) {
            paused = !paused;
        }
    }

    private boolean tryMove(int dx, int dy) {
        int nextRow = activePiece.getRow() + dy;
        int nextCol = activePiece.getCol() + dx;

        if (board.canPlace(activePiece, nextRow, nextCol, activePiece.getRotation())) {
            activePiece.moveTo(nextRow, nextCol);
            return true;
        }

        return false;
    }

    private void rotate(int delta) {
        if (paused || gameOver) {
            return;
        }

        int nextRotation = activePiece.getRotation() + delta;
        int[][] kicks = {
            {0, 0},
            {1, 0},
            {-1, 0},
            {2, 0},
            {-2, 0},
            {0, -1}
        };

        for (int[] kick : kicks) {
            int nextCol = activePiece.getCol() + kick[0];
            int nextRow = activePiece.getRow() + kick[1];

            if (board.canPlace(activePiece, nextRow, nextCol, nextRotation)) {
                activePiece.moveTo(nextRow, nextCol);
                activePiece.setRotation(nextRotation);
                return;
            }
        }
    }

    private void lockPiece() {
        board.lock(activePiece);

        int cleared = board.clearFullLines();
        if (cleared > 0) {
            score += switch (cleared) {
                case 1 -> 100 * level;
                case 2 -> 300 * level;
                case 3 -> 500 * level;
                default -> 800 * level;
            };
            linesCleared += cleared;
            level = 1 + (linesCleared / 10);
        }

        spawnPiece();
    }

    private void spawnPiece() {
        TetrominoType currentType = nextType;
        nextType = drawFromBag();
        activePiece.reset(currentType, SPAWN_ROW, SPAWN_COL);

        if (!board.canPlace(activePiece)) {
            gameOver = true;
        }
    }

    private TetrominoType drawFromBag() {
        if (bag.isEmpty()) {
            bag.addAll(Arrays.asList(TetrominoType.values()));
            Collections.shuffle(bag, random);
        }
        return bag.remove(bag.size() - 1);
    }

    public List<Point> getGhostCells() {
        int dropDistance = 0;
        while (board.canPlace(
            activePiece,
            activePiece.getRow() + dropDistance + 1,
            activePiece.getCol(),
            activePiece.getRotation()
        )) {
            dropDistance++;
        }

        return activePiece.cellsFor(
            activePiece.getRow() + dropDistance,
            activePiece.getCol(),
            activePiece.getRotation()
        );
    }

    public TetrisBoard getBoard() {
        return board;
    }

    public FallingPiece getActivePiece() {
        return activePiece;
    }

    public TetrominoType getNextType() {
        return nextType;
    }

    public int getScore() {
        return score;
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    public int getLevel() {
        return level;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getTickDelay() {
        return Math.max(120, 620 - ((level - 1) * 45));
    }
}
