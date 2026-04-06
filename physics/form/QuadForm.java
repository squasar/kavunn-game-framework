package physics.form;

import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Quad2;

public final class QuadForm implements Form2 {

    private final Quad2 quad;
    private Bounds2 cachedBounds;
    private Mesh2 cachedMesh;
    private Contour2 cachedContour;

    public QuadForm(Quad2 quad) {
        this.quad = quad;
    }

    public Quad2 getQuad() {
        return this.quad;
    }

    @Override
    public String getFormType() {
        return "quad";
    }

    @Override
    public Bounds2 getLocalBounds() {
        if (this.cachedBounds == null) {
            this.cachedBounds = Bounds2.fromPoints(this.quad.getVertices());
        }
        return this.cachedBounds;
    }

    @Override
    public Mesh2 toMesh(int subdivisionHint) {
        if (this.cachedMesh == null) {
            this.cachedMesh = new Mesh2(this.quad.toTriangles());
        }
        return this.cachedMesh;
    }

    @Override
    public Contour2 sampleContour(int sampleCount) {
        if (this.cachedContour == null) {
            this.cachedContour = this.quad.toContour();
        }
        return this.cachedContour;
    }
}
