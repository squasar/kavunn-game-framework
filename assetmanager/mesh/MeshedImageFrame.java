package assetmanager.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import assetmanager.source.ImageFrame;
import physics.authoring.AnchorPoint;
import physics.form.MeshForm;
import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.importing.RasterShapeProfile;
import physics.palette.PaletteSet;

public final class MeshedImageFrame {

    private final ImageFrame sourceFrame;
    private final RasterShapeProfile sourceProfile;
    private final MeshForm meshForm;
    private final Contour2 contour;
    private final Mesh2 mesh;
    private final Bounds2 bounds;
    private final PaletteSet palette;
    private final List<AnchorPoint> anchors;

    public MeshedImageFrame(
        ImageFrame sourceFrame,
        RasterShapeProfile sourceProfile,
        MeshForm meshForm,
        Contour2 contour,
        Mesh2 mesh,
        Bounds2 bounds,
        PaletteSet palette,
        List<AnchorPoint> anchors
    ) {
        this.sourceFrame = sourceFrame;
        this.sourceProfile = sourceProfile;
        this.meshForm = meshForm;
        this.contour = contour;
        this.mesh = mesh;
        this.bounds = bounds;
        this.palette = palette;
        this.anchors = Collections.unmodifiableList(new ArrayList<>(anchors));
    }

    public ImageFrame getSourceFrame() {
        return this.sourceFrame;
    }

    public RasterShapeProfile getSourceProfile() {
        return this.sourceProfile;
    }

    public MeshForm getMeshForm() {
        return this.meshForm;
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
