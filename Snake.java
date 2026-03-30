package com.example;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.example.core.Entity;
import com.example.core.Relation;

public class Snake extends Entity<Relation> {

    private final Deque<Point> body = new ArrayDeque<>();
    private Direction direction = Direction.RIGHT;
    private Direction pendingDirection = Direction.RIGHT;

    public Snake(int id, String label, int startX, int startY) {
        super(id, label);
        reset(startX, startY);
    }

    public void reset(int startX, int startY) {
        body.clear();
        body.addLast(new Point(startX, startY));
        body.addLast(new Point(startX - 1, startY));
        body.addLast(new Point(startX - 2, startY));
        direction = Direction.RIGHT;
        pendingDirection = Direction.RIGHT;
    }

    public void queueDirection(Direction nextDirection) {
        if (nextDirection == null || nextDirection.isOpposite(direction)) {
            return;
        }
        pendingDirection = nextDirection;
    }

    public Point nextHead() {
        Point head = body.peekFirst();
        return new Point(head.x + pendingDirection.dx(), head.y + pendingDirection.dy());
    }

    public void advance(Point nextHead, boolean grow) {
        direction = pendingDirection;
        body.addFirst(new Point(nextHead));
        if (!grow) {
            body.removeLast();
        }
    }

    public boolean hitsSelf(Point candidate, boolean growing) {
        int lastIndex = body.size() - 1;
        int index = 0;

        for (Point segment : body) {
            if (!growing && index == lastIndex) {
                break;
            }
            if (segment.equals(candidate)) {
                return true;
            }
            index++;
        }

        return false;
    }

    public boolean occupies(Point point) {
        for (Point segment : body) {
            if (segment.equals(point)) {
                return true;
            }
        }
        return false;
    }

    public Point getHead() {
        return new Point(body.peekFirst());
    }

    public int getLength() {
        return body.size();
    }

    public List<Point> getSegments() {
        List<Point> segments = new ArrayList<>();
        for (Point point : body) {
            segments.add(new Point(point));
        }
        return segments;
    }
}
