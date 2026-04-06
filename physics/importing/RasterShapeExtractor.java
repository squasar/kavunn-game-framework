package physics.importing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import physics.authoring.AnchorPoint;
import physics.authoring.FormComposer;
import physics.form.MeshForm;
import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Triangulator2;
import physics.palette.PaletteExtractor;
import physics.palette.PaletteSet;

public final class RasterShapeExtractor {

    private RasterShapeExtractor() {
    }

    public static RasterShapeProfile extract(BufferedImage image) {
        return extract(image, RasterShapeOptions.defaults());
    }

    public static RasterShapeProfile extract(BufferedImage image, RasterShapeOptions options) {
        if (image == null) {
            throw new IllegalArgumentException("Raster shape extraction requires an image.");
        }

        Contour2 contour = ContourTracer.trace(image, options.getAlphaThreshold()).ensureCounterClockwise();
        if (contour.size() > options.getMaxContourPoints()) {
            contour = contour.resample(options.getMaxContourPoints()).ensureCounterClockwise();
        }
        contour = contour.resample(options.getContourSamples()).ensureCounterClockwise();

        Mesh2 mesh = Triangulator2.triangulate(contour);
        MeshForm form = new MeshForm(mesh, contour);
        Bounds2 bounds = contour.getBounds();
        PaletteSet palette = PaletteExtractor.extract(image, options.getAlphaThreshold(), options.getMaxPaletteColors());

        List<AnchorPoint> anchors = new ArrayList<>(FormComposer.createStandardAnchors(bounds));
        anchors.add(new AnchorPoint("centroid", contour.getCentroid()));

        return new RasterShapeProfile(form, contour, mesh, bounds, palette, anchors);
    }
}
