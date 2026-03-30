package com.example.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//EntityGraph is a bridge. Yet, this bridge is like a highway. It has several purposes, several achievements like any multi roaded highway is :)
//These purposes and how they are achieved are explained below:
/*
 *  1- It creates and manages the graph solutions for the Entities. That is the main responsibility of this class.
 *  This graph is specialized in such a way that can be managed very accordingly and error prof.
 * 
 *  2- It serves as an adapter between Entities by implementing Command pattern. In this way, 
 *  you can define multiple customized functions that can work altogether along any entity unless they are
 *  very well defined
 * 
 *  3- It gives home to any entity by implementing the Composite pattern. No matter how complicated the structure is,
 *  we firmly believe that you can find some hierarchy at some level. That's just the way it is when all the entites are used altogether without
 *  creating any imbalance only when a good graph structure is implemented.
 *  
 *  4- By providing these functionalities, this class is vitally important to create the Context of any given entity or entity structure.
 * 
 * 
 *  You can find the implementation of the ideas above on below:
 * 
 * 
 * 
 */

public class EntitiesGraph extends Invoker implements Association {

    
    private int group_id=-1;
    private String group_label="";
    
    @SuppressWarnings("rawtypes")
    private Map<Entity, List<Entity>> adjacentVertices = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public void addEntity(int entity_id, String entity_label){
        Entity entity0 = new Entity(entity_id, entity_label);
        this.adjacentVertices.putIfAbsent(entity0, new ArrayList<>());
    }

    @SuppressWarnings("rawtypes")
    public void removeEntity(int entity_id, String entity_label){
        Entity entity = new Entity(entity_id, entity_label);
        this.adjacentVertices.values().stream().forEach(e->e.remove(entity));
        this.adjacentVertices.remove(new Entity(entity_id,entity_label));
    }

    @SuppressWarnings("rawtypes")
    public void addRelation(int entity_id1, String entity_label1, int entity_id2, String entity_label2){
        Entity entity1 = new Entity(entity_id1, entity_label1);
        Entity entity2 = new Entity(entity_id2, entity_label2);
        this.adjacentVertices.get(entity1).add(entity2);
        this.adjacentVertices.get(entity2).add(entity1);
        this.addBroker(entity1);
        this.addBroker(entity2);
    }
    @SuppressWarnings("rawtypes")
    public void removeRelation(int entity_id1, String entity_label1, int entity_id2, String entity_label2){
        Entity entity1 = new Entity(entity_id1, entity_label1);
        Entity entity2 = new Entity(entity_id2, entity_label2);
        List<Entity> eL1 = this.adjacentVertices.get(entity1);
        List<Entity> eL2 = this.adjacentVertices.get(entity2);
        if(eL1 != null){
            eL1.remove(entity2);
            this.removeBroker(entity2);
        }
        if(eL2 != null){
            eL2.remove(entity1);
            this.removeBroker(entity1);
        }
    }
    @SuppressWarnings("rawtypes")
    public List<Entity> getAdjacentVertices(int id, String label){
        return this.adjacentVertices.get(new Entity(id,label));
    }

    @Override
    public String getLabel() {
        return this.group_label;
    }

    @Override
    public void setLabel(String label) {
        this.group_label = label;
    }

    @Override
    public int getId() {
        return this.group_id;
    }

    @Override
    public void setId(int id) {
        this.group_id = id;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void update() {
        for (Entity key_entity : adjacentVertices.keySet()) {
            key_entity.update();
        }
    }

}
