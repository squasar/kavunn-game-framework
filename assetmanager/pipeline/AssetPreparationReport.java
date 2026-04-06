package assetmanager.pipeline;

import assetmanager.catalog.AssetKey;

public final class AssetPreparationReport {

    private final AssetKey bundleKey;
    private final int preparedFrames;
    private final long elapsedNanos;

    public AssetPreparationReport(AssetKey bundleKey, int preparedFrames, long elapsedNanos) {
        this.bundleKey = bundleKey;
        this.preparedFrames = preparedFrames;
        this.elapsedNanos = elapsedNanos;
    }

    public AssetKey getBundleKey() {
        return this.bundleKey;
    }

    public int getPreparedFrames() {
        return this.preparedFrames;
    }

    public long getElapsedNanos() {
        return this.elapsedNanos;
    }
}
