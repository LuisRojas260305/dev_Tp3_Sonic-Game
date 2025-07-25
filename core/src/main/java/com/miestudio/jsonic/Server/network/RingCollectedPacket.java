package com.miestudio.jsonic.Server.network;

import java.io.Serializable;

public class RingCollectedPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public int ringId;
    public int collectingPlayerId;

    public RingCollectedPacket(int ringId, int collectingPlayerId) {
        this.ringId = ringId;
        this.collectingPlayerId = collectingPlayerId;
    }
}