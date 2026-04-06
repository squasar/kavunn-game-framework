package physics.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Geometry2;
import physics.geometry.Mesh2;
import physics.geometry.Transform2;
import physics.geometry.Vector2;

public final class CompositeForm implements Form2 {

    public static final class Part {

        private final String name;
        private final Form2 form;
        private final Transform2 localTransform;

        public Part(String name, Form2 form, Transform2 localTransform) {
            this.name = name;
            this.form = form;
            this.localTransform = localTransform;
        }

        public String getName() {
            return this.name;
        }

        public Form2 getForm() {
            return this.form;
        }

        public Transform2 getLocalTransform() {
            return this.localTransform;
        }
    }

    private final List<Part> parts;

    public CompositeForm(List<Part> parts) {
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("A composite form requires at least one part.");
        }
        this.parts = Collections.unmodifiableList(new ArrayList<>(parts));
    }

    public List<Part> getParts() {
        return this.parts;
    }

    @Override
    public String getFormType() {
        return "composite";
    }

    @Override
    public Bounds2 getLocalBounds() {
        Bounds2 bounds = null;
        for (Part part : this.parts) {
            Bounds2 partBounds = part.getForm().toWorldMesh(part.getLocalTransform(), 24).getBounds();
            bounds = bounds == null ? partBounds : bounds.union(partBounds);
        }
        return bounds == null ? new Bounds2(0.0, 0.0, 0.0, 0.0) : bounds;
    }

    @Override
    public Mesh2 toMesh(int subdivisionHint) {
        Mesh2 mesh = Mesh2.empty();
        for (Part part : this.parts) {
            mesh = mesh.append(part.getForm().toWorldMesh(part.getLocalTransform(), subdivisionHint));
        }
        return mesh;
    }

    @Override
    public Contour2 sampleContour(int sampleCount) {
        List<Vector2> mergedPoints = new ArrayList<>();
        int perPart = Math.max(3, sampleCount / this.parts.size());
        for (Part part : this.parts) {
            mergedPoints.addAll(part.getForm().toWorldContour(part.getLocalTransform(), perPart).getPoints());
        }
        return new Contour2(Geometry2.convexHull(mergedPoints));
    }
}
