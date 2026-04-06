package physics.palette;

public final class PaletteSwatch {

    private final String role;
    private final PhysicsColor color;
    private final double weight;

    public PaletteSwatch(String role, PhysicsColor color, double weight) {
        this.role = role;
        this.color = color;
        this.weight = weight;
    }

    public String getRole() {
        return this.role;
    }

    public PhysicsColor getColor() {
        return this.color;
    }

    public double getWeight() {
        return this.weight;
    }
}
