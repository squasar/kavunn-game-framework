package physics.palette;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PaletteExtractor {

    private PaletteExtractor() {
    }

    public static PaletteSet extract(BufferedImage image, int alphaThreshold, int maxColors) {
        if (image == null) {
            throw new IllegalArgumentException("Palette extraction requires an image.");
        }

        Map<Integer, long[]> buckets = new HashMap<>();
        long total = 0L;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha < alphaThreshold) {
                    continue;
                }

                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;
                int key = ((red >> 3) << 10) | ((green >> 3) << 5) | (blue >> 3);

                long[] bucket = buckets.computeIfAbsent(key, ignored -> new long[4]);
                bucket[0] += red;
                bucket[1] += green;
                bucket[2] += blue;
                bucket[3] += 1;
                total++;
            }
        }

        if (total == 0L) {
            return new PaletteSet(List.of(new PaletteSwatch("primary", new PhysicsColor(255, 255, 255, 255), 1.0)));
        }

        List<Map.Entry<Integer, long[]>> sorted = new ArrayList<>(buckets.entrySet());
        sorted.sort(Comparator.comparingLong((Map.Entry<Integer, long[]> entry) -> entry.getValue()[3]).reversed());

        String[] roles = {"primary", "secondary", "accent", "support"};
        List<PaletteSwatch> swatches = new ArrayList<>();
        int safeMaxColors = Math.max(1, maxColors);
        for (int index = 0; index < Math.min(safeMaxColors, sorted.size()); index++) {
            long[] bucket = sorted.get(index).getValue();
            long count = Math.max(1L, bucket[3]);
            PhysicsColor color = new PhysicsColor(
                (int) (bucket[0] / count),
                (int) (bucket[1] / count),
                (int) (bucket[2] / count),
                255
            );
            double weight = count / (double) total;
            String role = roles[Math.min(index, roles.length - 1)];
            swatches.add(new PaletteSwatch(role, color, weight));
        }

        return new PaletteSet(swatches);
    }
}
