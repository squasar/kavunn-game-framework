package assetmanager.catalog;

import assetmanager.mesh.EntityMeshSequence;
import assetmanager.mesh.MeshPreparationOptions;
import assetmanager.source.EntityImageSequence;

public final class EntityAssetBundle {

    private final AssetKey key;
    private final EntityImageSequence imageSequence;
    private final MeshPreparationOptions preparationOptions;
    private EntityMeshSequence preparedMeshes;

    public EntityAssetBundle(AssetKey key, EntityImageSequence imageSequence) {
        this(key, imageSequence, MeshPreparationOptions.defaults());
    }

    public EntityAssetBundle(AssetKey key, EntityImageSequence imageSequence, MeshPreparationOptions preparationOptions) {
        if (key == null) {
            throw new IllegalArgumentException("An entity asset bundle requires a key.");
        }
        if (imageSequence == null) {
            throw new IllegalArgumentException("An entity asset bundle requires an image sequence.");
        }
        this.key = key;
        this.imageSequence = imageSequence;
        this.preparationOptions = preparationOptions == null ? MeshPreparationOptions.defaults() : preparationOptions;
    }

    public AssetKey getKey() {
        return this.key;
    }

    public EntityImageSequence getImageSequence() {
        return this.imageSequence;
    }

    public MeshPreparationOptions getPreparationOptions() {
        return this.preparationOptions;
    }

    public boolean isPrepared() {
        return this.preparedMeshes != null;
    }

    public EntityMeshSequence getPreparedMeshes() {
        return this.preparedMeshes;
    }

    public void setPreparedMeshes(EntityMeshSequence preparedMeshes) {
        this.preparedMeshes = preparedMeshes;
    }
}
