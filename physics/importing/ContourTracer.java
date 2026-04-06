package physics.importing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import physics.geometry.Contour2;
import physics.geometry.Vector2;

public final class ContourTracer {

    private ContourTracer() {
    }

    public static Contour2 trace(BufferedImage image, int alphaThreshold) {
        List<Vector2> boundary = collectBoundary(image, alphaThreshold);
        if (boundary.size() < 3) {
            boundary = List.of(
                new Vector2(0.0, 0.0),
                new Vector2(image.getWidth(), 0.0),
                new Vector2(image.getWidth(), image.getHeight()),
                new Vector2(0.0, image.getHeight())
            );
        }

        Vector2 centroid = centroid(boundary);
        boundary.sort(
            Comparator
                .comparingDouble((Vector2 point) -> Math.atan2(point.getY() - centroid.getY(), point.getX() - centroid.getX()))
                .thenComparingDouble(point -> centroid.distanceSquared(point))
        );

        return new Contour2(removeNearDuplicates(boundary));
    }

    private static List<Vector2> collectBoundary(BufferedImage image, int alphaThreshold) {
        List<Vector2> boundary = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (!isOpaque(image, x, y, alphaThreshold)) {
                    continue;
                }
                if (hasTransparentNeighbor(image, x, y, alphaThreshold)) {
                    boundary.add(new Vector2(x + 0.5, y + 0.5));
                }
            }
        }
        return boundary;
    }

    private static boolean hasTransparentNeighbor(BufferedImage image, int x, int y, int alphaThreshold) {
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                if (offsetX == 0 && offsetY == 0) {
                    continue;
                }

                int sampleX = x + offsetX;
                int sampleY = y + offsetY;
                if (sampleX < 0 || sampleY < 0 || sampleX >= image.getWidth() || sampleY >= image.getHeight()) {
                    return true;
                }
                if (!isOpaque(image, sampleX, sampleY, alphaThreshold)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOpaque(BufferedImage image, int x, int y, int alphaThreshold) {
        int alpha = (image.getRGB(x, y) >>> 24) & 0xFF;
        return alpha >= alphaThreshold;
    }

    private static Vector2 centroid(List<Vector2> points) {
        double x = 0.0;
        double y = 0.0;
        for (Vector2 point : points) {
            x += point.getX();
            y += point.getY();
        }
        double count = Math.max(1, points.size());
        return new Vector2(x / count, y / count);
    }

    private static List<Vector2> removeNearDuplicates(List<Vector2> points) {
        Set<String> visited = new LinkedHashSet<>();
        List<Vector2> unique = new ArrayList<>();
        for (Vector2 point : points) {
            String key = Math.round(point.getX() * 2.0) + ":" + Math.round(point.getY() * 2.0);
            if (visited.add(key)) {
                unique.add(point);
            }
        }
        return unique;
    }
}
