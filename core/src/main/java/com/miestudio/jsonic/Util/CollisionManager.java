package com.miestudio.jsonic.Util;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

/**
 * Gestiona las colisiones en el juego, incluyendo la deteccion de colisiones con el mapa
 * y la determinacion de si un personaje esta en el suelo.
 */
public class CollisionManager {

    public static class CollisionShape {
        public Shape2D shape;
        public boolean isPlatform;

        public CollisionShape(Shape2D shape, boolean isPlatform) {
            this.shape = shape;
            this.isPlatform = isPlatform;
        }
    }

    private static final float FEET_SENSOR_Y_OFFSET_GROUNDED = -10f;
    /**
     * Desplazamiento Y para el sensor de pies al verificar si esta en el suelo.
     */
    private static final float FEET_SENSOR_HEIGHT_GROUNDED = 15f;
    /**
     * Altura del sensor de pies al verificar si esta en el suelo.
     */
    private static final float FEET_SENSOR_Y_OFFSET_GROUNDY = -20f;
    /**
     * Desplazamiento Y para el sensor de pies al obtener la altura del suelo.
     */
    private static final float FEET_SENSOR_HEIGHT_GROUNDY = 25f;
    /**
     * Altura del sensor de pies al obtener la altura del suelo.
     */

    private Array<CollisionShape> collisionShapes;
    /**
     * Coleccion de formas 2D que representan las areas de colision.
     */
    private final float mapWidth;
    /**
     * Ancho del mapa del juego en pixeles.
     */
    private final float mapHeight; /** Altura del mapa del juego en pixeles. */

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
                    collisionShapes.add(new CollisionShape(((RectangleMapObject) object).getRectangle(), false)); // Por defecto no es plataforma
                }
            }
        }
    }

    /**
     * Añade las colisiones definidas en los tiles de una capa de tiles.
     * Los tiles deben tener una propiedad booleana llamada "Colisiones" establecida a true.
     * Este metodo debe ser llamado despues de crear CollisionManager si se utilizan colisiones de tiles.
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

                MapProperties props = cell.getTile().getProperties();
                boolean isCollision = props.get("Colision", false, Boolean.class);
                boolean isPlatform = props.get("Plataforma", false, Boolean.class);

                if (isCollision) {

                    Rectangle tileRect = new Rectangle(
                        x * layer.getTileWidth(),
                        y * layer.getTileHeight(),
                        layer.getTileWidth(),
                        layer.getTileHeight()
                    );

                    collisionShapes.add(new CollisionShape(tileRect, isPlatform));
                }
            }
        }
    }

    /**
     * Verifica si un rectangulo dado colisiona con alguna de las formas de colision del mapa
     * o con los limites del mapa.
     *
     * @param rect El rectangulo a verificar.
     * @return true si hay colision, false en caso contrario.
     */
    public boolean collides(Rectangle characterRect) {
        // Verificar bordes del mapa
        if (characterRect.x < 0 || characterRect.y < 0 ||
            characterRect.x + characterRect.width > mapWidth ||
            characterRect.y + characterRect.height > mapHeight) {
            return true;
        }

        // Verificar colisiones con objetos
        for (CollisionShape collisionShape : collisionShapes) {
            if (collisionShape.shape instanceof Rectangle) {
                Rectangle collisionRect = (Rectangle) collisionShape.shape;
                if (characterRect.overlaps(collisionRect)) {
                    if (collisionShape.isPlatform) {
                        // Si es una plataforma, solo colisiona si el personaje está cayendo sobre ella
                        // (la parte inferior del personaje está por encima de la parte superior de la plataforma)
                        // o si ya está encima (para evitar que la atraviese al caer)
                        if (characterRect.y >= collisionRect.y + collisionRect.height - 5) { // Pequeño umbral
                            return true; // Colisión desde arriba o ya encima
                        }
                    } else {
                        return true; // Colisión con un bloque sólido
                    }
                }
            }
        }
        return false;
    }

    /**
     * Verifica si un rectangulo de personaje esta en el suelo.
     * Utiliza un sensor de pies ligeramente mas grande para una deteccion precisa.
     *
     * @param characterRect El rectangulo que representa los limites del personaje.
     * @return true si el personaje esta en el suelo, false en caso contrario.
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
     * Obtiene la posicion Y mas alta del suelo debajo de un rectangulo de personaje.
     *
     * @param characterRect El rectangulo que representa los limites del personaje.
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
        for (CollisionShape collisionShape : collisionShapes) {
            if (collisionShape.shape instanceof Rectangle) {
                Rectangle collisionRect = (Rectangle) collisionShape.shape;
                if (feetSensor.overlaps(collisionRect)) {
                    // Si es una plataforma, solo consideramos el suelo si el personaje está por encima de ella
                    if (collisionShape.isPlatform) {
                        if (characterRect.y >= collisionRect.y + collisionRect.height - 5) { // Pequeño umbral
                            float top = collisionRect.y + collisionRect.height;
                            if (top > maxGroundY) {
                                maxGroundY = top;
                            }
                        }
                    } else { // Es un bloque sólido
                        float top = collisionRect.y + collisionRect.height;
                        if (top > maxGroundY) {
                            maxGroundY = top;
                        }
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
     * @return El ancho del mapa en pixeles.
     */
    public float getMapWidth() {
        return mapWidth;
    }

    /**
     * Obtiene la altura del mapa.
     *
     * @return La altura del mapa en pixeles.
     */
    public float getMapHeight() {
        return mapHeight;
    }

    /**
     * Obtiene la coleccion de formas de colision. Util para depuracion visual.
     *
     * @return Un Array de Shape2D que representa las formas de colision.
     */
    public Array<Shape2D> getCollisionShapes() {
        Array<Shape2D> shapes = new Array<>();
        for (CollisionShape cs : collisionShapes) {
            shapes.add(cs.shape);
        }
        return shapes;
    }

}
