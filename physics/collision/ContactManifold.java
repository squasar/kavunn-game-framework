package physics.collision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import physics.geometry.Vector2;

public final class ContactManifold {

    private final List<ContactPoint> contactPoints;
    private final Vector2 normal;
    private final double penetrationDepth;

    public ContactManifold(List<ContactPoint> contactPoints, Vector2 normal, double penetrationDepth) {
        if (contactPoints.isEmpty()) {
            this.contactPoints = List.of();
        } else if (contactPoints.size() == 1) {
            this.contactPoints = List.of(contactPoints.get(0));
        } else {
            this.contactPoints = Collections.unmodifiableList(new ArrayList<>(contactPoints));
        }
        this.normal = normal;
        this.penetrationDepth = penetrationDepth;
    }

    public ContactManifold(ContactPoint contactPoint, Vector2 normal, double penetrationDepth) {
        this.contactPoints = List.of(contactPoint);
        this.normal = normal;
        this.penetrationDepth = penetrationDepth;
    }

    public List<ContactPoint> getContactPoints() {
        return this.contactPoints;
    }

    public Vector2 getNormal() {
        return this.normal;
    }

    public double getPenetrationDepth() {
        return this.penetrationDepth;
    }
}
