package assetmanager.opengl;

import assetmanager.catalog.AssetKey;

public final class OpenGLMaterialDraft {

    private final AssetKey key;
    private final AssetKey shaderKey;
    private final AssetKey textureKey;
    private final AssetKey paletteKey;
    private final OpenGLBlendMode blendMode;
    private final double opacity;

    public OpenGLMaterialDraft(
        AssetKey key,
        AssetKey shaderKey,
        AssetKey textureKey,
        AssetKey paletteKey,
        OpenGLBlendMode blendMode,
        double opacity
    ) {
        if (key == null) {
            throw new IllegalArgumentException("An OpenGL material draft requires a key.");
        }
        this.key = key;
        this.shaderKey = shaderKey;
        this.textureKey = textureKey;
        this.paletteKey = paletteKey;
        this.blendMode = blendMode == null ? OpenGLBlendMode.ALPHA_BLEND : blendMode;
        this.opacity = Math.max(0.0, Math.min(1.0, opacity));
    }

    public AssetKey getKey() {
        return this.key;
    }

    public AssetKey getShaderKey() {
        return this.shaderKey;
    }

    public AssetKey getTextureKey() {
        return this.textureKey;
    }

    public AssetKey getPaletteKey() {
        return this.paletteKey;
    }

    public OpenGLBlendMode getBlendMode() {
        return this.blendMode;
    }

    public double getOpacity() {
        return this.opacity;
    }
}
