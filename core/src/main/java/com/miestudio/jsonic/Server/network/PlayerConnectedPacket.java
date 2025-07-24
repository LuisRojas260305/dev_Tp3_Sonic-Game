package com.miestudio.jsonic.server.network;

import java.io.Serializable;

/**
 * Paquete de red utilizado para notificar que un nuevo jugador se ha conectado al servidor.
 * Implementa {@link Serializable} para poder ser enviado a traves de la red.
 */
public class PlayerConnectedPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public int playerId; /** El ID del jugador que se ha conectado. */

    /**
     * Constructor para crear un nuevo PlayerConnectedPacket.
     * @param playerId El ID del jugador conectado.
     */
    public PlayerConnectedPacket(int playerId) {
        this.playerId = playerId;
    }
}