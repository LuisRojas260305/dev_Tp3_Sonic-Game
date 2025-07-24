package com.miestudio.jsonic.Server.network;

import java.io.Serializable;

/**
 * Paquete de red utilizado para notificar que un personaje ha sido tomado o liberado.
 * Implementa {@link Serializable} para poder ser enviado a través de la red.
 */
public class CharacterTakenPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    public String characterType; /** El tipo de personaje afectado (ej. "Sonic", "Tails"). */
    public boolean taken; /** true si el personaje ha sido tomado, false si ha sido liberado. */

    /**
     * Constructor para crear un nuevo CharacterTakenPacket.
     * @param characterType El tipo de personaje.
     * @param taken true si el personaje está tomado, false si está disponible.
     */
    public CharacterTakenPacket(String characterType, boolean taken) {
        this.characterType = characterType;
        this.taken = taken;
    }
}