package physics.form;

import java.util.ArrayList;
import java.util.List;

import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Geometry2;
import physics.geometry.Mesh2;
import physics.geometry.Vector2;

public final class MeshForm implements Form2 {

    private final Mesh2 mesh;
    private final Contour2 contour;

    public MeshForm(Mesh2 mesh) {
        this(mesh, deriveContour(mesh));
    }

    public MeshForm(Mesh2 mesh, Contour2 contour) {
        this.mesh = mesh;
        this.contour = contour;
    }

    public Mesh2 getMesh() {
        return this.mesh;
    }

    public Contour2 getContour() {
        return this.contour;
    }

    @Override
    public String getFormType() {
        return "mesh";
    }

    @Override
    public Bounds2 getLocalBounds() {
        return this.mesh.getBounds();
    }

    @Override
    public Mesh2 toMesh(int subdivisionHint) {
        return this.mesh;
    }

    @Override
    public Contour2 sampleContour(int sampleCount) {
        return this.contour.resample(Math.max(3, sampleCount));
    }

    private static Contour2 deriveContour(Mesh2 mesh) {
        List<Vector2> points = new ArrayList<>();
        mesh.getTriangles().forEach(triangle -> points.addAll(triangle.getVertices()));
        List<Vector2> hull = Geometry2.convexHull(points);
        if (hull.size() < 3) {
            throw new IllegalArgumentException("A mesh form requires at least one non-degenerate triangle.");
        }
        return new Contour2(hull);
    }
}
