<<<<<<< HEAD
package com.example;

import com.example.core.Relation;
=======
import core.Relation;
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)

public class RestartSnakeCommand implements Relation {

    private final SnakeWorld world;

    public RestartSnakeCommand(SnakeWorld world) {
        this.world = world;
    }

    @Override
    public void execute() {
        world.restart();
    }
}
