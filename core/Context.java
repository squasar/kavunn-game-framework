package com.example.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;



//This class is the main interface of the API we develop for Entities and their management capabilities as implemented below

public class Context {

    private Relation state = null;
    private List<Relation> entities = new ArrayList<>();
    private EntitiesGraph graph = new EntitiesGraph();
    private Deque<Relation> commandQueue = new ArrayDeque<>(); // For queued commands
    private Deque<Relation> commandHistory = new ArrayDeque<>(); // For undo functionality

    public void setState(Relation state){
        this.state = state;
    }

    public List<Relation> getEntities(){
        return this.entities;
    }

    public void action(){
        this.state.execute();
    }

    public EntitiesGraph getGraph(){
        return this.graph;
    }



    // Execute a single command
    public void executeCommand(Relation command) {
        command.execute();
        commandHistory.push(command); // Save command for potential undo
    }

    // Queue a command for later execution
    public void queueCommand(Relation command) {
        commandQueue.offer(command);
    }

    // Execute all queued commands
    public void executeQueuedCommands() {
        while (!commandQueue.isEmpty()) {
            Relation command = commandQueue.poll();
            executeCommand(command); // Execute and track for undo
        }
    }


}
