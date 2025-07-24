package com.miestudio.jsonic.Server.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    private final long sequenceNumber;
    private final ArrayList<PlayerState> players;
    private final List<ObjectState> objects;

    public GameState(ArrayList<PlayerState> players, List<ObjectState> objects, long sequenceNumber) {
        this.players = players;
        this.objects = objects;
        this.sequenceNumber = sequenceNumber;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public ArrayList<PlayerState> getPlayers() {
        return players;
    }

    public List<ObjectState> getObjects() {
        return objects;
    }
}
