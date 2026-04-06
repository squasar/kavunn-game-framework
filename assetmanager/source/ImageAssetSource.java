package assetmanager.source;

import java.awt.image.BufferedImage;

public interface ImageAssetSource {

    String getSourceLabel();

    BufferedImage loadImage();
}
