package com.miestudio.jsonic.Server.network;

/**
 * Paquete de red utilizado para comunicar que un jugador ha pasado el ratón
 * sobre un personaje en la pantalla de selección.
 */
public class PlayerHoverPacket {
    public int playerId; /** El ID del jugador que está haciendo hover. */
    public int hoveredCharacterIndex; /** El índice del personaje sobre el que se está haciendo hover. */
}
