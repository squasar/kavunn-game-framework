<<<<<<< HEAD
package com.example;

=======
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "--smoke-test".equalsIgnoreCase(args[0])) {
            if (args.length > 1 && "snake".equalsIgnoreCase(args[1])) {
                System.out.println(SnakeGameLauncher.runSmokeTest());
                return;
            }

            if (args.length > 1 && isBlockfall(args[1])) {
                System.out.println(TetrisGameLauncher.runSmokeTest());
                return;
            }

            System.out.println(SnakeGameLauncher.runSmokeTest());
            System.out.println(TetrisGameLauncher.runSmokeTest());
            return;
        }

        if (args.length > 0 && "snake".equalsIgnoreCase(args[0])) {
            SnakeGameLauncher.launch();
            return;
        }

        if (args.length > 0 && isBlockfall(args[0])) {
            TetrisGameLauncher.launch();
            return;
        }

        TetrisGameLauncher.launch();
    }

    private static boolean isBlockfall(String value) {
        return "tetris".equalsIgnoreCase(value) || "blockfall".equalsIgnoreCase(value);
    }
}
