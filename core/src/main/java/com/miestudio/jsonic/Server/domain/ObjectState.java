package com.miestudio.jsonic.Server.domain;

import java.io.Serializable;

public class ObjectState implements Serializable {
    private static final long serialVersionUID = 1l;
    private int id;
    private float x;
    private float y;
    private boolean active;
    private String type;
    private int totalCollectedTrash; // Solo para MaquinaReciclaje

    public ObjectState(int id, float x, float y, boolean active, String type, int totalCollectedTrash){
        this.id = id;
        this.x = x;
        this.y = y;
        this.active = active;
        this.type = type;
        this.totalCollectedTrash = totalCollectedTrash;
    }

    public int getId() { return id; }
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return active; }
    public String getType() { return type; }
    public int getTotalCollectedTrash() { return totalCollectedTrash; }
}
