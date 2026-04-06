package physics.authoring;

import java.util.ArrayList;
import java.util.List;

import physics.form.Form2;
import physics.form.MeshForm;
import physics.geometry.Contour2;
import physics.geometry.Geometry2;
import physics.geometry.Mesh2;
import physics.geometry.Triangulator2;
import physics.geometry.Vector2;

public final class FormMorpher {

    private FormMorpher() {
    }

    public static MeshForm morph(Form2 from, Form2 to, double progress, int sampleCount) {
        double alpha = Geometry2.clamp(progress, 0.0, 1.0);
        int safeCount = Math.max(12, sampleCount);

        Contour2 fromContour = from.sampleContour(safeCount).resample(safeCount).ensureCounterClockwise();
        Contour2 toContour = to.sampleContour(safeCount).resample(safeCount).ensureCounterClockwise();

        List<Vector2> morphedPoints = new ArrayList<>(safeCount);
        for (int index = 0; index < safeCount; index++) {
            morphedPoints.add(fromContour.get(index).lerp(toContour.get(index), alpha));
        }

        Contour2 contour = new Contour2(morphedPoints);
        Mesh2 mesh = Triangulator2.triangulate(contour);
        return new MeshForm(mesh, contour);
    }
}
