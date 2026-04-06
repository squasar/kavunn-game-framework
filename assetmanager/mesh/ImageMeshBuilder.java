package assetmanager.mesh;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import assetmanager.source.EntityImageSequence;
import assetmanager.source.ImageFrame;
import physics.authoring.AnchorPoint;
import physics.form.MeshForm;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Transform2;
import physics.geometry.Triangle2;
import physics.geometry.Vector2;
import physics.importing.RasterShapeExtractor;
import physics.importing.RasterShapeProfile;

public final class ImageMeshBuilder {

    public MeshedImageFrame mesh(BufferedImage image, MeshPreparationOptions options) {
        return mesh(new ImageFrame("direct-frame", "default", 0, 0L, new assetmanager.source.BufferedImageAssetSource("direct-buffer", image)), options);
    }

    public MeshedImageFrame mesh(ImageFrame frame, MeshPreparationOptions options) {
        if (frame == null) {
            throw new IllegalArgumentException("Image mesh building requires a frame.");
        }

        MeshPreparationOptions safeOptions = options == null ? MeshPreparationOptions.defaults() : options;
        RasterShapeProfile rawProfile = RasterShapeExtractor.extract(frame.loadImage(), safeOptions.getRasterShapeOptions());
        if (!safeOptions.isNormalizeToCenter()) {
            return new MeshedImageFrame(
                frame,
                rawProfile,
                rawProfile.getForm(),
                rawProfile.getContour(),
                rawProfile.getMesh(),
                rawProfile.getBounds(),
                rawProfile.getPalette(),
                rawProfile.getAnchors()
            );
        }

        Vector2 offset = rawProfile.getBounds().getCenter().multiply(-1.0);
        Transform2 translation = Transform2.translation(offset.getX(), offset.getY());
        Mesh2 normalizedMesh = translateMesh(rawProfile.getMesh(), offset);
        Contour2 normalizedContour = rawProfile.getContour().transformed(translation);
        MeshForm normalizedForm = new MeshForm(normalizedMesh, normalizedContour);
        List<AnchorPoint> normalizedAnchors = translateAnchors(rawProfile.getAnchors(), offset);

        return new MeshedImageFrame(
            frame,
            rawProfile,
            normalizedForm,
            normalizedContour,
            normalizedMesh,
            normalizedContour.getBounds(),
            rawProfile.getPalette(),
            normalizedAnchors
        );
    }

    public EntityMeshSequence mesh(EntityImageSequence sequence, MeshPreparationOptions options) {
        if (sequence == null) {
            throw new IllegalArgumentException("Entity mesh building requires an image sequence.");
        }

        List<MeshedImageFrame> frames = new ArrayList<>();
        for (ImageFrame frame : sequence.getFramesInPreparationOrder()) {
            frames.add(mesh(frame, options));
        }
        return new EntityMeshSequence(sequence.getEntityKey(), frames);
    }

    private static Mesh2 translateMesh(Mesh2 mesh, Vector2 offset) {
        List<Triangle2> translated = new ArrayList<>(mesh.getTriangles().size());
        for (Triangle2 triangle : mesh.getTriangles()) {
            translated.add(triangle.translated(offset));
        }
        return new Mesh2(translated);
    }

    private static List<AnchorPoint> translateAnchors(List<AnchorPoint> anchors, Vector2 offset) {
        List<AnchorPoint> translated = new ArrayList<>(anchors.size());
        for (AnchorPoint anchor : anchors) {
            translated.add(new AnchorPoint(anchor.getName(), anchor.getLocalPosition().add(offset)));
        }
        return translated;
    }
}
