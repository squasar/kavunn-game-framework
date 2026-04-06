package assetmanager.opengl;

import assetmanager.catalog.AssetCatalog;
import assetmanager.catalog.AssetKey;

public final class OpenGLDraftLibrary {

    private final AssetKey key;
    private final AssetCatalog<OpenGLPaletteDraft> palettes = new AssetCatalog<>();
    private final AssetCatalog<OpenGLFontDraft> fonts = new AssetCatalog<>();
    private final AssetCatalog<OpenGLShaderDraft> shaders = new AssetCatalog<>();
    private final AssetCatalog<OpenGLTextureDraft> textures = new AssetCatalog<>();
    private final AssetCatalog<OpenGLMaterialDraft> materials = new AssetCatalog<>();
    private final AssetCatalog<OpenGLUiModuleDraft> uiModules = new AssetCatalog<>();

    public OpenGLDraftLibrary(AssetKey key) {
        if (key == null) {
            throw new IllegalArgumentException("An OpenGL draft library requires a key.");
        }
        this.key = key;
    }

    public AssetKey getKey() {
        return this.key;
    }

    public AssetCatalog<OpenGLPaletteDraft> getPalettes() {
        return this.palettes;
    }

    public AssetCatalog<OpenGLFontDraft> getFonts() {
        return this.fonts;
    }

    public AssetCatalog<OpenGLShaderDraft> getShaders() {
        return this.shaders;
    }

    public AssetCatalog<OpenGLTextureDraft> getTextures() {
        return this.textures;
    }

    public AssetCatalog<OpenGLMaterialDraft> getMaterials() {
        return this.materials;
    }

    public AssetCatalog<OpenGLUiModuleDraft> getUiModules() {
        return this.uiModules;
    }

    public void registerPalette(OpenGLPaletteDraft paletteDraft) {
        this.palettes.register(paletteDraft.getKey(), paletteDraft);
    }

    public void registerFont(OpenGLFontDraft fontDraft) {
        this.fonts.register(fontDraft.getKey(), fontDraft);
    }

    public void registerShader(OpenGLShaderDraft shaderDraft) {
        this.shaders.register(shaderDraft.getKey(), shaderDraft);
    }

    public void registerTexture(OpenGLTextureDraft textureDraft) {
        this.textures.register(textureDraft.getKey(), textureDraft);
    }

    public void registerMaterial(OpenGLMaterialDraft materialDraft) {
        this.materials.register(materialDraft.getKey(), materialDraft);
    }

    public void registerUiModule(OpenGLUiModuleDraft uiModuleDraft) {
        this.uiModules.register(uiModuleDraft.getKey(), uiModuleDraft);
    }
}
