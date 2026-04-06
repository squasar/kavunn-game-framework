package assetmanager.opengl;

import java.util.List;

import assetmanager.catalog.AssetKey;

public final class OpenGLUiModuleDraft {

    private final AssetKey key;
    private final String moduleType;
    private final double designWidth;
    private final double designHeight;
    private final AssetKey paletteKey;
    private final AssetKey fontKey;
    private final List<OpenGLUiElementDraft> elements;

    public OpenGLUiModuleDraft(
        AssetKey key,
        String moduleType,
        double designWidth,
        double designHeight,
        AssetKey paletteKey,
        AssetKey fontKey,
        List<OpenGLUiElementDraft> elements
    ) {
        if (key == null) {
            throw new IllegalArgumentException("An OpenGL UI module draft requires a key.");
        }
        this.key = key;
        this.moduleType = moduleType == null || moduleType.isBlank() ? "module" : moduleType.trim();
        this.designWidth = Math.max(0.0, designWidth);
        this.designHeight = Math.max(0.0, designHeight);
        this.paletteKey = paletteKey;
        this.fontKey = fontKey;
        this.elements = List.copyOf(elements == null ? List.of() : elements);
    }

    public AssetKey getKey() {
        return this.key;
    }

    public String getModuleType() {
        return this.moduleType;
    }

    public double getDesignWidth() {
        return this.designWidth;
    }

    public double getDesignHeight() {
        return this.designHeight;
    }

    public AssetKey getPaletteKey() {
        return this.paletteKey;
    }

    public AssetKey getFontKey() {
        return this.fontKey;
    }

    public List<OpenGLUiElementDraft> getElements() {
        return this.elements;
    }
}
