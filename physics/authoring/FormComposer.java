package physics.authoring;

import java.util.ArrayList;
import java.util.List;

import physics.form.CompositeForm;
import physics.form.Form2;
import physics.geometry.Bounds2;
import physics.geometry.Transform2;
import physics.geometry.Vector2;

public final class FormComposer {

    private FormComposer() {
    }

    public static CompositeForm compose(Form2 form) {
        return new CompositeForm(List.of(new CompositeForm.Part("root", form, Transform2.identity())));
    }

    public static CompositeForm attach(
        Form2 base,
        String baseAnchor,
        Form2 addition,
        String additionAnchor,
        Vector2 offset
    ) {
        Vector2 basePoint = resolveStandardAnchor(base.getLocalBounds(), baseAnchor);
        Vector2 additionPoint = resolveStandardAnchor(addition.getLocalBounds(), additionAnchor);
        Vector2 translation = basePoint.add(offset).subtract(additionPoint);

        List<CompositeForm.Part> parts = new ArrayList<>();
        parts.add(new CompositeForm.Part("base", base, Transform2.identity()));
        parts.add(new CompositeForm.Part("addition", addition, Transform2.translation(translation.getX(), translation.getY())));
        return new CompositeForm(parts);
    }

    public static List<AnchorPoint> createStandardAnchors(Bounds2 bounds) {
        Vector2 center = bounds.getCenter();
        return List.of(
            new AnchorPoint("center", center),
            new AnchorPoint("top", new Vector2(center.getX(), bounds.getMinY())),
            new AnchorPoint("bottom", new Vector2(center.getX(), bounds.getMaxY())),
            new AnchorPoint("left", new Vector2(bounds.getMinX(), center.getY())),
            new AnchorPoint("right", new Vector2(bounds.getMaxX(), center.getY())),
            new AnchorPoint("topLeft", new Vector2(bounds.getMinX(), bounds.getMinY())),
            new AnchorPoint("topRight", new Vector2(bounds.getMaxX(), bounds.getMinY())),
            new AnchorPoint("bottomLeft", new Vector2(bounds.getMinX(), bounds.getMaxY())),
            new AnchorPoint("bottomRight", new Vector2(bounds.getMaxX(), bounds.getMaxY()))
        );
    }

    public static Vector2 resolveStandardAnchor(Bounds2 bounds, String anchorName) {
        String safeName = anchorName == null ? "center" : anchorName;
        Vector2 center = bounds.getCenter();
        return switch (safeName) {
            case "top" -> new Vector2(center.getX(), bounds.getMinY());
            case "bottom" -> new Vector2(center.getX(), bounds.getMaxY());
            case "left" -> new Vector2(bounds.getMinX(), center.getY());
            case "right" -> new Vector2(bounds.getMaxX(), center.getY());
            case "topLeft" -> new Vector2(bounds.getMinX(), bounds.getMinY());
            case "topRight" -> new Vector2(bounds.getMaxX(), bounds.getMinY());
            case "bottomLeft" -> new Vector2(bounds.getMinX(), bounds.getMaxY());
            case "bottomRight" -> new Vector2(bounds.getMaxX(), bounds.getMaxY());
            default -> center;
        };
    }
}
