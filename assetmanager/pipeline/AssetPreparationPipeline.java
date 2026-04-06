package assetmanager.pipeline;

import java.util.List;

import assetmanager.catalog.EntityAssetBundle;

public interface AssetPreparationPipeline {

    AssetPreparationReport prepare(EntityAssetBundle bundle);

    List<AssetPreparationTask> getTasks();
}
