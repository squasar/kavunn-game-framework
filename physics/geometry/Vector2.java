package physics.geometry;

import java.util.Objects;

public final class Vector2 {

    public static final Vector2 ZERO = new Vector2(0.0, 0.0);
    public static final Vector2 UNIT_X = new Vector2(1.0, 0.0);
    public static final Vector2 UNIT_Y = new Vector2(0.0, 1.0);

    private final double x;
    private final double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public Vector2 add(Vector2 other) {
        Objects.requireNonNull(other, "Vector addition requires a second vector.");
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        Objects.requireNonNull(other, "Vector subtraction requires a second vector.");
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 multiply(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    public Vector2 divide(double scalar) {
        if (scalar == 0.0) {
            throw new IllegalArgumentException("A vector cannot be divided by zero.");
        }
        return new Vector2(this.x / scalar, this.y / scalar);
    }

    public double dot(Vector2 other) {
        Objects.requireNonNull(other, "Dot product requires a second vector.");
        return this.x * other.x + this.y * other.y;
    }

    public double cross(Vector2 other) {
        Objects.requireNonNull(other, "Cross product requires a second vector.");
        return this.x * other.y - this.y * other.x;
    }

    public double lengthSquared() {
        return this.dot(this);
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public Vector2 normalized() {
        double length = length();
        if (length == 0.0) {
            return ZERO;
        }
        return divide(length);
    }

    public double distanceSquared(Vector2 other) {
        return subtract(other).lengthSquared();
    }

    public double distanceTo(Vector2 other) {
        return Math.sqrt(distanceSquared(other));
    }

    public Vector2 lerp(Vector2 other, double alpha) {
        return new Vector2(
            this.x + ((other.x - this.x) * alpha),
            this.y + ((other.y - this.y) * alpha)
        );
    }

    public Vector2 perpendicularLeft() {
        return new Vector2(-this.y, this.x);
    }

    public Vector2 perpendicularRight() {
        return new Vector2(this.y, -this.x);
    }

    public Vector2 rotate(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vector2(this.x * cos - this.y * sin, this.x * sin + this.y * cos);
    }

    public boolean isZero() {
        return this.x == 0.0 && this.y == 0.0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vector2 vector)) {
            return false;
        }
        return Double.compare(vector.x, x) == 0 && Double.compare(vector.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    @Override
    public String toString() {
        return "Vector2{x=" + this.x + ", y=" + this.y + "}";
    }
}
