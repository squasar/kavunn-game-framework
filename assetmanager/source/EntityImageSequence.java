package assetmanager.source;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import assetmanager.catalog.AssetKey;

public final class EntityImageSequence {

    private static final Comparator<ImageFrame> FRAME_ORDER =
        Comparator.comparing(ImageFrame::getClipName)
            .thenComparingInt(ImageFrame::getOrderIndex)
            .thenComparing(ImageFrame::getFrameId);

    private final AssetKey entityKey;
    private final List<ImageFrame> frames = new ArrayList<>();

    public EntityImageSequence(AssetKey entityKey) {
        if (entityKey == null) {
            throw new IllegalArgumentException("An entity image sequence requires an asset key.");
        }
        this.entityKey = entityKey;
    }

    public AssetKey getEntityKey() {
        return this.entityKey;
    }

    public void addFrame(ImageFrame frame) {
        if (frame == null) {
            throw new IllegalArgumentException("An entity image sequence cannot contain a null frame.");
        }
        this.frames.add(frame);
    }

    public void addFrame(String frameId, String clipName, int orderIndex, long durationMillis, ImageAssetSource source) {
        addFrame(new ImageFrame(frameId, clipName, orderIndex, durationMillis, source));
    }

    public List<ImageFrame> getFrames() {
        return List.copyOf(this.frames);
    }

    public List<ImageFrame> getFramesInPreparationOrder() {
        List<ImageFrame> ordered = new ArrayList<>(this.frames);
        ordered.sort(FRAME_ORDER);
        return ordered;
    }

    public List<ImageFrame> getFramesForClip(String clipName) {
        String safeClip = clipName == null || clipName.isBlank() ? "default" : clipName.trim();
        List<ImageFrame> matching = new ArrayList<>();
        for (ImageFrame frame : this.frames) {
            if (safeClip.equals(frame.getClipName())) {
                matching.add(frame);
            }
        }
        matching.sort(FRAME_ORDER);
        return matching;
    }

    public Set<String> getClipNames() {
        Set<String> clipNames = new LinkedHashSet<>();
        for (ImageFrame frame : this.frames) {
            clipNames.add(frame.getClipName());
        }
        return clipNames;
    }

    public boolean isEmpty() {
        return this.frames.isEmpty();
    }

    public int size() {
        return this.frames.size();
    }
}
