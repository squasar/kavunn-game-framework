package com.example.core;

import java.util.ArrayList;
import java.util.List;


// An Invoker is an elegant way to define the commong ground of similar or same responsibilities. Be cautious, I meant responsibilities, not just functions in Command pattern.

public class Invoker {

    private List<Relation> relationList = new ArrayList<Relation>();

    public void addBroker(Relation relation){
        this.relationList.add(relation);
    }

    public void executeBroker(){
        for(Relation relation : this.relationList){
            relation.execute();
        }
    }

    public void removeBroker(Relation relation){
        this.relationList.remove(relation);
    }

}
