package com.miestudio.jsonic.Server.network;

import java.io.Serializable;

/**
 * Paquete de red utilizado para notificar que un anillo ha sido recolectado.
 */
public class RingCollectedPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public int ringId; /** El ID del anillo recolectado. */
    public int collectingPlayerId; /** El ID del jugador que recolectó el anillo. */

    public RingCollectedPacket(int ringId, int collectingPlayerId) {
        this.ringId = ringId;
        this.collectingPlayerId = collectingPlayerId;
    }
}