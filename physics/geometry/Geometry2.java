package physics.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class Geometry2 {

    private Geometry2() {
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean isClockwise(List<Vector2> polygon) {
        return polygonSignedArea(polygon) < 0.0;
    }

    public static double polygonSignedArea(List<Vector2> polygon) {
        double sum = 0.0;
        for (int index = 0; index < polygon.size(); index++) {
            Vector2 current = polygon.get(index);
            Vector2 next = polygon.get((index + 1) % polygon.size());
            sum += current.cross(next);
        }
        return sum * 0.5;
    }

    public static double contourLength(List<Vector2> contour) {
        double total = 0.0;
        for (int index = 0; index < contour.size(); index++) {
            total += contour.get(index).distanceTo(contour.get((index + 1) % contour.size()));
        }
        return total;
    }

    public static boolean pointInTriangle(Vector2 point, Triangle2 triangle) {
        Vector2 a = triangle.getA();
        Vector2 b = triangle.getB();
        Vector2 c = triangle.getC();

        double area = triangle.area();
        double a1 = Math.abs(point.subtract(a).cross(point.subtract(b))) * 0.5;
        double a2 = Math.abs(point.subtract(b).cross(point.subtract(c))) * 0.5;
        double a3 = Math.abs(point.subtract(c).cross(point.subtract(a))) * 0.5;

        return Math.abs((a1 + a2 + a3) - area) <= 1.0e-6;
    }

    public static Vector2 closestPointOnSegment(Vector2 point, Vector2 start, Vector2 end) {
        Vector2 segment = end.subtract(start);
        double lengthSquared = segment.lengthSquared();
        if (lengthSquared == 0.0) {
            return start;
        }

        double alpha = clamp(point.subtract(start).dot(segment) / lengthSquared, 0.0, 1.0);
        return start.add(segment.multiply(alpha));
    }

    public static List<Vector2> convexHull(Collection<Vector2> input) {
        List<Vector2> points = new ArrayList<>(input);
        if (points.size() <= 3) {
            return points;
        }

        points.sort(Comparator.comparingDouble(Vector2::getX).thenComparingDouble(Vector2::getY));
        List<Vector2> lower = new ArrayList<>();
        for (Vector2 point : points) {
            while (lower.size() >= 2 && cross(lower.get(lower.size() - 2), lower.get(lower.size() - 1), point) <= 0.0) {
                lower.remove(lower.size() - 1);
            }
            lower.add(point);
        }

        List<Vector2> upper = new ArrayList<>();
        for (int index = points.size() - 1; index >= 0; index--) {
            Vector2 point = points.get(index);
            while (upper.size() >= 2 && cross(upper.get(upper.size() - 2), upper.get(upper.size() - 1), point) <= 0.0) {
                upper.remove(upper.size() - 1);
            }
            upper.add(point);
        }

        lower.remove(lower.size() - 1);
        upper.remove(upper.size() - 1);
        lower.addAll(upper);
        return lower;
    }

    private static double cross(Vector2 a, Vector2 b, Vector2 c) {
        return b.subtract(a).cross(c.subtract(a));
    }
}
