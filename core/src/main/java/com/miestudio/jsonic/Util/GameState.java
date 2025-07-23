package com.miestudio.jsonic.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el estado completo del juego en un momento específico,
 * incluyendo tanto el estado de los jugadores como el estado de la contaminación.
 * Esta clase es serializable para poder ser enviada desde el servidor a los clientes.
 */
public class GameState implements Serializable {

    /**
     * Número de versión para la serialización. Incrementado a 2L debido a la adición
     * del estado de contaminación. Esto asegura compatibilidad entre versiones.
     */
    private static final long serialVersionUID = 1L;

    /** La lista de los estados de cada jugador en la partida. */
    private List<PlayerState> players;

    

    /** Número de secuencia para la versión del estado del juego. */
    private long sequenceNumber;

    /**
     * Constructor para el estado del juego.
     *
     * @param players Lista de estados de los jugadores
     * @param corruptionStates Lista de estados de contaminación (puede ser null)
     * @param sequenceNumber Número de secuencia de esta actualización del estado del juego
     */
    public GameState(List<PlayerState> players, long sequenceNumber) {
        this.players = players;
        
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Constructor para compatibilidad con versiones anteriores.
     *
     * @param players Lista de estados de los jugadores
     */
    public GameState(List<PlayerState> players) {
        this(players, 0);
    }

    public List<PlayerState> getPlayers() {
        return players;
    }

    

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    
}
