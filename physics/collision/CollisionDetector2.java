package physics.collision;

import java.util.List;

import physics.body.PhysicsBody;
import physics.form.CircleForm;
import physics.form.QuadForm;
import physics.geometry.Bounds2;
import physics.geometry.Contour2;
import physics.geometry.Mesh2;
import physics.geometry.Transform2;
import physics.geometry.Triangle2;
import physics.geometry.Vector2;

public final class CollisionDetector2 {

    private static final double EPSILON = 1.0e-8;

    private CollisionDetector2() {
    }

    public static CollisionReport detect(PhysicsBody bodyA, PhysicsBody bodyB, int subdivisionHint) {
        if (bodyA == null || bodyB == null || !bodyA.isEnabled() || !bodyB.isEnabled()) {
            return CollisionReport.noHit();
        }

        Bounds2 boundsA = bodyA.getWorldBounds();
        Bounds2 boundsB = bodyB.getWorldBounds();
        if (!boundsA.intersects(boundsB)) {
            return CollisionReport.noHit();
        }

        CollisionReport specialized = detectSpecialized(
            bodyA,
            bodyA.getTransform(),
            bodyB,
            bodyB.getTransform()
        );
        if (specialized != null) {
            return specialized;
        }

        return detectGeneric(bodyA, bodyA.getTransform(), bodyB, bodyB.getTransform(), subdivisionHint);
    }

    public static CollisionReport detect(
        PhysicsBody bodyA,
        Transform2 transformA,
        PhysicsBody bodyB,
        Transform2 transformB,
        int subdivisionHint
    ) {
        if (bodyA == null || bodyB == null || !bodyA.isEnabled() || !bodyB.isEnabled()) {
            return CollisionReport.noHit();
        }

        Bounds2 boundsA = transformA == bodyA.getTransform()
            ? bodyA.getWorldBounds()
            : transformBounds(bodyA.getForm().getLocalBounds(), transformA);
        Bounds2 boundsB = transformB == bodyB.getTransform()
            ? bodyB.getWorldBounds()
            : transformBounds(bodyB.getForm().getLocalBounds(), transformB);
        if (!boundsA.intersects(boundsB)) {
            return CollisionReport.noHit();
        }

        CollisionReport specialized = detectSpecialized(bodyA, transformA, bodyB, transformB);
        if (specialized != null) {
            return specialized;
        }

        return detectGeneric(bodyA, transformA, bodyB, transformB, subdivisionHint);
    }

    public static RaycastHit raycastBody(PhysicsBody body, Vector2 origin, Vector2 direction, double maxDistance, int sampleCount) {
        if (body == null || !body.isEnabled()) {
            return RaycastHit.noHit();
        }

        Vector2 safeDirection = direction.normalized();
        if (safeDirection.isZero()) {
            return RaycastHit.noHit();
        }

        Contour2 contour = body.getWorldContour(Math.max(12, sampleCount));
        RaycastHit best = RaycastHit.noHit();
        for (int index = 0; index < contour.size(); index++) {
            Vector2 start = contour.get(index);
            Vector2 end = contour.get((index + 1) % contour.size());
            RaycastHit hit = intersectRaySegment(body, origin, safeDirection, maxDistance, start, end);
            if (hit.isHit() && hit.getDistance() < best.getDistance()) {
                best = hit;
            }
        }
        return best;
    }

    private static CollisionReport detectSpecialized(
        PhysicsBody bodyA,
        Transform2 transformA,
        PhysicsBody bodyB,
        Transform2 transformB
    ) {
        if (bodyA.getForm() instanceof CircleForm circleA && bodyB.getForm() instanceof CircleForm circleB) {
            return detectCircleCircle(bodyA, circleA, transformA, bodyB, circleB, transformB);
        }
        if (bodyA.getForm() instanceof CircleForm circle && bodyB.getForm() instanceof QuadForm quad) {
            return detectCircleQuad(bodyA, circle, transformA, bodyB, quad, transformB, true);
        }
        if (bodyA.getForm() instanceof QuadForm quad && bodyB.getForm() instanceof CircleForm circle) {
            return detectCircleQuad(bodyB, circle, transformB, bodyA, quad, transformA, false);
        }
        return null;
    }

    private static CollisionReport detectCircleCircle(
        PhysicsBody bodyA,
        CircleForm circleA,
        Transform2 transformA,
        PhysicsBody bodyB,
        CircleForm circleB,
        Transform2 transformB
    ) {
        Vector2 centerA = transformA.apply(circleA.getCircle().getCenter());
        Vector2 centerB = transformB.apply(circleB.getCircle().getCenter());
        double radiusA = scaledCircleRadius(circleA, transformA);
        double radiusB = scaledCircleRadius(circleB, transformB);
        Vector2 delta = centerB.subtract(centerA);
        double distanceSquared = delta.lengthSquared();
        double radiusSum = radiusA + radiusB;
        if (distanceSquared > radiusSum * radiusSum) {
            return CollisionReport.noHit();
        }

        double distance = Math.sqrt(distanceSquared);
        Vector2 normal = distance <= EPSILON ? Vector2.UNIT_X : delta.divide(distance);
        double penetrationDepth = radiusSum - distance;
        Vector2 contactPosition = centerA.add(normal.multiply(radiusA - penetrationDepth * 0.5));
        return createHitReport(bodyA, bodyB, normal, contactPosition, penetrationDepth);
    }

    private static CollisionReport detectCircleQuad(
        PhysicsBody circleBody,
        CircleForm circleForm,
        Transform2 circleTransform,
        PhysicsBody quadBody,
        QuadForm quadForm,
        Transform2 quadTransform,
        boolean circleIsBodyA
    ) {
        Vector2 circleCenterWorld = circleTransform.apply(circleForm.getCircle().getCenter());
        Vector2 circleCenterLocal = quadTransform.inverseApply(circleCenterWorld);
        Bounds2 quadBounds = quadForm.getLocalBounds();
        double radius = scaledCircleRadius(circleForm, circleTransform);

        boolean centerInside = quadBounds.contains(circleCenterLocal);
        double closestX = clamp(circleCenterLocal.getX(), quadBounds.getMinX(), quadBounds.getMaxX());
        double closestY = clamp(circleCenterLocal.getY(), quadBounds.getMinY(), quadBounds.getMaxY());

        Vector2 localNormalCircleToQuad;
        Vector2 localContactPoint;
        double penetrationDepth;

        if (centerInside) {
            double toLeft = circleCenterLocal.getX() - quadBounds.getMinX();
            double toRight = quadBounds.getMaxX() - circleCenterLocal.getX();
            double toBottom = circleCenterLocal.getY() - quadBounds.getMinY();
            double toTop = quadBounds.getMaxY() - circleCenterLocal.getY();
            double minimum = Math.min(Math.min(toLeft, toRight), Math.min(toBottom, toTop));

            if (minimum == toLeft) {
                localNormalCircleToQuad = Vector2.UNIT_X;
                localContactPoint = new Vector2(quadBounds.getMinX(), circleCenterLocal.getY());
            } else if (minimum == toRight) {
                localNormalCircleToQuad = Vector2.UNIT_X.multiply(-1.0);
                localContactPoint = new Vector2(quadBounds.getMaxX(), circleCenterLocal.getY());
            } else if (minimum == toBottom) {
                localNormalCircleToQuad = Vector2.UNIT_Y;
                localContactPoint = new Vector2(circleCenterLocal.getX(), quadBounds.getMinY());
            } else {
                localNormalCircleToQuad = Vector2.UNIT_Y.multiply(-1.0);
                localContactPoint = new Vector2(circleCenterLocal.getX(), quadBounds.getMaxY());
            }
            penetrationDepth = radius + minimum;
        } else {
            double deltaX = closestX - circleCenterLocal.getX();
            double deltaY = closestY - circleCenterLocal.getY();
            double distanceSquared = deltaX * deltaX + deltaY * deltaY;
            if (distanceSquared > radius * radius) {
                return CollisionReport.noHit();
            }

            double distance = Math.sqrt(distanceSquared);
            if (distance <= EPSILON) {
                Vector2 quadCenterLocal = quadBounds.getCenter();
                localNormalCircleToQuad = quadCenterLocal.subtract(circleCenterLocal).normalized();
                if (localNormalCircleToQuad.isZero()) {
                    localNormalCircleToQuad = Vector2.UNIT_X;
                }
            } else {
                localNormalCircleToQuad = new Vector2(deltaX / distance, deltaY / distance);
            }
            localContactPoint = new Vector2(closestX, closestY);
            penetrationDepth = radius - distance;
        }

        Vector2 worldNormalCircleToQuad = quadTransform.applyDirection(localNormalCircleToQuad).normalized();
        if (worldNormalCircleToQuad.isZero()) {
            worldNormalCircleToQuad = Vector2.UNIT_X;
        }
        Vector2 worldContactPoint = quadTransform.apply(localContactPoint);

        if (circleIsBodyA) {
            return createHitReport(circleBody, quadBody, worldNormalCircleToQuad, worldContactPoint, penetrationDepth);
        }
        return createHitReport(quadBody, circleBody, worldNormalCircleToQuad.multiply(-1.0), worldContactPoint, penetrationDepth);
    }

    private static CollisionReport detectGeneric(
        PhysicsBody bodyA,
        Transform2 transformA,
        PhysicsBody bodyB,
        Transform2 transformB,
        int subdivisionHint
    ) {
        Mesh2 meshA = bodyA.getForm().toWorldMesh(transformA, subdivisionHint);
        Mesh2 meshB = bodyB.getForm().toWorldMesh(transformB, subdivisionHint);
        CollisionReport best = CollisionReport.noHit();
        for (Triangle2 triangleA : meshA.getTriangles()) {
            for (Triangle2 triangleB : meshB.getTriangles()) {
                CollisionReport report = detectTriangleTriangle(bodyA, bodyB, triangleA, triangleB);
                if (report.isHit() && report.getPenetrationDepth() > best.getPenetrationDepth()) {
                    best = report;
                }
            }
        }
        return best;
    }

    private static CollisionReport detectTriangleTriangle(
        PhysicsBody bodyA,
        PhysicsBody bodyB,
        Triangle2 triangleA,
        Triangle2 triangleB
    ) {
        Vector2[] axes = buildAxes(triangleA, triangleB);
        double smallestOverlap = Double.POSITIVE_INFINITY;
        Vector2 bestAxis = Vector2.ZERO;

        for (Vector2 axis : axes) {
            if (axis.isZero()) {
                continue;
            }

            Projection projectionA = project(triangleA.getVertices(), axis);
            Projection projectionB = project(triangleB.getVertices(), axis);
            double overlap = projectionA.overlap(projectionB);
            if (overlap <= 0.0) {
                return CollisionReport.noHit();
            }

            if (overlap < smallestOverlap) {
                smallestOverlap = overlap;
                bestAxis = axis.normalized();
            }
        }

        Vector2 centroidA = triangleA.getCentroid();
        Vector2 centroidB = triangleB.getCentroid();
        Vector2 direction = centroidB.subtract(centroidA);
        if (direction.dot(bestAxis) < 0.0) {
            bestAxis = bestAxis.multiply(-1.0);
        }

        Vector2 contactPosition = centroidA.lerp(centroidB, 0.5);
        return createHitReport(bodyA, bodyB, bestAxis, contactPosition, smallestOverlap);
    }

    private static CollisionReport createHitReport(
        PhysicsBody bodyA,
        PhysicsBody bodyB,
        Vector2 normal,
        Vector2 contactPosition,
        double penetrationDepth
    ) {
        ContactPoint contactPoint = new ContactPoint(contactPosition, normal, -penetrationDepth);
        ContactManifold manifold = new ContactManifold(contactPoint, normal, penetrationDepth);
        return new CollisionReport(
            true,
            bodyA,
            bodyB,
            manifold,
            bodyA.isSensor() || bodyB.isSensor(),
            0.0
        );
    }

    private static RaycastHit intersectRaySegment(
        PhysicsBody body,
        Vector2 origin,
        Vector2 direction,
        double maxDistance,
        Vector2 start,
        Vector2 end
    ) {
        Vector2 segment = end.subtract(start);
        double denominator = direction.cross(segment);
        if (Math.abs(denominator) < EPSILON) {
            return RaycastHit.noHit();
        }

        Vector2 difference = start.subtract(origin);
        double t = difference.cross(segment) / denominator;
        double u = difference.cross(direction) / denominator;
        if (t < 0.0 || t > maxDistance || u < 0.0 || u > 1.0) {
            return RaycastHit.noHit();
        }

        Vector2 point = origin.add(direction.multiply(t));
        Vector2 normal = segment.perpendicularLeft().normalized();
        if (normal.dot(direction) > 0.0) {
            normal = normal.multiply(-1.0);
        }
        return new RaycastHit(true, body, point, normal, t);
    }

    private static Bounds2 transformBounds(Bounds2 localBounds, Transform2 transform) {
        Vector2 bottomLeft = transform.apply(new Vector2(localBounds.getMinX(), localBounds.getMinY()));
        Vector2 bottomRight = transform.apply(new Vector2(localBounds.getMaxX(), localBounds.getMinY()));
        Vector2 topRight = transform.apply(new Vector2(localBounds.getMaxX(), localBounds.getMaxY()));
        Vector2 topLeft = transform.apply(new Vector2(localBounds.getMinX(), localBounds.getMaxY()));

        double minX = Math.min(Math.min(bottomLeft.getX(), bottomRight.getX()), Math.min(topRight.getX(), topLeft.getX()));
        double minY = Math.min(Math.min(bottomLeft.getY(), bottomRight.getY()), Math.min(topRight.getY(), topLeft.getY()));
        double maxX = Math.max(Math.max(bottomLeft.getX(), bottomRight.getX()), Math.max(topRight.getX(), topLeft.getX()));
        double maxY = Math.max(Math.max(bottomLeft.getY(), bottomRight.getY()), Math.max(topRight.getY(), topLeft.getY()));
        return new Bounds2(minX, minY, maxX, maxY);
    }

    private static double scaledCircleRadius(CircleForm circleForm, Transform2 transform) {
        double scale = Math.max(Math.abs(transform.getScaleX()), Math.abs(transform.getScaleY()));
        return circleForm.getCircle().getRadius() * (scale == 0.0 ? 1.0 : scale);
    }

    private static Vector2[] buildAxes(Triangle2 triangleA, Triangle2 triangleB) {
        return new Vector2[] {
            edgeNormal(triangleA.getA(), triangleA.getB()),
            edgeNormal(triangleA.getB(), triangleA.getC()),
            edgeNormal(triangleA.getC(), triangleA.getA()),
            edgeNormal(triangleB.getA(), triangleB.getB()),
            edgeNormal(triangleB.getB(), triangleB.getC()),
            edgeNormal(triangleB.getC(), triangleB.getA())
        };
    }

    private static Vector2 edgeNormal(Vector2 start, Vector2 end) {
        return end.subtract(start).perpendicularLeft().normalized();
    }

    private static Projection project(List<Vector2> points, Vector2 axis) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Vector2 point : points) {
            double projected = point.dot(axis);
            min = Math.min(min, projected);
            max = Math.max(max, projected);
        }
        return new Projection(min, max);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class Projection {

        private final double min;
        private final double max;

        private Projection(double min, double max) {
            this.min = min;
            this.max = max;
        }

        private double overlap(Projection other) {
            return Math.min(this.max, other.max) - Math.max(this.min, other.min);
        }
    }
}
