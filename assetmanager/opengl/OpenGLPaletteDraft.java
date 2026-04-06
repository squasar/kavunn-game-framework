package assetmanager.opengl;

import assetmanager.catalog.AssetKey;
import physics.palette.PaletteSet;

public final class OpenGLPaletteDraft {

    private final AssetKey key;
    private final PaletteSet palette;

    public OpenGLPaletteDraft(AssetKey key, PaletteSet palette) {
        if (key == null) {
            throw new IllegalArgumentException("An OpenGL palette draft requires a key.");
        }
        if (palette == null) {
            throw new IllegalArgumentException("An OpenGL palette draft requires a palette.");
        }
        this.key = key;
        this.palette = palette;
    }

    public AssetKey getKey() {
        return this.key;
    }

    public PaletteSet getPalette() {
        return this.palette;
    }
}
