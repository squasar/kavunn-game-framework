package physics.authoring;

import physics.geometry.Vector2;

public final class AnchorPoint {

    private final String name;
    private final Vector2 localPosition;

    public AnchorPoint(String name, Vector2 localPosition) {
        this.name = name;
        this.localPosition = localPosition;
    }

    public String getName() {
        return this.name;
    }

    public Vector2 getLocalPosition() {
        return this.localPosition;
    }
}
