package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Representa al personaje Robot en el juego, extendiendo las funcionalidades base de Personajes.
 * Actualmente, esta clase utiliza animaciones de placeholder y su habilidad especial no esta implementada.
 */
public class Robot extends Personajes {

    /**
     * Constructor para el personaje Robot.
     * @param playerId El ID del jugador asociado a este Robot.
     * @param atlas El TextureAtlas que contendrá las texturas del Robot (actualmente no utilizado para animaciones reales).
     */
    public Robot(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        // El atlas no se usa directamente aquí ya que las animaciones son placeholders.
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(0, 0);
    }

    /**
     * Carga las animaciones para el personaje Robot.
     * Actualmente, utiliza animaciones de placeholder.
     */
    private void cargarAnimaciones() {
        // Animaciones de placeholder para Robot
        Array<TextureRegion> idleFrames = new Array<>();
        // Aquí se añadirían los frames específicos del robot si estuvieran disponibles.
        // Por ahora, se usa un frame dummy.
        idleFrames.add(new TextureRegion()); // Reemplazar con textura real del robot
        idleAnimation = new Animation<>(0.1f, idleFrames, Animation.PlayMode.LOOP);

        runAnimation = idleAnimation; // Placeholder
        jumpAnimation = idleAnimation; // Placeholder
        rollAnimation = idleAnimation; // Placeholder
    }

    /**
     * Libera los recursos específicos del Robot.
     * Actualmente no hay recursos específicos que liberar directamente en esta clase.
     */
    @Override
    public void dispose() {
        // Disponer recursos si los hubiera
    }

    /**
     * Implementacion de la habilidad especial del Robot.
     * Actualmente no implementada.
     */
    @Override
    public void useAbility() {
        // Habilidad no implementada aún.
    }
}