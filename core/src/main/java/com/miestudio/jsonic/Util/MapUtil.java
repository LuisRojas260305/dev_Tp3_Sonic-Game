package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase de utilidad para operaciones relacionadas con el mapa de tiles del juego.
 * Proporciona metodos para encontrar puntos de spawn y otras utilidades de mapa.
 */
public class MapUtil {
    /**
     * Encuentra un punto de spawn especifico para un tipo de personaje en una capa dada del mapa.
     * Los puntos de spawn se definen como tiles con la propiedad "Spawn" establecida a true,
     * y una propiedad "To" que coincide con el tipo de personaje buscado.
     * @param layerName El nombre de la capa del mapa donde buscar los puntos de spawn.
     * @param target El tipo de entidad cuyo punto de spawn se esta buscando.
     * @return La posicion (Vector2) del punto de spawn del personaje, o null si no se encuentra.
     */
    public static Vector2 findSpawnPoint(TiledMap map, String layerName, String target) {
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("MapUtil", "Layer '" + layerName + "' not found.");
            return null;
        }
        if (!(layer instanceof TiledMapTileLayer)) {
            Gdx.app.log("MapUtil", "Layer '" + layerName + "' is not a TiledMapTileLayer.");
            return null;
        }

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) {
                    // Gdx.app.log("MapUtil", "Cell or tile is null at " + x + "," + y);
                    continue;
                }

                MapProperties props = cell.getTile().getProperties();
                boolean isSpawn = props.get("Spawn", false, Boolean.class);
                String toValue = props.get("To", String.class);

                Gdx.app.log("MapUtil", "Checking tile at " + x + "," + y + ": Spawn=" + isSpawn + ", To=" + toValue);

                if (isSpawn && target.equals(toValue)) {
                    float spawnX = x * tileLayer.getTileWidth();
                    float spawnY = y * tileLayer.getTileHeight();
                    Gdx.app.log("MapUtil", "Spawn point found for " + target + ": " + spawnX + ", " + spawnY);
                    return new Vector2(spawnX, spawnY);
                }
            }
        }
        return null;
    }

    /**
     * Encuentra todos los puntos de spawn para un tipo de entidad especifico en una capa dada del mapa.
     * @param map El mapa de tiles.
     * @param layerName El nombre de la capa del mapa donde buscar los puntos de spawn.
     * @param target El tipo de entidad cuyos puntos de spawn se estan buscando.
     * @return Una lista de posiciones (Vector2) de los puntos de spawn encontrados.
     */
    public static List<Vector2> findAllSpawnPoints(TiledMap map, String layerName, String target) {
        List<Vector2> spawnPoints = new ArrayList<>();
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) return spawnPoints;
        if (!(layer instanceof TiledMapTileLayer)) return spawnPoints;

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                MapProperties props = cell.getTile().getProperties();
                boolean isSpawn = props.get("Spawn", false, Boolean.class);
                String toValue = props.get("To", String.class);

                if (isSpawn && target.equals(toValue)) {
                    float spawnX = x * tileLayer.getTileWidth();
                    float spawnY = y * tileLayer.getTileHeight();
                    spawnPoints.add(new Vector2(spawnX, spawnY));
                }
            }
        }
        return spawnPoints;
    }
}
