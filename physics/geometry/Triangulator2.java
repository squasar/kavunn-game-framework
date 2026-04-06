package physics.geometry;

import java.util.ArrayList;
import java.util.List;

public final class Triangulator2 {

    private Triangulator2() {
    }

    public static Mesh2 triangulate(Contour2 contour) {
        List<Vector2> polygon = new ArrayList<>(contour.ensureCounterClockwise().getPoints());
        if (polygon.size() < 3) {
            return Mesh2.empty();
        }

        List<Triangle2> triangles = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (int index = 0; index < polygon.size(); index++) {
            indices.add(index);
        }

        int guard = 0;
        while (indices.size() > 3 && guard < 10_000) {
            boolean earFound = false;
            for (int index = 0; index < indices.size(); index++) {
                int prevIndex = indices.get((index - 1 + indices.size()) % indices.size());
                int currIndex = indices.get(index);
                int nextIndex = indices.get((index + 1) % indices.size());

                Vector2 prev = polygon.get(prevIndex);
                Vector2 current = polygon.get(currIndex);
                Vector2 next = polygon.get(nextIndex);

                if (current.subtract(prev).cross(next.subtract(current)) <= 0.0) {
                    continue;
                }

                Triangle2 candidate = new Triangle2(prev, current, next);
                boolean containsOtherPoint = false;
                for (Integer otherIndex : indices) {
                    if (otherIndex == prevIndex || otherIndex == currIndex || otherIndex == nextIndex) {
                        continue;
                    }
                    if (Geometry2.pointInTriangle(polygon.get(otherIndex), candidate)) {
                        containsOtherPoint = true;
                        break;
                    }
                }

                if (!containsOtherPoint) {
                    triangles.add(candidate);
                    indices.remove(index);
                    earFound = true;
                    break;
                }
            }

            if (!earFound) {
                break;
            }
            guard++;
        }

        if (indices.size() == 3) {
            triangles.add(new Triangle2(
                polygon.get(indices.get(0)),
                polygon.get(indices.get(1)),
                polygon.get(indices.get(2))
            ));
        }

        return new Mesh2(triangles);
    }
}
