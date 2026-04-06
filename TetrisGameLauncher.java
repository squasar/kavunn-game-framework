<<<<<<< HEAD
package com.example;

=======
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

<<<<<<< HEAD
import com.example.core.Context;
=======
import core.Context;
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)

public final class TetrisGameLauncher {

    private TetrisGameLauncher() {
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            TetrisWorld world = new TetrisWorld();
            Context context = GameContextFactory.create(world);
            TetrisGamePanel panel = new TetrisGamePanel(context, world);

            JFrame frame = new JFrame("Blockfall");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }

    public static String runSmokeTest() {
        TetrisWorld world = new TetrisWorld(new Random(11L));
        Context context = GameContextFactory.create(world);

        context.queueCommand(new TetrisActionCommand(world, TetrisAction.MOVE_LEFT));
        context.queueCommand(new TetrisActionCommand(world, TetrisAction.ROTATE_CW));
        context.queueCommand(new TetrisActionCommand(world, TetrisAction.HARD_DROP));
        context.executeQueuedCommands();

        for (int step = 0; step < 4; step++) {
            context.queueCommand(world);
            context.executeQueuedCommands();
        }

        return "Blockfall smoke test passed: score="
            + world.getScore()
            + " lines="
            + world.getLinesCleared()
            + " level="
            + world.getLevel()
            + " gameOver="
            + world.isGameOver()
            + " next="
            + world.getNextType().name();
    }
}
