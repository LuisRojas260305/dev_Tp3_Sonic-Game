package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScrollingBackground {
    private final Texture texture;
    private final float speed;
    private float x1, x2;
    private float renderHeight;

    public ScrollingBackground(String path, float speed) {
        this.texture = new Texture(Gdx.files.internal(path));
        this.speed = speed;
        updateRenderHeight();
        reset();
    }

    // Actualiza la altura de renderizado para que coincida con la pantalla
    public void updateRenderHeight() {
        renderHeight = Gdx.graphics.getHeight();
    }

    public void reset() {
        this.x1 = 0;
        this.x2 = getRenderWidth(); // Usar el ancho calculado
    }

    // Calcula el ancho manteniendo la relación de aspecto
    private float getRenderWidth() {
        float aspectRatio = (float) texture.getWidth() / texture.getHeight();
        return renderHeight * aspectRatio;
    }

    public void update(float delta) {
        x1 -= speed * delta;
        x2 -= speed * delta;

        // Reiniciar posición cuando una textura sale completamente de la pantalla
        float renderWidth = getRenderWidth();
        if (x1 + renderWidth <= 0) {
            x1 = x2 + renderWidth;
        }
        if (x2 + renderWidth <= 0) {
            x2 = x1 + renderWidth;
        }
    }

    public void render(SpriteBatch batch) {
        float renderWidth = getRenderWidth();

        // Dibujar las dos copias del fondo con la altura exacta de la pantalla
        batch.draw(texture, x1, 0, renderWidth, renderHeight);
        batch.draw(texture, x2, 0, renderWidth, renderHeight);
    }

    public void dispose() {
        texture.dispose();
    }
}
