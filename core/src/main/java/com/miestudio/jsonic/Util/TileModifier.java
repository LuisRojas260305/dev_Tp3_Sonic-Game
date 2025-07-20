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

public class TileModifier {
    private static final ObjectMap<String, TextureRegion> cacheTexturas = new ObjectMap<>();
    private static final Random random = new Random();

    public static void modificarTile(TiledMap map, String layerName, int tileX, int tileY, Color tint) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);

        if (cell != null && cell.getTile() != null) {
            String cacheKey = cell.getTile().toString() + tint.toString();

            if (!cacheTexturas.containsKey(cacheKey)) {
                TextureRegion original = cell.getTile().getTextureRegion();
                TextureRegion modificada = tintTexture(original, tint);
                cacheTexturas.put(cacheKey, modificada);
            }

            StaticTiledMapTile newTile = new StaticTiledMapTile(cacheTexturas.get(cacheKey));
            cell.setTile(newTile);
        }
    }

    private static TextureRegion tintTexture(TextureRegion original, Color tint) {
        // Obtener Pixmap de la textura original
        Pixmap originalPixmap = new Pixmap(
            original.getRegionWidth(),
            original.getRegionHeight(),
            Pixmap.Format.RGBA8888
        );

        original.getTexture().getTextureData().prepare();
        Pixmap sourcePixmap = original.getTexture().getTextureData().consumePixmap();

        originalPixmap.drawPixmap(
            sourcePixmap,
            0, 0,
            original.getRegionX(), original.getRegionY(),
            original.getRegionWidth(), original.getRegionHeight()
        );

        // Aplicar tintado
        for (int x = 0; x < originalPixmap.getWidth(); x++) {
            for (int y = 0; y < originalPixmap.getHeight(); y++) {
                Color color = new Color();
                Color.rgba8888ToColor(color, originalPixmap.getPixel(x, y));

                // Blend con el color de tintado
                color.mul(tint);

                originalPixmap.setColor(color);
                originalPixmap.drawPixel(x, y);
            }
        }

        // Añadir efecto de suciedad basado en el nivel
        addDirtEffect(originalPixmap, tint);

        Texture texture = new Texture(originalPixmap);
        originalPixmap.dispose();
        sourcePixmap.dispose();

        return new TextureRegion(texture);
    }

    private static void addDirtEffect(Pixmap pixmap, Color baseColor) {
        int densidad = 15 + (int)((1 - baseColor.r) * 30); // Más sucio cuanto más oscuro

        for (int i = 0; i < densidad; i++) {
            int x = random.nextInt(pixmap.getWidth());
            int y = random.nextInt(pixmap.getHeight());
            int size = 1 + random.nextInt(3);

            // Variación de color para las manchas
            Color dirtColor = varyColor(baseColor);
            pixmap.setColor(dirtColor);

            // Diferentes formas según la densidad
            if (random.nextFloat() > 0.7f) {
                pixmap.fillCircle(x, y, size);
            } else {
                pixmap.fillRectangle(x, y, size, size);
            }
        }
    }

    private static Color varyColor(Color base) {
        float variation = 0.15f;
        float r = clamp(base.r + (random.nextFloat() * 2 - 1) * variation);
        float g = clamp(base.g + (random.nextFloat() * 2 - 1) * variation);
        float b = clamp(base.b + (random.nextFloat() * 2 - 1) * variation);
        return new Color(r, g, b, base.a);
    }

    private static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }
}
