package com.miestudio.jsonic.Server.network;

import java.io.Serializable;

public class CharacterSelectionPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public int playerId;
    public String characterType;

    public CharacterSelectionPacket(int playerId, String characterType) {
        this.playerId = playerId;
        this.characterType = characterType;
    }
}
