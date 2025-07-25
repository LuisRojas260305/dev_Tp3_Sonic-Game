package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Representa un objeto de anillo coleccionable en el juego.
 * Extiende la clase abstracta {@link Objetos} para su gestion.
 */
public class Anillo extends Objetos {
    private Animation<TextureRegion> animacion; /** Animacion del anillo. */
    private float tiempoEstado; /** Tiempo de estado actual de la animacion. */

    /**
     * Constructor para crear un nuevo objeto Anillo.
     * @param x Posicion X inicial del anillo.
     * @param y Posicion Y inicial del anillo.
     * @param animacion La animacion a utilizar para el anillo.
     */
    public Anillo(float x, float y, Animation<TextureRegion> animacion) {
        super(x, y, animacion.getKeyFrame(0));
        this.animacion = animacion;
        this.hitbox = new Rectangle(x, y, animacion.getKeyFrame(0).getRegionWidth(), animacion.getKeyFrame(0).getRegionHeight());
        this.tiempoEstado = 0;
    }

    /**
     * Actualiza el estado del anillo en cada fotograma.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos.
     */
    @Override
    public void actualizar(float delta) {
        if (activo) {
            tiempoEstado += delta;
            textura = animacion.getKeyFrame(tiempoEstado, true);
        }
    }
}
