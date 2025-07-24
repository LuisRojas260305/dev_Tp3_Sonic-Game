package com.miestudio.jsonic.Server.domain;

import java.io.Serializable;

/**
 * Representa el estado de los inputs de un cliente en un momento dado.
 * Esta clase es serializable para ser enviada desde el cliente al servidor.
 */
public class InputState implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean up, down, left, right, ability; /** Estado de los botones de direccion y habilidad. */
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
     * Verifica si el boton 'arriba' esta presionado.
     * @return true si el boton 'arriba' esta presionado, false en caso contrario.
     */
    public boolean isUp() { return up; }
    /**
     * Establece el estado del boton 'arriba'.
     * @param up true si el boton 'arriba' esta presionado, false en caso contrario.
     */
    public void setUp(boolean up) { this.up = up; }

    /**
     * Verifica si el boton 'abajo' esta presionado.
     * @return true si el boton 'abajo' esta presionado, false en caso contrario.
     */
    public boolean isDown() { return down; }
    /**
     * Establece el estado del boton 'abajo'.
     * @param down true si el boton 'abajo' esta presionado, false en caso contrario.
     */
    public void setDown(boolean down) { this.down = down; }

    /**
     * Verifica si el boton 'izquierda' esta presionado.
     * @return true si el boton 'izquierda' esta presionado, false en caso contrario.
     */
    public boolean isLeft() { return left; }
    /**
     * Establece el estado del boton 'izquierda'.
     * @param left true si el boton 'izquierda' esta presionado, false en caso contrario.
     */
    public void setLeft(boolean left) { this.left = left; }

    /**
     * Verifica si el boton 'derecha' esta presionado.
     * @return true si el boton 'derecha' esta presionado, false en caso contrario.
     */
    public boolean isRight() { return right; }
    /**
     * Establece el estado del boton 'derecha'.
     * @param right true si el boton 'derecha' esta presionado, false en caso contrario.
     */
    public void setRight(boolean right) { this.right = right; }

    /**
     * Verifica si el boton de 'habilidad' esta presionado.
     * @return true si el boton de 'habilidad' esta presionado, false en caso contrario.
     */
    public boolean isAbility() { return ability; }
    /**
     * Establece el estado del boton de 'habilidad'.
     * @param ability true si el boton de 'habilidad' esta presionado, false en caso contrario.
     */
    public void setAbility(boolean ability) { this.ability = ability; }
}
