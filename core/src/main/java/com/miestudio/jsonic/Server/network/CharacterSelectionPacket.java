package com.miestudio.jsonic.Server.network;

import java.io.Serializable;

/**
 * Paquete de red utilizado para comunicar la seleccion de personaje de un jugador.
 * Implementa {@link Serializable} para poder ser enviado a traves de la red.
 */
public class CharacterSelectionPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public int playerId; /** El ID del jugador que seleccionó el personaje. */
    public String characterType; /** El tipo de personaje seleccionado (ej. "Sonic", "Tails"). */

    /**
     * Constructor para crear un nuevo CharacterSelectionPacket.
     * @param playerId El ID del jugador.
     * @param characterType El tipo de personaje seleccionado.
     */
    public CharacterSelectionPacket(int playerId, String characterType) {
        this.playerId = playerId;
        this.characterType = characterType;
    }
}
