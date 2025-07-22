/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 *
 * @author usuario
 */
public class Efecto extends Objetos {
    private Animation<TextureRegion> animacion;
    private float tiempoEstado;
    private float duracion;

    public Efecto(float x, float y, Animation<TextureRegion> animacion) {
        super(x, y, animacion.getKeyFrame(0));
        this.animacion = animacion;
        this.duracion = animacion.getAnimationDuration();
        this.hitbox = new Rectangle(x, y, 15f, 15f); // Sin hitbox
    }

    @Override
    public void actualizar(float delta) {
        tiempoEstado += delta;
        textura = animacion.getKeyFrame(tiempoEstado, false);
        
        if (tiempoEstado >= duracion) {
            activo = false;
        }
    }
}
