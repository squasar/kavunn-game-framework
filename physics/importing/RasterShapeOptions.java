package physics.importing;

public final class RasterShapeOptions {

    private final int alphaThreshold;
    private final int maxContourPoints;
    private final int contourSamples;
    private final int maxPaletteColors;

    public RasterShapeOptions(int alphaThreshold, int maxContourPoints, int contourSamples, int maxPaletteColors) {
        this.alphaThreshold = alphaThreshold;
        this.maxContourPoints = maxContourPoints;
        this.contourSamples = contourSamples;
        this.maxPaletteColors = maxPaletteColors;
    }

    public static RasterShapeOptions defaults() {
        return new RasterShapeOptions(20, 128, 64, 5);
    }

    public int getAlphaThreshold() {
        return this.alphaThreshold;
    }

    public int getMaxContourPoints() {
        return this.maxContourPoints;
    }

    public int getContourSamples() {
        return this.contourSamples;
    }

    public int getMaxPaletteColors() {
        return this.maxPaletteColors;
    }
}
