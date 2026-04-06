package assetmanager.pipeline;

import java.util.ArrayList;
import java.util.List;

import assetmanager.catalog.EntityAssetBundle;
import assetmanager.mesh.EntityMeshSequence;
import assetmanager.mesh.ImageMeshBuilder;

public final class DefaultAssetPreparationPipeline implements AssetPreparationPipeline {

    private final ImageMeshBuilder meshBuilder = new ImageMeshBuilder();
    private final List<AssetPreparationTask> tasks = new ArrayList<>();

    public DefaultAssetPreparationPipeline() {
        this.tasks.add(bundle -> {
            EntityMeshSequence meshes = this.meshBuilder.mesh(bundle.getImageSequence(), bundle.getPreparationOptions());
            bundle.setPreparedMeshes(meshes);
        });
    }

    @Override
    public AssetPreparationReport prepare(EntityAssetBundle bundle) {
        long started = System.nanoTime();
        for (AssetPreparationTask task : this.tasks) {
            task.prepare(bundle);
        }
        long elapsed = System.nanoTime() - started;
        int preparedFrames = bundle.getPreparedMeshes() == null ? 0 : bundle.getPreparedMeshes().size();
        return new AssetPreparationReport(bundle.getKey(), preparedFrames, elapsed);
    }

    @Override
    public List<AssetPreparationTask> getTasks() {
        return List.copyOf(this.tasks);
    }

    public void addTask(AssetPreparationTask task) {
        if (task == null) {
            throw new IllegalArgumentException("The asset preparation pipeline cannot add a null task.");
        }
        this.tasks.add(task);
    }
}
