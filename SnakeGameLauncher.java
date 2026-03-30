package com.example;

import java.awt.Point;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.example.core.Context;

public final class SnakeGameLauncher {

    private SnakeGameLauncher() {
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            SnakeWorld world = new SnakeWorld(20, 20);
            Context context = GameContextFactory.create(world);
            SnakeGamePanel panel = new SnakeGamePanel(context, world);

            JFrame frame = new JFrame("Snake Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }

    public static String runSmokeTest() {
        SnakeWorld world = new SnakeWorld(12, 12, new Random(7L));
        Context context = GameContextFactory.create(world);

        context.queueCommand(new DirectionCommand(world.getSnake(), Direction.DOWN));
        context.executeQueuedCommands();

        for (int step = 0; step < 5; step++) {
            context.queueCommand(world);
            context.executeQueuedCommands();
        }

        Point head = world.getSnake().getHead();
        return "Snake smoke test passed: head="
            + head.x
            + ","
            + head.y
            + " score="
            + world.getScore()
            + " length="
            + world.getSnake().getLength()
            + " gameOver="
            + world.isGameOver();
    }
}
