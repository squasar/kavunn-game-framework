package assetmanager.opengl;

import assetmanager.catalog.AssetKey;

public final class OpenGLTextureDraft {

    private final AssetKey key;
    private final AssetKey sourceBundleKey;
    private final boolean generateMipmaps;
    private final String minFilter;
    private final String magFilter;
    private final String wrapMode;

    public OpenGLTextureDraft(
        AssetKey key,
        AssetKey sourceBundleKey,
        boolean generateMipmaps,
        String minFilter,
        String magFilter,
        String wrapMode
    ) {
        if (key == null) {
            throw new IllegalArgumentException("An OpenGL texture draft requires a key.");
        }
        this.key = key;
        this.sourceBundleKey = sourceBundleKey;
        this.generateMipmaps = generateMipmaps;
        this.minFilter = minFilter == null || minFilter.isBlank() ? "linear" : minFilter.trim();
        this.magFilter = magFilter == null || magFilter.isBlank() ? "linear" : magFilter.trim();
        this.wrapMode = wrapMode == null || wrapMode.isBlank() ? "clamp-to-edge" : wrapMode.trim();
    }

    public AssetKey getKey() {
        return this.key;
    }

    public AssetKey getSourceBundleKey() {
        return this.sourceBundleKey;
    }

    public boolean isGenerateMipmaps() {
        return this.generateMipmaps;
    }

    public String getMinFilter() {
        return this.minFilter;
    }

    public String getMagFilter() {
        return this.magFilter;
    }

    public String getWrapMode() {
        return this.wrapMode;
    }
}
