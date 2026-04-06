package assetmanager.source;

import java.awt.image.BufferedImage;

public final class ImageFrame {

    private final String frameId;
    private final String clipName;
    private final int orderIndex;
    private final long durationMillis;
    private final ImageAssetSource source;

    public ImageFrame(String frameId, String clipName, int orderIndex, long durationMillis, ImageAssetSource source) {
        if (source == null) {
            throw new IllegalArgumentException("An image frame requires an image source.");
        }
        this.frameId = frameId == null || frameId.isBlank() ? "frame-" + orderIndex : frameId.trim();
        this.clipName = clipName == null || clipName.isBlank() ? "default" : clipName.trim();
        this.orderIndex = orderIndex;
        this.durationMillis = Math.max(0L, durationMillis);
        this.source = source;
    }

    public String getFrameId() {
        return this.frameId;
    }

    public String getClipName() {
        return this.clipName;
    }

    public int getOrderIndex() {
        return this.orderIndex;
    }

    public long getDurationMillis() {
        return this.durationMillis;
    }

    public ImageAssetSource getSource() {
        return this.source;
    }

    public BufferedImage loadImage() {
        return this.source.loadImage();
    }
}
