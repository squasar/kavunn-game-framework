package assetmanager.opengl;

import assetmanager.catalog.AssetKey;
import assetmanager.font.FontStyle;
import physics.palette.PhysicsColor;

public final class OpenGLFontDraft {

    private final AssetKey key;
    private final String family;
    private final FontStyle style;
    private final int pointSize;
    private final double lineHeight;
    private final PhysicsColor baseColor;
    private final boolean antialias;
    private final String glyphSet;

    public OpenGLFontDraft(
        AssetKey key,
        String family,
        FontStyle style,
        int pointSize,
        double lineHeight,
        PhysicsColor baseColor,
        boolean antialias,
        String glyphSet
    ) {
        if (key == null) {
            throw new IllegalArgumentException("An OpenGL font draft requires a key.");
        }
        this.key = key;
        this.family = family == null || family.isBlank() ? "Serif" : family.trim();
        this.style = style == null ? FontStyle.REGULAR : style;
        this.pointSize = Math.max(1, pointSize);
        this.lineHeight = Math.max(0.5, lineHeight);
        this.baseColor = baseColor == null ? new PhysicsColor(255, 255, 255, 255) : baseColor;
        this.antialias = antialias;
        this.glyphSet = glyphSet == null || glyphSet.isBlank()
            ? "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            : glyphSet;
    }

    public AssetKey getKey() {
        return this.key;
    }

    public String getFamily() {
        return this.family;
    }

    public FontStyle getStyle() {
        return this.style;
    }

    public int getPointSize() {
        return this.pointSize;
    }

    public double getLineHeight() {
        return this.lineHeight;
    }

    public PhysicsColor getBaseColor() {
        return this.baseColor;
    }

    public boolean isAntialias() {
        return this.antialias;
    }

    public String getGlyphSet() {
        return this.glyphSet;
    }
}
