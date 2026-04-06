package assetmanager.mesh;

import physics.importing.RasterShapeOptions;

public final class MeshPreparationOptions {

    private final RasterShapeOptions rasterShapeOptions;
    private final boolean normalizeToCenter;

    public MeshPreparationOptions(RasterShapeOptions rasterShapeOptions, boolean normalizeToCenter) {
        this.rasterShapeOptions = rasterShapeOptions == null ? RasterShapeOptions.defaults() : rasterShapeOptions;
        this.normalizeToCenter = normalizeToCenter;
    }

    public static MeshPreparationOptions defaults() {
        return new MeshPreparationOptions(RasterShapeOptions.defaults(), true);
    }

    public RasterShapeOptions getRasterShapeOptions() {
        return this.rasterShapeOptions;
    }

    public boolean isNormalizeToCenter() {
        return this.normalizeToCenter;
    }
}
