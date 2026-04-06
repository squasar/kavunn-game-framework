package physics.geometry;

import java.util.Objects;

public final class Transform2 {

    public static final Transform2 IDENTITY = new Transform2(Vector2.ZERO, 0.0, 1.0, 1.0);

    private final Vector2 translation;
    private final double rotationRadians;
    private final double scaleX;
    private final double scaleY;

    public Transform2(Vector2 translation, double rotationRadians, double scaleX, double scaleY) {
        this.translation = Objects.requireNonNull(translation, "Transform translation cannot be null.");
        this.rotationRadians = rotationRadians;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public static Transform2 identity() {
        return IDENTITY;
    }

    public static Transform2 translation(double x, double y) {
        return new Transform2(new Vector2(x, y), 0.0, 1.0, 1.0);
    }

    public Vector2 getTranslation() {
        return this.translation;
    }

    public double getRotationRadians() {
        return this.rotationRadians;
    }

    public double getScaleX() {
        return this.scaleX;
    }

    public double getScaleY() {
        return this.scaleY;
    }

    public Transform2 withTranslation(Vector2 translation) {
        return new Transform2(translation, this.rotationRadians, this.scaleX, this.scaleY);
    }

    public Transform2 withRotation(double rotationRadians) {
        return new Transform2(this.translation, rotationRadians, this.scaleX, this.scaleY);
    }

    public Transform2 withScale(double scaleX, double scaleY) {
        return new Transform2(this.translation, this.rotationRadians, scaleX, scaleY);
    }

    public Transform2 translate(Vector2 offset) {
        return withTranslation(this.translation.add(offset));
    }

    public Transform2 rotate(double radians) {
        return withRotation(this.rotationRadians + radians);
    }

    public Transform2 scale(double scaleX, double scaleY) {
        return withScale(this.scaleX * scaleX, this.scaleY * scaleY);
    }

    public Vector2 apply(Vector2 point) {
        Objects.requireNonNull(point, "Transform application requires a point.");
        Vector2 scaled = new Vector2(point.getX() * this.scaleX, point.getY() * this.scaleY);
        return scaled.rotate(this.rotationRadians).add(this.translation);
    }

    public Vector2 applyDirection(Vector2 direction) {
        Objects.requireNonNull(direction, "Transform direction application requires a vector.");
        Vector2 scaled = new Vector2(direction.getX() * this.scaleX, direction.getY() * this.scaleY);
        return scaled.rotate(this.rotationRadians);
    }

    public Vector2 inverseApply(Vector2 point) {
        Objects.requireNonNull(point, "Inverse transform requires a point.");
        Vector2 translated = point.subtract(this.translation).rotate(-this.rotationRadians);
        double safeScaleX = this.scaleX == 0.0 ? 1.0 : this.scaleX;
        double safeScaleY = this.scaleY == 0.0 ? 1.0 : this.scaleY;
        return new Vector2(translated.getX() / safeScaleX, translated.getY() / safeScaleY);
    }
}
