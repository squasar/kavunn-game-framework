package com.example;

import java.util.Arrays;

import com.example.core.Entity;
import com.example.core.Relation;

public class TetrisBoard extends Entity<Relation> {

    private final int rows;
    private final int cols;
    private final TetrominoType[][] lockedCells;

    public TetrisBoard(int id, String label, int rows, int cols) {
        super(id, label);
        this.rows = rows;
        this.cols = cols;
        this.lockedCells = new TetrominoType[rows][cols];
    }

    public void clear() {
        for (TetrominoType[] row : lockedCells) {
            Arrays.fill(row, null);
        }
    }

    public boolean canPlace(FallingPiece piece) {
        return canPlace(piece, piece.getRow(), piece.getCol(), piece.getRotation());
    }

    public boolean canPlace(FallingPiece piece, int row, int col, int rotation) {
        for (java.awt.Point point : piece.cellsFor(row, col, rotation)) {
            if (point.x < 0 || point.x >= cols || point.y < 0 || point.y >= rows) {
                return false;
            }
            if (lockedCells[point.y][point.x] != null) {
                return false;
            }
        }
        return true;
    }

    public void lock(FallingPiece piece) {
        for (java.awt.Point point : piece.cells()) {
            lockedCells[point.y][point.x] = piece.getType();
        }
    }

    public int clearFullLines() {
        TetrominoType[][] nextState = new TetrominoType[rows][cols];
        int targetRow = rows - 1;
        int cleared = 0;

        for (int row = rows - 1; row >= 0; row--) {
            if (isFull(row)) {
                cleared++;
                continue;
            }
            System.arraycopy(lockedCells[row], 0, nextState[targetRow], 0, cols);
            targetRow--;
        }

        for (int row = 0; row < rows; row++) {
            System.arraycopy(nextState[row], 0, lockedCells[row], 0, cols);
        }

        return cleared;
    }

    private boolean isFull(int row) {
        for (int col = 0; col < cols; col++) {
            if (lockedCells[row][col] == null) {
                return false;
            }
        }
        return true;
    }

    public TetrominoType getCell(int row, int col) {
        return lockedCells[row][col];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
