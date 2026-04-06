import java.awt.Point;

import core.Entity;
import core.Relation;

public class Food extends Entity<Relation> {

    private Point position = new Point(0, 0);

    public Food(int id, String label) {
        super(id, label);
    }

    public Point getPosition() {
        return new Point(position);
    }

    public void relocate(Point nextPosition) {
        position = new Point(nextPosition);
    }
}
