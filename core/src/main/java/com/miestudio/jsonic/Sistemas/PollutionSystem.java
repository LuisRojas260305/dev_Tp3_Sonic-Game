package com.miestudio.jsonic.Sistemas;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;


import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.TileModifier;

import java.util.Random;

public class PollutionSystem {
    private final Array<PuntoContaminacion> puntosContaminacion = new Array<>();
    private final Array<PuntoContaminacion> potentialOrigins = new Array<>(); // Nueva lista para orígenes potenciales
    private final Random random = new Random();
    private final TiledMap map;
    private final String capaBase;

    private float tiempoAcumuladoPropagacion = 0; // Renombrado para claridad
    private final float intervaloPropagacion = 10f; // Propagación cada 10 segundos

    private float tiempoAcumuladoActivacionOrigen = 0; // Nuevo temporizador
    private final float intervaloActivacionOrigen = 30f; // Cada 30 segundos se activa un nuevo origen

    // Cache de tiles originales
    private final ObjectMap<String, TiledMapTile> tilesOriginales = new ObjectMap<>();

    public PollutionSystem(TiledMap map, String capaBase) {
        this.map = map;
        this.capaBase = capaBase;
        cacheTilesOriginales();
        // Recolectar todos los orígenes potenciales al inicio
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(capaBase);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    MapProperties props = cell.getTile().getProperties();
                    if (props.containsKey(Constantes.PROP_ORIGEN) && props.get(Constantes.PROP_ORIGEN, Boolean.class)) {
                        potentialOrigins.add(new PuntoContaminacion(x, y, 3));
                    }
                }
            }
        }
    }

    private void cacheTilesOriginales() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(capaBase);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    String key = x + ":" + y;
                    tilesOriginales.put(key, cell.getTile());
                }
            }
        }
    }

    public void update(float delta) {
        tiempoAcumuladoPropagacion += delta;
        if (tiempoAcumuladoPropagacion >= intervaloPropagacion) {
            propagarContaminacion();
            tiempoAcumuladoPropagacion = 0;
        }

        tiempoAcumuladoActivacionOrigen += delta;
        if (tiempoAcumuladoActivacionOrigen >= intervaloActivacionOrigen) {
            activarNuevoOrigen();
            tiempoAcumuladoActivacionOrigen = 0;
        }
    }

    public void generarContaminacionInicial() {
        // Activar 3-4 orígenes aleatorios al inicio
        int numToActivate = Math.min(random.nextInt(2) + 3, potentialOrigins.size); // 3 o 4, o menos si no hay suficientes
        for (int i = 0; i < numToActivate; i++) {
            int randomIndex = random.nextInt(potentialOrigins.size);
            PuntoContaminacion origin = potentialOrigins.removeIndex(randomIndex);
            generarPuntoContaminacion(origin.tileX, origin.tileY, origin.nivel);
        }
    }

    private void activarNuevoOrigen() {
        if (potentialOrigins.size > 0) {
            int randomIndex = random.nextInt(potentialOrigins.size);
            PuntoContaminacion origin = potentialOrigins.removeIndex(randomIndex);
            generarPuntoContaminacion(origin.tileX, origin.tileY, origin.nivel);
        }
    }

    public void generarPuntoContaminacion(int tileX, int tileY, int nivel) {
        if (!esTileContaminado(tileX, tileY)) {
            puntosContaminacion.add(new PuntoContaminacion(tileX, tileY, nivel));
            aplicarContaminacionVisual(tileX, tileY, nivel);
        }
    }

    public void propagarContaminacion() {
        Array<PuntoContaminacion> puntosParaAgregar = new Array<>();

        // Iterar sobre una copia para evitar ConcurrentModificationException
        Array<PuntoContaminacion> currentPuntos = new Array<>(puntosContaminacion);

        for (PuntoContaminacion punto : currentPuntos) {
            // Los tiles de nivel 1 ya no propagan
            if (punto.nivel <= 1) continue;

            Array<PuntoContaminacion> adyacentesContaminables = new Array<>();
            int[][] direcciones = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

            for (int[] dir : direcciones) {
                int nx = punto.tileX + dir[0];
                int ny = punto.tileY + dir[1];

                // Comprobar si el tile es válido, contaminable y NO está ya contaminado
                if (esTileValido(nx, ny) && esTileContaminable(nx, ny) && !esTileContaminado(nx, ny)) {
                    adyacentesContaminables.add(new PuntoContaminacion(nx, ny, punto.nivel - 1));
                }
            }

            if (adyacentesContaminables.size > 0) {
                // Elegir aleatoriamente entre 1 y todos los tiles adyacentes contaminables
                int numToContaminate = random.nextInt(adyacentesContaminables.size) + 1;
                for (int i = 0; i < numToContaminate; i++) {
                    int randomIndex = random.nextInt(adyacentesContaminables.size);
                    PuntoContaminacion nuevoPunto = adyacentesContaminables.removeIndex(randomIndex);
                    puntosParaAgregar.add(nuevoPunto);
                }
            }
        }

        // Añadir los nuevos puntos de contaminación a la lista principal y aplicar visuales
        for (PuntoContaminacion nuevo : puntosParaAgregar) {
            // Doble verificación para asegurar que no se contamine un tile ya contaminado
            if (!esTileContaminado(nuevo.tileX, nuevo.tileY)) {
                puntosContaminacion.add(nuevo);
                aplicarContaminacionVisual(nuevo.tileX, nuevo.tileY, nuevo.nivel);
            }
        }
    }

    private boolean esTileContaminable(int tileX, int tileY) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(capaBase);
        Cell cell = layer.getCell(tileX, tileY);
        if (cell == null || cell.getTile() == null) return false;

        MapProperties props = cell.getTile().getProperties();
        return props.containsKey(Constantes.PROP_CONTAMINABLE) && props.get(Constantes.PROP_CONTAMINABLE, Boolean.class);
    }

    private void aplicarContaminacionVisual(int tileX, int tileY, int nivel) {
        // Ejecutar la modificación visual en el hilo de renderizado de LibGDX
        Gdx.app.postRunnable(() -> {
            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(capaBase);
            TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
            if (cell == null) return;

            // Obtener el color según el nivel
            Color color = getColorForLevel(nivel);
            TileModifier.modificarTile(map, capaBase, tileX, tileY, color);
        });
    }

    public boolean limpiarContaminacion(int tileX, int tileY) {
        for (int i = puntosContaminacion.size - 1; i >= 0; i--) {
            PuntoContaminacion punto = puntosContaminacion.get(i);
            if (punto.tileX == tileX && punto.tileY == tileY) {
                puntosContaminacion.removeIndex(i);
                restaurarTileOriginal(tileX, tileY);
                return true;
            }
        }
        return false;
    }

    private void restaurarTileOriginal(int tileX, int tileY) {
        // Ejecutar la restauración visual en el hilo de renderizado de LibGDX
        Gdx.app.postRunnable(() -> {
            String key = tileX + ":" + tileY;
            TiledMapTile original = tilesOriginales.get(key);

            if (original != null) {
                TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(capaBase);
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                if (cell != null) {
                    cell.setTile(original);
                }
            }
        });
    }

    private boolean esTileValido(int x, int y) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(capaBase);
        return x >= 0 && y >= 0 && x < layer.getWidth() && y < layer.getHeight();
    }

    public boolean esTileContaminado(int x, int y) {
        for (PuntoContaminacion punto : puntosContaminacion) {
            if (punto.tileX == x && punto.tileY == y) return true;
        }
        return false;
    }

    public Array<PuntoContaminacion> getPuntosContaminacion() {
        return puntosContaminacion;
    }

    public static class PuntoContaminacion {
        public final int tileX;
        public final int tileY;
        public final int nivel;

        public PuntoContaminacion(int tileX, int tileY, int nivel) {
            this.tileX = tileX;
            this.tileY = tileY;
            this.nivel = nivel;
        }
    }

    public static Color getColorForLevel(int nivel) {
        switch(nivel) {
            case 1: return new Color(0.9f, 0.9f, 0.5f, 1f); // Amarillo apagado
            case 2: return new Color(0.7f, 0.7f, 0.3f, 1f); // Verde oliva
            case 3: return new Color(0.4f, 0.4f, 0.2f, 1f); // Marrón tierra
            default: return new Color(0.8f, 0.8f, 0.4f, 1f);
        }
    }
}