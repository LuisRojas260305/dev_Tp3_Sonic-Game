package com.miestudio.jsonic.Server.network;

import java.io.Serializable;

public class CharacterTakenPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public String characterType;
    public boolean taken;

    public CharacterTakenPacket(String characterType, boolean taken) {
        this.characterType = characterType;
        this.taken = taken;
    }
}