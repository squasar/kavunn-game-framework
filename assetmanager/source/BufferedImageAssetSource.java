package assetmanager.source;

import java.awt.image.BufferedImage;

public final class BufferedImageAssetSource implements ImageAssetSource {

    private final String sourceLabel;
    private final BufferedImage image;

    public BufferedImageAssetSource(String sourceLabel, BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("A buffered image asset source requires an image.");
        }
        this.sourceLabel = sourceLabel == null || sourceLabel.isBlank() ? "buffered-image" : sourceLabel.trim();
        this.image = image;
    }

    @Override
    public String getSourceLabel() {
        return this.sourceLabel;
    }

    @Override
    public BufferedImage loadImage() {
        return this.image;
    }
}
