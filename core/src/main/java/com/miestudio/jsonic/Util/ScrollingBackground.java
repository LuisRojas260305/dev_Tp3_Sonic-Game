package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScrollingBackground {
    private final Texture texture;
    private final float speed;
    private float x1, x2;
    private float renderHeight;
    private float renderWidth;

    public ScrollingBackground(String path, float speed) {
        this.texture = new Texture(Gdx.files.internal(path));
        this.speed = speed;
        updateRenderSize();
        reset();
    }

    // Actualiza el tamaño de renderizado cuando cambia el tamaño de la pantalla
    public void updateRenderSize() {
        renderHeight = Gdx.graphics.getHeight();
        float aspectRatio = (float) texture.getWidth() / texture.getHeight();
        renderWidth = renderHeight * aspectRatio;
    }

    public void reset() {
        this.x1 = 0;
        this.x2 = renderWidth; // Usar el ancho calculado
    }

    public void update(float delta) {
        x1 -= speed * delta;
        x2 -= speed * delta;

        // Reiniciar posición cuando una textura sale completamente de la pantalla
        if (x1 + renderWidth <= 0) {
            x1 = x2 + renderWidth;
        }
        if (x2 + renderWidth <= 0) {
            x2 = x1 + renderWidth;
        }
    }

    public void render(SpriteBatch batch) {
        // Dibujar las dos copias del fondo con el tamaño calculado
        batch.draw(texture, x1, 0, renderWidth, renderHeight);
        batch.draw(texture, x2, 0, renderWidth, renderHeight);
    }

    public void dispose() {
        texture.dispose();
    }
}
