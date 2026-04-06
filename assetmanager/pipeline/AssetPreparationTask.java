package assetmanager.pipeline;

import assetmanager.catalog.EntityAssetBundle;

@FunctionalInterface
public interface AssetPreparationTask {

    void prepare(EntityAssetBundle bundle);
}
