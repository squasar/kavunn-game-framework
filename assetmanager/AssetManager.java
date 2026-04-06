package assetmanager;

import java.util.ArrayList;
import java.util.List;

import assetmanager.catalog.AssetCatalog;
import assetmanager.catalog.AssetKey;
import assetmanager.catalog.EntityAssetBundle;
import assetmanager.mesh.EntityMeshSequence;
import assetmanager.opengl.OpenGLDraftLibrary;
import assetmanager.pipeline.AssetPreparationPipeline;
import assetmanager.pipeline.AssetPreparationReport;
import assetmanager.pipeline.DefaultAssetPreparationPipeline;

public final class AssetManager {

    private final AssetCatalog<EntityAssetBundle> entityBundles = new AssetCatalog<>();
    private final AssetCatalog<OpenGLDraftLibrary> draftLibraries = new AssetCatalog<>();
    private final AssetPreparationPipeline preparationPipeline;

    public AssetManager() {
        this(new DefaultAssetPreparationPipeline());
    }

    public AssetManager(AssetPreparationPipeline preparationPipeline) {
        this.preparationPipeline = preparationPipeline == null ? new DefaultAssetPreparationPipeline() : preparationPipeline;
    }

    public AssetCatalog<EntityAssetBundle> getEntityBundles() {
        return this.entityBundles;
    }

    public AssetCatalog<OpenGLDraftLibrary> getDraftLibraries() {
        return this.draftLibraries;
    }

    public void registerEntityBundle(EntityAssetBundle bundle) {
        this.entityBundles.register(bundle.getKey(), bundle);
    }

    public void registerDraftLibrary(OpenGLDraftLibrary library) {
        this.draftLibraries.register(library.getKey(), library);
    }

    public AssetPreparationReport prepareEntityAssets(AssetKey key) {
        EntityAssetBundle bundle = this.entityBundles.require(key);
        return this.preparationPipeline.prepare(bundle);
    }

    public List<AssetPreparationReport> prepareAllEntityAssets() {
        List<AssetPreparationReport> reports = new ArrayList<>();
        for (EntityAssetBundle bundle : this.entityBundles.values()) {
            reports.add(this.preparationPipeline.prepare(bundle));
        }
        return reports;
    }

    public EntityMeshSequence requirePreparedMeshSequence(AssetKey key) {
        EntityAssetBundle bundle = this.entityBundles.require(key);
        if (!bundle.isPrepared()) {
            this.preparationPipeline.prepare(bundle);
        }
        return bundle.getPreparedMeshes();
    }

    public OpenGLDraftLibrary requireDraftLibrary(AssetKey key) {
        return this.draftLibraries.require(key);
    }
}
