package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public abstract class Objetos {
    protected int id;
    public float x;
    public float y;
    protected TextureRegion textura;
    protected Rectangle hitbox;
    protected boolean activo = true;

    public Objetos(float x, float y, TextureRegion textura) {
        this.x = x;
        this.y = y;
        this.textura = textura;
        this.hitbox = new Rectangle(x, y, 0.5f, 0.5f);
    }

    public abstract void actualizar(float delta);

    public void renderizar(SpriteBatch batch) {
        if (activo) {
            batch.draw(textura, x, y, hitbox.width, hitbox.height);
        }
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public TextureRegion getTexture() {
        return textura;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
