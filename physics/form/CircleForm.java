package physics.form;

import java.util.ArrayList;
import java.util.List;

import physics.geometry.Bounds2;
import physics.geometry.Circle2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Triangle2;
import physics.geometry.Vector2;

public final class CircleForm implements Form2 {

    private final Circle2 circle;
    private final int defaultSubdivision;
    private Mesh2 cachedMesh;
    private int cachedMeshSubdivision = Integer.MIN_VALUE;
    private Contour2 cachedContour;
    private int cachedContourSamples = Integer.MIN_VALUE;

    public CircleForm(Circle2 circle) {
        this(circle, 24);
    }

    public CircleForm(Circle2 circle, int defaultSubdivision) {
        this.circle = circle;
        this.defaultSubdivision = Math.max(8, defaultSubdivision);
    }

    public Circle2 getCircle() {
        return this.circle;
    }

    @Override
    public String getFormType() {
        return "circle";
    }

    @Override
    public Bounds2 getLocalBounds() {
        return this.circle.getBounds();
    }

    @Override
    public Mesh2 toMesh(int subdivisionHint) {
        int segments = Math.max(this.defaultSubdivision, subdivisionHint);
        if (this.cachedMesh != null && this.cachedMeshSubdivision == segments) {
            return this.cachedMesh;
        }
        Contour2 contour = this.circle.sampleContour(segments);
        Vector2 center = this.circle.getCenter();
        List<Triangle2> triangles = new ArrayList<>(segments);
        for (int index = 0; index < contour.size(); index++) {
            Vector2 current = contour.get(index);
            Vector2 next = contour.get((index + 1) % contour.size());
            triangles.add(new Triangle2(center, current, next));
        }
        this.cachedMesh = new Mesh2(triangles);
        this.cachedMeshSubdivision = segments;
        return this.cachedMesh;
    }

    @Override
    public Contour2 sampleContour(int sampleCount) {
        int safeCount = Math.max(this.defaultSubdivision, sampleCount);
        if (this.cachedContour != null && this.cachedContourSamples == safeCount) {
            return this.cachedContour;
        }
        this.cachedContour = this.circle.sampleContour(safeCount);
        this.cachedContourSamples = safeCount;
        return this.cachedContour;
    }
}
