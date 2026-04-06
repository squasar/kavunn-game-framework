package assetmanager.source;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public final class PathImageAssetSource implements ImageAssetSource {

    private final Path path;
    private final String sourceLabel;
    private volatile BufferedImage cachedImage;

    public PathImageAssetSource(Path path) {
        this(path, null);
    }

    public PathImageAssetSource(Path path, String sourceLabel) {
        if (path == null) {
            throw new IllegalArgumentException("A path image asset source requires a path.");
        }
        this.path = path;
        this.sourceLabel = sourceLabel == null || sourceLabel.isBlank() ? path.toString() : sourceLabel.trim();
    }

    @Override
    public String getSourceLabel() {
        return this.sourceLabel;
    }

    @Override
    public BufferedImage loadImage() {
        BufferedImage cached = this.cachedImage;
        if (cached != null) {
            return cached;
        }
        if (!Files.exists(this.path)) {
            throw new IllegalStateException("Image asset path does not exist: " + this.path);
        }
        try {
            BufferedImage image = ImageIO.read(this.path.toFile());
            if (image == null) {
                throw new IllegalStateException("Unsupported image format for asset source: " + this.path);
            }
            this.cachedImage = image;
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read image asset: " + this.path, exception);
        }
    }
}
