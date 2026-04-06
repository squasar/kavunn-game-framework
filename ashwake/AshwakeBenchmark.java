package ashwake;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import core.Context;
import render.DefaultRenderingPipeline;
import render.Java2DRenderBackend;
import render.Universe;

final class AshwakeBenchmark {

    private static final int WARMUP_FRAMES = 120;
    private static final int MEASURED_FRAMES = 720;

    private AshwakeBenchmark() {
    }

    public static String runSuite() {
        StringBuilder builder = new StringBuilder();
        builder.append("Ashwake benchmark suite").append(System.lineSeparator());

        for (AshwakeBenchmarkScenario scenario : AshwakeBenchmarkScenario.values()) {
            BenchmarkResult result = runScenario(scenario);
            if (builder.length() > 0) {
                builder.append(System.lineSeparator());
            }
            builder.append(result.format()).append(System.lineSeparator());
        }

        return builder.toString().trim();
    }

    public static String runScenarioByName(String scenarioName) {
        return runScenario(AshwakeBenchmarkScenario.parse(scenarioName)).format();
    }

    private static BenchmarkResult runScenario(AshwakeBenchmarkScenario scenario) {
        AshwakeRunWorld world = new AshwakeRunWorld(new Random(67L));
        world.prepareBenchmarkScenario(scenario);

        Context context = AshwakeContextFactory.create(world);
        Universe universe = new Universe(9700, "ashwake-benchmark-universe");
        DefaultRenderingPipeline pipeline = AshwakeRenderFactory.createJava2DPipeline();
        BufferedImage image = new BufferedImage(
            AshwakeRoomWorld.ROOM_WIDTH,
            AshwakeRoomWorld.ROOM_HEIGHT,
            BufferedImage.TYPE_INT_ARGB
        );

        for (int frame = 0; frame < WARMUP_FRAMES; frame++) {
            world.sustainBenchmarkScenario(scenario, frame);
            queueBenchmarkCommands(context, world, scenario, frame);
            context.queueCommand(world);
            context.executeQueuedCommands();
            renderJava2D(pipeline, universe, world, image);
        }

        long[] updateNanos = new long[MEASURED_FRAMES];
        long[] renderNanos = new long[MEASURED_FRAMES];
        int peakEnemies = 0;
        int peakProjectiles = 0;
        int peakHazards = 0;
        int peakPickups = 0;

        for (int frame = 0; frame < MEASURED_FRAMES; frame++) {
            world.sustainBenchmarkScenario(scenario, frame);

            long updateStart = System.nanoTime();
            queueBenchmarkCommands(context, world, scenario, frame);
            context.queueCommand(world);
            context.executeQueuedCommands();
            updateNanos[frame] = System.nanoTime() - updateStart;

            long renderStart = System.nanoTime();
            renderJava2D(pipeline, universe, world, image);
            renderNanos[frame] = System.nanoTime() - renderStart;

            peakEnemies = Math.max(peakEnemies, world.getRoomWorld().getEnemies().size());
            peakProjectiles = Math.max(peakProjectiles, world.getRoomWorld().getProjectiles().size());
            peakHazards = Math.max(peakHazards, world.getRoomWorld().getHazards().size());
            peakPickups = Math.max(peakPickups, world.getRoomWorld().getPickups().size());
        }

        return new BenchmarkResult(
            scenario,
            updateNanos,
            renderNanos,
            peakEnemies,
            peakProjectiles,
            peakHazards,
            peakPickups,
            world.isRunOver(),
            world.isVictory()
        );
    }

    private static void renderJava2D(
        DefaultRenderingPipeline pipeline,
        Universe universe,
        AshwakeRunWorld world,
        BufferedImage image
    ) {
        Graphics2D g2 = image.createGraphics();
        try {
            AshwakeRenderFactory.configureGraphics(g2);
            Java2DRenderBackend backend = new Java2DRenderBackend(g2);
            pipeline.render(universe, world, backend);
        } finally {
            g2.dispose();
        }
    }

    private static void queueBenchmarkCommands(
        Context context,
        AshwakeRunWorld world,
        AshwakeBenchmarkScenario scenario,
        int frame
    ) {
        double moveX = Math.sin(frame * 0.09);
        double moveY = Math.cos(frame * 0.07);
        double aimX = Math.cos(frame * 0.11);
        double aimY = Math.sin(frame * 0.11 - 0.6);

        switch (scenario) {
            case PROJECTILE_HELL -> {
                moveX += Math.sin(frame * 0.17) * 0.5;
                aimX = Math.cos(frame * 0.18);
                aimY = Math.sin(frame * 0.18);
            }
            case BOSS_ARENA -> {
                moveX = Math.sin(frame * 0.05);
                moveY = Math.sin(frame * 0.10) * 0.55;
                aimX = Math.cos(frame * 0.08);
                aimY = -0.8 + Math.sin(frame * 0.06) * 0.25;
            }
            case REWARD_CHAMBER -> {
                moveX *= 0.45;
                moveY *= 0.35;
                aimX = 0.0;
                aimY = -1.0;
            }
        }

        context.queueCommand(new AshwakeMoveCommand(world, moveX, moveY));

        if (scenario != AshwakeBenchmarkScenario.REWARD_CHAMBER || frame % 24 == 0) {
            context.queueCommand(new AshwakeFireCommand(world, aimX, aimY));
        }
        if (frame % 90 == 0) {
            context.queueCommand(new AshwakeDashCommand(world));
        }
        if (frame % 150 == 0) {
            context.queueCommand(new AshwakeSecondaryCommand(world));
        }
        if (scenario == AshwakeBenchmarkScenario.REWARD_CHAMBER && world.isChoosingModifier()) {
            context.queueCommand(new AshwakeSelectModifierCommand(world, frame % 3));
        }
    }

    private static final class BenchmarkResult {

        private final AshwakeBenchmarkScenario scenario;
        private final long[] updateNanos;
        private final long[] renderNanos;
        private final int peakEnemies;
        private final int peakProjectiles;
        private final int peakHazards;
        private final int peakPickups;
        private final boolean runOver;
        private final boolean victory;

        private BenchmarkResult(
            AshwakeBenchmarkScenario scenario,
            long[] updateNanos,
            long[] renderNanos,
            int peakEnemies,
            int peakProjectiles,
            int peakHazards,
            int peakPickups,
            boolean runOver,
            boolean victory
        ) {
            this.scenario = scenario;
            this.updateNanos = updateNanos;
            this.renderNanos = renderNanos;
            this.peakEnemies = peakEnemies;
            this.peakProjectiles = peakProjectiles;
            this.peakHazards = peakHazards;
            this.peakPickups = peakPickups;
            this.runOver = runOver;
            this.victory = victory;
        }

        private String format() {
            return String.format(
                Locale.US,
                "%s%n  update avg %.3f ms  p95 %.3f ms  max %.3f ms%n  render avg %.3f ms  p95 %.3f ms  max %.3f ms%n  peaks enemies=%d projectiles=%d hazards=%d pickups=%d%n  outcome runOver=%s victory=%s",
                this.scenario.title(),
                nanosToMillis(average(this.updateNanos)),
                nanosToMillis(percentile(this.updateNanos, 0.95)),
                nanosToMillis(max(this.updateNanos)),
                nanosToMillis(average(this.renderNanos)),
                nanosToMillis(percentile(this.renderNanos, 0.95)),
                nanosToMillis(max(this.renderNanos)),
                this.peakEnemies,
                this.peakProjectiles,
                this.peakHazards,
                this.peakPickups,
                this.runOver,
                this.victory
            );
        }

        private static double average(long[] values) {
            long total = 0L;
            for (long value : values) {
                total += value;
            }
            return total / (double) values.length;
        }

        private static long percentile(long[] values, double ratio) {
            long[] copy = Arrays.copyOf(values, values.length);
            Arrays.sort(copy);
            int index = (int) Math.min(copy.length - 1, Math.ceil(copy.length * ratio) - 1);
            return copy[index];
        }

        private static long max(long[] values) {
            long maxValue = 0L;
            for (long value : values) {
                maxValue = Math.max(maxValue, value);
            }
            return maxValue;
        }

        private static double nanosToMillis(double nanos) {
            return nanos / 1_000_000.0;
        }
    }
}
