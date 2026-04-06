package assetmanager.mesh;

import java.util.ArrayList;
import java.util.List;

import assetmanager.catalog.AssetKey;

public final class EntityMeshSequence {

    private final AssetKey entityKey;
    private final List<MeshedImageFrame> frames;

    public EntityMeshSequence(AssetKey entityKey, List<MeshedImageFrame> frames) {
        if (entityKey == null) {
            throw new IllegalArgumentException("An entity mesh sequence requires an asset key.");
        }
        this.entityKey = entityKey;
        this.frames = List.copyOf(frames);
    }

    public AssetKey getEntityKey() {
        return this.entityKey;
    }

    public List<MeshedImageFrame> getFrames() {
        return this.frames;
    }

    public List<MeshedImageFrame> getFramesForClip(String clipName) {
        String safeClip = clipName == null || clipName.isBlank() ? "default" : clipName.trim();
        List<MeshedImageFrame> matching = new ArrayList<>();
        for (MeshedImageFrame frame : this.frames) {
            if (safeClip.equals(frame.getSourceFrame().getClipName())) {
                matching.add(frame);
            }
        }
        return matching;
    }

    public boolean isEmpty() {
        return this.frames.isEmpty();
    }

    public int size() {
        return this.frames.size();
    }
}
