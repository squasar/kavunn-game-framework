package physics.palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PaletteSet {

    private final List<PaletteSwatch> swatches;

    public PaletteSet(List<PaletteSwatch> swatches) {
        this.swatches = Collections.unmodifiableList(new ArrayList<>(swatches));
    }

    public List<PaletteSwatch> getSwatches() {
        return this.swatches;
    }

    public PaletteSwatch getPrimary() {
        return getByIndex(0);
    }

    public PaletteSwatch getSecondary() {
        return getByIndex(1);
    }

    public PaletteSwatch getAccent() {
        return getByIndex(2);
    }

    private PaletteSwatch getByIndex(int index) {
        if (this.swatches.isEmpty()) {
            return new PaletteSwatch("empty", new PhysicsColor(255, 255, 255, 255), 0.0);
        }
        return this.swatches.get(Math.min(index, this.swatches.size() - 1));
    }
}
