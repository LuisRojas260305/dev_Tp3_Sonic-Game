package com.miestudio.jsonic.Server.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {

    public enum GameStatus {
        PLAYING, WON, LOST
    }

    private final long sequenceNumber;
    private final ArrayList<PlayerState> players;
    private final List<ObjectState> objects;
    private final float gameTimeRemaining;
    private final GameStatus gameStatus;

    public GameState(ArrayList<PlayerState> players, List<ObjectState> objects, long sequenceNumber, float gameTimeRemaining, GameStatus gameStatus) {
        this.players = players;
        this.objects = objects;
        this.sequenceNumber = sequenceNumber;
        this.gameTimeRemaining = gameTimeRemaining;
        this.gameStatus = gameStatus;
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

    public float getGameTimeRemaining() {
        return gameTimeRemaining;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }
}
