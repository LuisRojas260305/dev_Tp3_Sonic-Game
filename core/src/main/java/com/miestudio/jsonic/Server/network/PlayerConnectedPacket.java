package com.miestudio.jsonic.server.network;

import java.io.Serializable;

public class PlayerConnectedPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public int playerId;

    public PlayerConnectedPacket(int playerId) {
        this.playerId = playerId;
    }
}