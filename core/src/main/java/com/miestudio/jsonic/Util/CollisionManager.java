package com.miestudio.jsonic.Util;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

/**
 * Gestiona las colisiones en el juego, incluyendo la detección de colisiones con el mapa
 * y la determinación de si un personaje está en el suelo.
 */
public class CollisionManager {
    private static final float FEET_SENSOR_Y_OFFSET_GROUNDED = -10f;
    /**
     * Desplazamiento Y para el sensor de pies al verificar si está en el suelo.
     */
    private static final float FEET_SENSOR_HEIGHT_GROUNDED = 15f;
    /**
     * Altura del sensor de pies al verificar si está en el suelo.
     */
    private static final float FEET_SENSOR_Y_OFFSET_GROUNDY = -20f;
    /**
     * Desplazamiento Y para el sensor de pies al obtener la altura del suelo.
     */
    private static final float FEET_SENSOR_HEIGHT_GROUNDY = 25f;
    /**
     * Altura del sensor de pies al obtener la altura del suelo.
     */

    private Array<Shape2D> collisionShapes;
    /**
     * Colección de formas 2D que representan las áreas de colisión.
     */
    private final float mapWidth;
    /**
     * Ancho del mapa del juego en píxeles.
     */
    private final float mapHeight; /** Altura del mapa del juego en píxeles. */

    /**
     * Constructor de CollisionManager.
     *
     * @param map             El mapa de tiles del juego.
     * @param objectLayerName El nombre de la capa de objetos en el mapa que contiene las colisiones.
     * @param mapWidth        El ancho total del mapa en píxeles.
     * @param mapHeight       La altura total del mapa en píxeles.
     */
    public CollisionManager(TiledMap map, String objectLayerName, float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        collisionShapes = new Array<>();
        // Cargar colisiones desde la Object Layer estándar (si existe)
        MapLayer layer = map.getLayers().get(objectLayerName);
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    collisionShapes.add(((RectangleMapObject) object).getRectangle());
                }
            }
        }
    }

    /**
     * Añade las colisiones definidas en los tiles de una capa de tiles.
     * Los tiles deben tener una propiedad booleana llamada "Colisiones" establecida a true.
     * Este método debe ser llamado después de crear CollisionManager si se utilizan colisiones de tiles.
     *
     * @param map           El mapa de tiles del juego.
     * @param tileLayerName El nombre de la capa de tiles que contiene las colisiones.
     */
    public void addTileCollisions(TiledMap map, String tileLayerName) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(tileLayerName);

        if (layer == null) return;

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);

                if (cell == null) continue;

                Object collisionProp = cell.getTile().getProperties().get("Colision");
                boolean colision = false;

                if (collisionProp instanceof Boolean) {
                    colision = (Boolean) collisionProp;
                } else if (collisionProp instanceof String) {
                    colision = Boolean.parseBoolean((String) collisionProp);
                }

                if (colision) {

                    Rectangle tileRect = new Rectangle(
                        x * layer.getTileWidth(),
                        y * layer.getTileHeight(),
                        layer.getTileWidth(),
                        layer.getTileHeight()
                    );

                    collisionShapes.add(tileRect);
                }
            }
        }
    }

    /**
     * Verifica si un rectángulo dado colisiona con alguna de las formas de colisión del mapa
     * o con los límites del mapa.
     *
     * @param rect El rectángulo a verificar.
     * @return true si hay colisión, false en caso contrario.
     */
    public boolean collides(Rectangle rect) {
        // Verificar bordes del mapa
        if (rect.x < 0 || rect.y < 0 ||
            rect.x + rect.width > mapWidth ||
            rect.y + rect.height > mapHeight) {
            return true;
        }

        // Verificar colisiones con objetos
        for (Shape2D shape : collisionShapes) {
            if (shape instanceof Rectangle) {
                if (rect.overlaps((Rectangle) shape)) return true;
            }
        }
        return false;
    }

    /**
     * Verifica si un rectángulo de personaje está en el suelo.
     * Utiliza un sensor de pies ligeramente más grande para una detección precisa.
     *
     * @param characterRect El rectángulo que representa los límites del personaje.
     * @return true si el personaje está en el suelo, false en caso contrario.
     */
    public boolean isOnGround(Rectangle characterRect) {
        // Área de detección más grande para mejor precisión
        Rectangle feetSensor = new Rectangle(
            characterRect.x + characterRect.width / 4,
            characterRect.y + FEET_SENSOR_Y_OFFSET_GROUNDED,  // Mayor área de detección
            characterRect.width / 2,
            FEET_SENSOR_HEIGHT_GROUNDED  // Mayor altura para mejor detección
        );

        return collides(feetSensor);
    }

    /**
     * Obtiene la posición Y más alta del suelo debajo de un rectángulo de personaje.
     *
     * @param characterRect El rectángulo que representa los límites del personaje.
     * @return La coordenada Y del suelo, o -1 si no se detecta suelo.
     */
    public float getGroundY(Rectangle characterRect) {
        // Área de detección más grande para mejor precisión
        Rectangle feetSensor = new Rectangle(
            characterRect.x + characterRect.width / 4,
            characterRect.y + FEET_SENSOR_Y_OFFSET_GROUNDY,  // Mayor área de detección
            characterRect.width / 2,
            FEET_SENSOR_HEIGHT_GROUNDY  // Mayor altura para mejor detección
        );

        float maxGroundY = -1; // Valor inicial

        // Verificar colisiones con objetos
        for (Shape2D shape : collisionShapes) {
            if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                if (feetSensor.overlaps(rect)) {
                    // La parte superior del rectángulo de colisión
                    float top = rect.y + rect.height;
                    if (top > maxGroundY) {
                        maxGroundY = top;
                    }
                }
            }
        }

        // Verificar borde inferior del mapa
        if (characterRect.y < 0) {
            maxGroundY = Math.max(maxGroundY, 0);
        }

        return maxGroundY;
    }

    /**
     * Obtiene el ancho del mapa.
     *
     * @return El ancho del mapa en píxeles.
     */
    public float getMapWidth() {
        return mapWidth;
    }

    /**
     * Obtiene la altura del mapa.
     *
     * @return La altura del mapa en píxeles.
     */
    public float getMapHeight() {
        return mapHeight;
    }

    /**
     * Obtiene la colección de formas de colisión. Útil para depuración visual.
     *
     * @return Un Array de Shape2D que representa las formas de colisión.
     */
    public Array<Shape2D> getCollisionShapes() {
        return collisionShapes;
    }

}
