package com.example.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Every entity is consisted of its attributes along by also providing functionalities that are based on its relationships which can only be the kind of either Association or Relation. Also, an entity can be compound of multiple defined entities.

//The idea descripted above is implemented below.

public class Entity<T extends Relation> extends Params implements Association, Relation {

    private int id=-1;
    private String label="";
    private List<T> children = new ArrayList<>();

    public Entity(int id, String label){
        super(label, PrimaryTypeValue.integer(id));
        this.id = id;
        this.label=label;
    }

    public void addChildEntity(T entity) {
        children.add(entity);
    }

    public void removeChildEntity(T entity) {
        children.remove(entity);
    }

    public List<T> getChildren() {
        return children;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void update() {
        //It will be handled via sub classes
        //It implements the composite pattern
        //Thus, via this method, could easily manage the the game world and the world objects aka states
    }

    @Override
    public void execute() {
        //It will be handled via sub classes
        //It implements command pattern
        //Thus, via this method, it could easily manage the game objects and their interactions according to the updated states
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Entity<?> entity)) {
            return false;
        }
        return id == entity.id && Objects.equals(label, entity.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }

    @Override
    public String toString() {
        return "Entity{id=" + id + ", label='" + label + "'}";
    }


}
