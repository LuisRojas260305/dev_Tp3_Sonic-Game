package com.miestudio.jsonic.Sistemas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.Constantes;

public class VisualPollutionSystem {
    private TextureRegion texturaHumo;
    private Texture humoTexture; // Añadimos una referencia a la Texture
    private float tiempoAcumulado = 0;

    public VisualPollutionSystem() {
        crearTexturas();
    }

    private void crearTexturas() {
        // Crear textura de humo procedural
        texturaHumo = crearTexturaHumo(32, 32);
    }

    private TextureRegion crearTexturaHumo(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 0.5f); // Blanco semitransparente
        pixmap.fillCircle(width / 2, height / 2, width / 2 - 1); // Un círculo para simular una nube
        humoTexture = new Texture(pixmap); // Creamos la Texture
        pixmap.dispose(); // Liberamos el Pixmap
        return new TextureRegion(humoTexture); // Creamos el TextureRegion a partir de la Texture
    }

    public void dibujarEfectos(SpriteBatch batch, Array<PollutionSystem.PuntoContaminacion> puntos) {
        batch.begin();
        tiempoAcumulado += Gdx.graphics.getDeltaTime();

        Array<PollutionSystem.PuntoContaminacion> puntosCopia = new Array<>(puntos);
        for (PollutionSystem.PuntoContaminacion punto : puntosCopia) {
            // Solo dibujar humo en puntos de origen (nivel 3)
            if (punto.nivel == 3) {
                dibujarHumo(batch, punto);
            }
        }

        batch.end();
    }

    private void dibujarHumo(SpriteBatch batch, PollutionSystem.PuntoContaminacion punto) {
        float x = punto.tileX * Constantes.TILE_SIZE;
        float y = punto.tileY * Constantes.TILE_SIZE;

        // Animación de flotación
        float offsetY = MathUtils.sin(tiempoAcumulado * 2 + punto.tileX) * 3;
        float alpha = 0.6f + MathUtils.sin(tiempoAcumulado * 3) * 0.2f;

        // Tamaño variable
        float scale = 1.2f + MathUtils.sin(tiempoAcumulado * 1.5f) * 0.3f;
        float size = Constantes.TILE_SIZE * scale;

        // Color basado en el nivel de contaminación
        Color smokeColor = PollutionSystem.getColorForLevel(punto.nivel).cpy();
        smokeColor.a = alpha;

        batch.setColor(smokeColor);
        batch.draw(
            texturaHumo,
            x - (size - Constantes.TILE_SIZE) / 2,
            y - (size - Constantes.TILE_SIZE) / 2 + offsetY,
            size,
            size
        );
        batch.setColor(Color.WHITE);
    }

    public void actualizar(float delta) {
        // Actualizar lógica de efectos si es necesario
    }

    public void dispose() {
        if (humoTexture != null) {
            humoTexture.dispose();
        }
    }
}
