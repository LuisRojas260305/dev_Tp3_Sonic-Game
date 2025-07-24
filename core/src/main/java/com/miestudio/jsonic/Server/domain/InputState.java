package com.miestudio.jsonic.Server.domain;

import java.io.Serializable;

/**
 * Representa el estado de los inputs de un cliente en un momento dado.
 * Esta clase es serializable para ser enviada desde el cliente al servidor.
 */
public class InputState implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean up, down, left, right, ability; /** Estado de los botones de dirección y habilidad. */
    private int playerId; /** ID del jugador al que pertenecen estos inputs. */

    /**
     * Obtiene el ID del jugador.
     * @return El ID del jugador.
     */
    public int getPlayerId() { return playerId; }
    /**
     * Establece el ID del jugador.
     * @param playerId El ID del jugador.
     */
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    /**
     * Verifica si el botón 'arriba' está presionado.
     * @return true si el botón 'arriba' está presionado, false en caso contrario.
     */
    public boolean isUp() { return up; }
    /**
     * Establece el estado del botón 'arriba'.
     * @param up true si el botón 'arriba' está presionado, false en caso contrario.
     */
    public void setUp(boolean up) { this.up = up; }

    /**
     * Verifica si el botón 'abajo' está presionado.
     * @return true si el botón 'abajo' está presionado, false en caso contrario.
     */
    public boolean isDown() { return down; }
    /**
     * Establece el estado del botón 'abajo'.
     * @param down true si el botón 'abajo' está presionado, false en caso contrario.
     */
    public void setDown(boolean down) { this.down = down; }

    /**
     * Verifica si el botón 'izquierda' está presionado.
     * @return true si el botón 'izquierda' está presionado, false en caso contrario.
     */
    public boolean isLeft() { return left; }
    /**
     * Establece el estado del botón 'izquierda'.
     * @param left true si el botón 'izquierda' está presionado, false en caso contrario.
     */
    public void setLeft(boolean left) { this.left = left; }

    /**
     * Verifica si el botón 'derecha' está presionado.
     * @return true si el botón 'derecha' está presionado, false en caso contrario.
     */
    public boolean isRight() { return right; }
    /**
     * Establece el estado del botón 'derecha'.
     * @param right true si el botón 'derecha' está presionado, false en caso contrario.
     */
    public void setRight(boolean right) { this.right = right; }

    /**
     * Verifica si el botón de 'habilidad' está presionado.
     * @return true si el botón de 'habilidad' está presionado, false en caso contrario.
     */
    public boolean isAbility() { return ability; }
    /**
     * Establece el estado del botón de 'habilidad'.
     * @param ability true si el botón de 'habilidad' está presionado, false en caso contrario.
     */
    public void setAbility(boolean ability) { this.ability = ability; }
}
