package physics.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Contour2 {

    private final List<Vector2> points;

    public Contour2(List<Vector2> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("A contour requires at least 3 points.");
        }
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    public List<Vector2> getPoints() {
        return this.points;
    }

    public int size() {
        return this.points.size();
    }

    public Vector2 get(int index) {
        return this.points.get(index);
    }

    public Bounds2 getBounds() {
        return Bounds2.fromPoints(this.points);
    }

    public Vector2 getCentroid() {
        double areaFactor = 0.0;
        double centroidX = 0.0;
        double centroidY = 0.0;

        for (int index = 0; index < this.points.size(); index++) {
            Vector2 current = this.points.get(index);
            Vector2 next = this.points.get((index + 1) % this.points.size());
            double cross = current.cross(next);
            areaFactor += cross;
            centroidX += (current.getX() + next.getX()) * cross;
            centroidY += (current.getY() + next.getY()) * cross;
        }

        if (areaFactor == 0.0) {
            return getBounds().getCenter();
        }

        double area = areaFactor * 0.5;
        return new Vector2(centroidX / (6.0 * area), centroidY / (6.0 * area));
    }

    public Contour2 transformed(Transform2 transform) {
        List<Vector2> transformed = new ArrayList<>(this.points.size());
        for (Vector2 point : this.points) {
            transformed.add(transform.apply(point));
        }
        return new Contour2(transformed);
    }

    public Contour2 resample(int sampleCount) {
        int safeCount = Math.max(3, sampleCount);
        List<Vector2> resampled = new ArrayList<>(safeCount);
        double totalLength = Geometry2.contourLength(this.points);
        if (totalLength == 0.0) {
            return this;
        }

        double segmentLength = totalLength / safeCount;
        double carried = 0.0;
        int currentIndex = 0;
        Vector2 current = this.points.get(0);
        resampled.add(current);

        while (resampled.size() < safeCount) {
            Vector2 next = this.points.get((currentIndex + 1) % this.points.size());
            double edgeLength = current.distanceTo(next);
            if (edgeLength == 0.0) {
                currentIndex++;
                current = next;
                continue;
            }

            if (carried + edgeLength >= segmentLength) {
                double alpha = (segmentLength - carried) / edgeLength;
                current = current.lerp(next, alpha);
                resampled.add(current);
                carried = 0.0;
            } else {
                carried += edgeLength;
                currentIndex++;
                current = next;
            }
        }

        return new Contour2(resampled);
    }

    public Contour2 ensureCounterClockwise() {
        return Geometry2.isClockwise(this.points) ? reverse() : this;
    }

    public Contour2 reverse() {
        List<Vector2> reversed = new ArrayList<>(this.points);
        Collections.reverse(reversed);
        return new Contour2(reversed);
    }
}
