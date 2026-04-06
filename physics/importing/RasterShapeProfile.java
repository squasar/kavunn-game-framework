package physics.importing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.authoring.AnchorPoint;
import physics.form.MeshForm;
import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.palette.PaletteSet;

public final class RasterShapeProfile {

    private final MeshForm form;
    private final Contour2 contour;
    private final Mesh2 mesh;
    private final Bounds2 bounds;
    private final PaletteSet palette;
    private final List<AnchorPoint> anchors;

    public RasterShapeProfile(
        MeshForm form,
        Contour2 contour,
        Mesh2 mesh,
        Bounds2 bounds,
        PaletteSet palette,
        List<AnchorPoint> anchors
    ) {
        this.form = form;
        this.contour = contour;
        this.mesh = mesh;
        this.bounds = bounds;
        this.palette = palette;
        this.anchors = Collections.unmodifiableList(new ArrayList<>(anchors));
    }

    public MeshForm getForm() {
        return this.form;
    }

    public Contour2 getContour() {
        return this.contour;
    }

    public Mesh2 getMesh() {
        return this.mesh;
    }

    public Bounds2 getBounds() {
        return this.bounds;
    }

    public PaletteSet getPalette() {
        return this.palette;
    }

    public List<AnchorPoint> getAnchors() {
        return this.anchors;
    }
}
