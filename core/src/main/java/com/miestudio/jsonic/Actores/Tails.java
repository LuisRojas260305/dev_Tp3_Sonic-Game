package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Representa al personaje Tails en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas para Tails.
 */
public class Tails extends Personajes {
    private TextureAtlas AtlasTails; /** Atlas de texturas específico para Tails. */

    /**
     * Constructor para el personaje Tails.
     *
     * @param playerId El ID del jugador asociado a este Tails.
     * @param atlas    El TextureAtlas que contiene las texturas de Tails.
     */
    public Tails(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        this.AtlasTails = atlas;
        cargarAnimaciones();
        currentAnimation = idleAnimation;
    }

    /**
     * Carga todas las animaciones específicas de Tails desde su TextureAtlas.
     */
    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            idleFrames.add(AtlasTails.findRegion("TailsIdle" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            runFrames.add(AtlasTails.findRegion("TailsRun" + i));
        }

        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 2; i++) {
            ballFrames.add(AtlasTails.findRegion("TailsHit" + i));
        }

        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            jumpFrames.add(AtlasTails.findRegion("TailsJump" + i));
        }

        jumpAnimation = new Animation<>(0.25f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    /**
     * Libera los recursos específicos de Tails.
     * En este caso, el TextureAtlas se gestiona centralmente en la clase Assets, por lo que no se libera aquí.
     */
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }

    /**
     * Implementación de la habilidad especial de Tails.
     * Actualmente no implementada.
     */
    @Override
    public void useAbility() {
        // Habilidad no implementada aún
    }
}
