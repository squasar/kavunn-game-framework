import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import core.Entity;
import core.Relation;

public class FallingPiece extends Entity<Relation> {

    private TetrominoType type = TetrominoType.I;
    private int row = 0;
    private int col = 3;
    private int rotation = 0;

    public FallingPiece(int id, String label) {
        super(id, label);
    }

    public void reset(TetrominoType type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
        this.rotation = 0;
    }

    public void moveBy(int dx, int dy) {
        this.col += dx;
        this.row += dy;
    }

    public void moveTo(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setRotation(int rotation) {
        this.rotation = Math.floorMod(rotation, 4);
    }

    public List<Point> cells() {
        return cellsFor(row, col, rotation);
    }

    public List<Point> cellsFor(int row, int col, int rotation) {
        List<Point> points = new ArrayList<>();
        for (int[] cell : type.cells(rotation)) {
            points.add(new Point(col + cell[1], row + cell[0]));
        }
        return points;
    }

    public TetrominoType getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getRotation() {
        return rotation;
    }
}
