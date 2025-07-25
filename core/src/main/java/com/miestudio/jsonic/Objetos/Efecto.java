/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Representa un efecto visual temporal en el juego, como una explosion o un destello.
 * Extiende la clase abstracta {@link Objetos} y se desactiva despues de su duracion.
 */
public class Efecto extends Objetos {
    private Animation<TextureRegion> animacion; /** Animacion del efecto. */
    private float tiempoEstado; /** Tiempo de estado actual de la animacion. */
    private float duracion; /** Duracion total de la animacion del efecto. */

    /**
     * Constructor para crear un nuevo objeto Efecto.
     * @param x Posicion X inicial del efecto.
     * @param y Posicion Y inicial del efecto.
     * @param animacion La animacion a utilizar para el efecto.
     */
    public Efecto(float x, float y, Animation<TextureRegion> animacion) {
        super(x, y, animacion.getKeyFrame(0));
        this.animacion = animacion;
        this.duracion = animacion.getAnimationDuration();
        this.hitbox = new Rectangle(x, y, animacion.getKeyFrame(0).getRegionWidth(), animacion.getKeyFrame(0).getRegionHeight());
    }

    /**
     * Actualiza el estado del efecto en cada fotograma.
     * El efecto se desactiva automaticamente una vez que su animacion ha terminado.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos.
     */
    @Override
    public void actualizar(float delta) {
        tiempoEstado += delta;
        textura = animacion.getKeyFrame(tiempoEstado, false);
        
        if (tiempoEstado >= duracion) {
            activo = false;
        }
    }
}
