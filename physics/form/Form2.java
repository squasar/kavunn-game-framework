package physics.form;

import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Transform2;
import physics.geometry.Vector2;

public interface Form2 {

    String getFormType();

    Bounds2 getLocalBounds();

    Mesh2 toMesh(int subdivisionHint);

    Contour2 sampleContour(int sampleCount);

    default Vector2 getLocalCentroid() {
        return sampleContour(24).getCentroid();
    }

    default Mesh2 toWorldMesh(Transform2 transform, int subdivisionHint) {
        return toMesh(subdivisionHint).transformed(transform);
    }

    default Contour2 toWorldContour(Transform2 transform, int sampleCount) {
        return sampleContour(sampleCount).transformed(transform);
    }
}
