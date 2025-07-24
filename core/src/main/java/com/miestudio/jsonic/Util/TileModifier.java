package com.miestudio.jsonic.Util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Random;

/**
 * Clase de utilidad para modificar visualmente los tiles de un mapa en tiempo de ejecucion.
 * Permite aplicar tintes y efectos de "suciedad" a los tiles.
 */
public class TileModifier {
    private static final ObjectMap<String, TextureRegion> cacheTexturas = new ObjectMap<>();
    /**
     * Cache para almacenar texturas de tiles modificadas y reutilizarlas.
     */
    private static final Random random = new Random(); /** Generador de numeros aleatorios para efectos visuales. */

    /**
     * Modifica visualmente un tile especifico en una capa del mapa, aplicando un tinte de color.
     * Si el tile modificado ya esta en cache, se reutiliza la textura.
     *
     * @param map       El mapa de tiles a modificar.
     * @param layerName El nombre de la capa donde se encuentra el tile.
     * @param tileX     La coordenada X del tile en la capa.
     * @param tileY     La coordenada Y del tile en la capa.
     * @param tint      El color de tinte a aplicar al tile.
     */
    public static void modificarTile(TiledMap map, String layerName, int tileX, int tileY, Color tint) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);

        if (cell != null && cell.getTile() != null) {
            // Generar una clave unica para la cache basada en el tile original y el tinte
            String cacheKey = cell.getTile().toString() + tint.toString();

            // Si la textura modificada no esta en cache, crearla y añadirla
            if (!cacheTexturas.containsKey(cacheKey)) {
                TextureRegion original = cell.getTile().getTextureRegion();
                TextureRegion modificada = tintTexture(original, tint);
                cacheTexturas.put(cacheKey, modificada);
            }

            // Establecer el nuevo tile modificado en la celda
            StaticTiledMapTile newTile = new StaticTiledMapTile(cacheTexturas.get(cacheKey));
            cell.setTile(newTile);
        }
    }

    /**
     * Aplica un tinte de color a una TextureRegion y añade un efecto de "suciedad".
     *
     * @param original La TextureRegion original a tintar.
     * @param tint     El color de tinte a aplicar.
     * @return Una nueva TextureRegion con el tinte y el efecto de suciedad aplicados.
     */
    private static TextureRegion tintTexture(TextureRegion original, Color tint) {
        // Obtener Pixmap de la textura original para manipulacion de pixeles
        Pixmap originalPixmap = new Pixmap(
            original.getRegionWidth(),
            original.getRegionHeight(),
            Pixmap.Format.RGBA8888
        );

        // Preparar y consumir el Pixmap de la textura original
        original.getTexture().getTextureData().prepare();
        Pixmap sourcePixmap = original.getTexture().getTextureData().consumePixmap();

        // Dibujar la región original en el nuevo Pixmap
        originalPixmap.drawPixmap(
            sourcePixmap,
            0, 0,
            original.getRegionX(), original.getRegionY(),
            original.getRegionWidth(), original.getRegionHeight()
        );

        // Aplicar tintado pixel por pixel
        for (int x = 0; x < originalPixmap.getWidth(); x++) {
            for (int y = 0; y < originalPixmap.getHeight(); y++) {
                Color color = new Color();
                Color.rgba8888ToColor(color, originalPixmap.getPixel(x, y));

                // Mezclar el color del pixel con el color de tinte
                color.mul(tint);

                originalPixmap.setColor(color);
                originalPixmap.drawPixel(x, y);
            }
        }

        // Añadir efecto de suciedad basado en el nivel de oscuridad del tinte
        addDirtEffect(originalPixmap, tint);

        // Crear una nueva Texture a partir del Pixmap modificado y liberar recursos
        Texture texture = new Texture(originalPixmap);
        originalPixmap.dispose();
        sourcePixmap.dispose();

        return new TextureRegion(texture);
    }

    /**
     * Añade un efecto de "suciedad" aleatorio a un Pixmap.
     * La densidad y el color de la suciedad varian segun el color base.
     *
     * @param pixmap    El Pixmap al que se le añadira el efecto de suciedad.
     * @param baseColor El color base utilizado para determinar la variacion de la suciedad.
     */
    private static void addDirtEffect(Pixmap pixmap, Color baseColor) {
        // Calcular la densidad de la suciedad (mas sucio cuanto mas oscuro sea el color base)
        int densidad = 15 + (int) ((1 - baseColor.r) * 30);

        for (int i = 0; i < densidad; i++) {
            // Generar coordenadas y tamaño aleatorios para la mancha de suciedad
            int x = random.nextInt(pixmap.getWidth());
            int y = random.nextInt(pixmap.getHeight());
            int size = 1 + random.nextInt(3);

            // Variar el color de la suciedad ligeramente respecto al color base
            Color dirtColor = varyColor(baseColor);
            pixmap.setColor(dirtColor);

            // Dibujar diferentes formas de suciedad (circulos o rectangulos) aleatoriamente
            if (random.nextFloat() > 0.7f) {
                pixmap.fillCircle(x, y, size);
            } else {
                pixmap.fillRectangle(x, y, size, size);
            }
        }
    }

    /**
     * Varia ligeramente un color dado para crear un efecto de suciedad mas natural.
     *
     * @param base El color base a variar.
     * @return Un nuevo Color con una ligera variacion.
     */
    private static Color varyColor(Color base) {
        float variation = 0.15f; // Magnitud de la variación
        // Aplicar variación aleatoria a los componentes RGB y asegurar que estén dentro del rango [0, 1]
        float r = clamp(base.r + (random.nextFloat() * 2 - 1) * variation);
        float g = clamp(base.g + (random.nextFloat() * 2 - 1) * variation);
        float b = clamp(base.b + (random.nextFloat() * 2 - 1) * variation);
        return new Color(r, g, b, base.a);
    }

    /**
     * Limita un valor flotante dentro del rango [0, 1].
     *
     * @param value El valor a limitar.
     * @return El valor limitado.
     */
    private static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }
}
