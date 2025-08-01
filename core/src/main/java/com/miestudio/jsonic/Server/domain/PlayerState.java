package com.miestudio.jsonic.Server.domain;

import com.miestudio.jsonic.Util.CollectibleType;

import java.io.Serializable;
import java.util.Map;

/**
 * Representa el estado de un unico jugador en un momento dado.
 * Esta clase es serializable para ser incluida en el {@link GameState} que se envia a traves de la red.
 */
public class PlayerState implements Serializable {

    /**
     * Numero de version para la serializacion.
     */
    private static final long serialVersionUID = 1L;

    private int playerId;
    /**
     * El ID unico del jugador.
     */
    private float x, y;
    /**
     * Coordenadas X e Y de la posicion del jugador.
     */
    private boolean facingRight;
    /**
     * Indica si el jugador esta mirando a la derecha.
     */
    private String currentAnimationName;
    /**
     * Nombre de la animacion actual del jugador (ej. "idle", "run", "jump").
     */
    private float animationStateTime;
    /**
     * Tiempo de estado de la animacion actual del jugador.
     */
    private String characterType; /** El tipo de personaje que el jugador está usando (ej. "Sonic", "Tails"). */

    private final Map<CollectibleType, Integer> collectibles;

    private boolean flying;
    private boolean isAvispa; /** Indica si este PlayerState representa una avispa. */
    private float targetX, targetY; /** Posición objetivo para la avispa (si es una avispa). */
    private boolean active; /** Indica si el personaje está activo (para avispas y otros personajes temporales). */
    private int lives; /** El número de vidas restantes del jugador. */
    /**
     * Constructor para crear un nuevo PlayerState.
     *
     * @param playerId             El ID unico del jugador.
     * @param x                    La coordenada X de la posicion del jugador.
     * @param y                    La coordenada Y de la posicion del jugador.
     * @param facingRight          true si el jugador esta mirando a la derecha, false en caso contrario.
     * @param currentAnimationName El nombre de la animacion actual del jugador.
     * @param animationStateTime   El tiempo de estado de la animacion actual del jugador.
     * @param characterType        El tipo de personaje que el jugador esta usando.
     */
    public PlayerState(int playerId, float x, float y, boolean facingRight, String currentAnimationName, float animationStateTime, String characterType, boolean flying, Map<CollectibleType, Integer> collectibles, boolean isAvispa, float targetX, float targetY, boolean active, int lives) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.currentAnimationName = currentAnimationName;
        this.animationStateTime = animationStateTime;
        this.characterType = characterType;
        this.flying = flying;
        this.collectibles = collectibles;
        this.isAvispa = isAvispa;
        this.targetX = targetX;
        this.targetY = targetY;
        this.active = active;
        this.lives = lives;
    }

    /**
     * Obtiene el ID unico del jugador.
     *
     * @return El ID del jugador.
     */
    public int getPlayerId() {
        return playerId;
    }

    public boolean isFlying() {
    return flying;
}
    
    /**
     * Obtiene la coordenada X de la posicion del jugador.
     *
     * @return La coordenada X.
     */
    public float getX() {
        return x;
    }

    /**
     * Obtiene la coordenada Y de la posicion del jugador.
     *
     * @return La coordenada Y.
     */
    public float getY() {
        return y;
    }

    /**
     * Verifica si el jugador esta mirando a la derecha.
     *
     * @return true si el jugador mira a la derecha, false en caso contrario.
     */
    public boolean isFacingRight() {
        return facingRight;
    }

    /**
     * Obtiene el nombre de la animacion actual del jugador.
     *
     * @return El nombre de la animacion actual.
     */
    public String getCurrentAnimationName() {
        return currentAnimationName;
    }

    /**
     * Obtiene el tiempo de estado de la animacion actual del jugador.
     *
     * @return El tiempo de estado de la animacion.
     */
    public float getAnimationStateTime() {
        return animationStateTime;
    }

    /**
     * Obtiene el tipo de personaje que el jugador está usando.
     *
     * @return El tipo de personaje.
     */
    public String getCharacterType() {
        return characterType;
    }

    public Map<CollectibleType, Integer> getCollectibles() {
        return collectibles;
    }

    public boolean isAvispa() {
        return isAvispa;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public boolean isActive() {
        return active;
    }

    public int getLives() {
        return lives;
    }
}
