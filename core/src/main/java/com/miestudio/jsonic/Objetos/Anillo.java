package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Anillo extends Objetos {
    private Animation<TextureRegion> animacion;
    private float tiempoEstado;

    public Anillo(float x, float y, Animation<TextureRegion> animacion) {
        super(x, y, animacion.getKeyFrame(0));
        this.animacion = animacion;
        this.hitbox = new Rectangle(x, y, 15f, 15f);
        this.tiempoEstado = 0;
    }

    @Override
    public void actualizar(float delta) {
        if (activo) {
            tiempoEstado += delta;
            textura = animacion.getKeyFrame(tiempoEstado, true);
        }
    }
}
