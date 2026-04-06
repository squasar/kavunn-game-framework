package ashwake;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import core.Context;
import render.DefaultRenderingPipeline;
import render.NoOpRenderBackend;
import render.Universe;

public final class AshwakeLauncher {

    private AshwakeLauncher() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--smoke-test".equalsIgnoreCase(args[0])) {
            System.out.println(runSmokeTest());
            return;
        }

        if (args.length > 0 && "--benchmark".equalsIgnoreCase(args[0])) {
            if (args.length > 1) {
                System.out.println(AshwakeBenchmark.runScenarioByName(args[1]));
            } else {
                System.out.println(AshwakeBenchmark.runSuite());
            }
            return;
        }

        launch();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            AshwakeRunWorld world = new AshwakeRunWorld();
            Context context = AshwakeContextFactory.create(world);
            Universe universe = new Universe(9400, "ashwake-universe");
            DefaultRenderingPipeline pipeline = AshwakeRenderFactory.createJava2DPipeline();
            AshwakeGamePanel panel = new AshwakeGamePanel(context, world, universe, pipeline);

            JFrame frame = new JFrame("Ashwake");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }

    public static String runSmokeTest() {
        AshwakeRunWorld world = new AshwakeRunWorld(new java.util.Random(17L));
        Context context = AshwakeContextFactory.create(world);
        Universe universe = new Universe(9400, "ashwake-universe");
        DefaultRenderingPipeline pipeline = AshwakeRenderFactory.createJava2DPipeline();

        for (int tick = 0; tick < 420; tick++) {
            double moveX = tick % 90 < 45 ? 1.0 : -1.0;
            double moveY = tick % 120 < 60 ? 0.0 : 1.0;
            context.queueCommand(new AshwakeMoveCommand(world, moveX, moveY));
            context.queueCommand(new AshwakeFireCommand(world, 0.0, -1.0));
            if (tick % 100 == 0) {
                context.queueCommand(new AshwakeDashCommand(world));
            }
            if (tick % 140 == 0) {
                context.queueCommand(new AshwakeSecondaryCommand(world));
            }
            context.queueCommand(world);
            context.executeQueuedCommands();
        }

        pipeline.render(universe, world, NoOpRenderBackend.INSTANCE);

        return "Ashwake smoke test passed: room="
            + world.getCurrentRoomNumber()
            + "/"
            + world.getTotalRooms()
            + " kills="
            + world.getKills()
            + " pickups="
            + world.getEssence()
            + " enemies="
            + world.getRoomWorld().getEnemies().size()
            + " projectiles="
            + world.getRoomWorld().getProjectiles().size()
            + " modifiers="
            + world.getActiveModifiers().size()
            + " runOver="
            + world.isRunOver()
            + " victory="
            + world.isVictory();
    }
}
