package physics.palette;

public final class PhysicsColor {

    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    public PhysicsColor(int red, int green, int blue, int alpha) {
        this.red = clamp(red);
        this.green = clamp(green);
        this.blue = clamp(blue);
        this.alpha = clamp(alpha);
    }

    public static PhysicsColor fromArgb(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        return new PhysicsColor(red, green, blue, alpha);
    }

    public int getRed() {
        return this.red;
    }

    public int getGreen() {
        return this.green;
    }

    public int getBlue() {
        return this.blue;
    }

    public int getAlpha() {
        return this.alpha;
    }

    public int toArgb() {
        return (this.alpha << 24) | (this.red << 16) | (this.green << 8) | this.blue;
    }

    public PhysicsColor blend(PhysicsColor other, double alpha) {
        double clamped = Math.max(0.0, Math.min(1.0, alpha));
        return new PhysicsColor(
            (int) Math.round(this.red + ((other.red - this.red) * clamped)),
            (int) Math.round(this.green + ((other.green - this.green) * clamped)),
            (int) Math.round(this.blue + ((other.blue - this.blue) * clamped)),
            (int) Math.round(this.alpha + ((other.alpha - this.alpha) * clamped))
        );
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
