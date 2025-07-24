package com.miestudio.jsonic.Server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.miestudio.jsonic.Actores.*;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Objetos.Anillo;
import com.miestudio.jsonic.Objetos.CargarObjetos;
import com.miestudio.jsonic.Objetos.Objetos;
import com.miestudio.jsonic.Server.domain.InputState;
import com.miestudio.jsonic.Util.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.miestudio.jsonic.Server.domain.*;

/**
 * Clase GameServer que gestiona la logica del juego en el lado del servidor (host).
 * Se encarga de la actualizacion del estado del juego, la gestion de personajes,
 * la creacion de objetos, la deteccion de colisiones y la sincronizacion de red.
 */
public class GameServer {

    private final JuegoSonic game; /** Referencia a la instancia principal del juego. */
    private final ConcurrentHashMap<Integer, Personajes> characters; /** Mapa de personajes activos en el juego, indexados por ID de jugador. */
    private final ConcurrentHashMap<Integer, InputState> playerInputs; /** Mapa de estados de entrada de los jugadores. */
    private final CollisionManager collisionManager; /** Gestor de colisiones del juego. */
    private final CargarObjetos cargarObjetos; /** Gestor para cargar y manejar objetos del juego. */
    private final ConcurrentHashMap<Integer, Objetos> gameObjects = new ConcurrentHashMap<>(); /** Mapa de objetos del juego activos, indexados por ID de objeto. */
    private int nextObjectId = 0; /** Contador para asignar IDs unicos a los nuevos objetos del juego. */
    private final float mapWidth; /** Ancho del mapa del juego en pixeles. */
    private final float mapHeight; /** Altura del mapa del juego en pixeles. */
    private final TiledMap map; /** El mapa de tiles del juego. */
    private volatile boolean running = false; /** Indica si el bucle principal del servidor esta en ejecucion. */
    private long sequenceNumber = 0; /** Numero de secuencia para el estado del juego, utilizado para la sincronizacion. */

    /**
     * Constructor de GameServer.
     * @param game La instancia principal del juego.
     * @param playerInputs Mapa concurrente de inputs de los jugadores.
     * @param map El mapa de tiles del juego.
     * @param collisionManager El gestor de colisiones del juego.
     * @param mapWidth El ancho del mapa en pixeles.
     * @param mapHeight La altura del mapa en pixeles.
     */
    public GameServer(JuegoSonic game, ConcurrentHashMap<Integer, InputState> playerInputs, TiledMap map, CollisionManager collisionManager, float mapWidth, float mapHeight) {
        this.game = game;
        this.playerInputs = playerInputs;
        this.map = map;
        this.collisionManager = collisionManager;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.characters = new ConcurrentHashMap<>();
        this.cargarObjetos = new CargarObjetos(game.getAssets().objetosAtlas);
        initializeCharacters();
        spawnGameObjects("Anillo", "SpawnObjetos");
    }

    /**
     * Genera objetos del juego en puntos de spawn definidos en el mapa.
     * @param objectType El tipo de objeto a generar (ej. "Anillo").
     * @param layerName El nombre de la capa del mapa donde buscar los puntos de spawn.
     */
    private void spawnGameObjects(String objectType, String layerName) {

        List<Vector2> spawnPoints = MapUtil.findAllSpawnPoints(map, layerName, objectType);

        if (spawnPoints.isEmpty()) {
            Gdx.app.log("GameServer", "No spawn points found for " + objectType);
            return;
        }

        

        MapLayer layer = map.getLayers().get(layerName);
        float tileWigth = 32;
        float tileHeight = 32;

        if (layer instanceof TiledMapTileLayer) {
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
            tileWigth = tileLayer.getTileWidth();
            tileHeight = tileLayer.getTileHeight();
        }

        for (Vector2 spawnPoint : spawnPoints) {

            float centeredX = spawnPoint.x + (tileWigth - 15f) / 2;
            float centeredY = spawnPoint.y + (tileHeight - 15f) / 2;

            if ("Anillo".equals(objectType)) {
                Anillo anillo = new Anillo(centeredX, centeredY, cargarObjetos.animacionAnillo);
                anillo.setId(nextObjectId++);
                gameObjects.put(anillo.getId(), anillo);
                cargarObjetos.agregarAnillo(anillo);
            }
        }
        Gdx.app.log("GameServer", "Se han creado " + spawnPoints.size() + " " + objectType + "s.");
    }

    /**
     * Inicializa los personajes de los jugadores basandose en los personajes seleccionados
     * y los posiciona en sus respectivos puntos de spawn en el mapa.
     */
    private void initializeCharacters() {
        Assets assets = game.getAssets();
        Map<Integer, String> playerCharacterMap = game.networkManager.getSelectedCharacters();

        for (Map.Entry<Integer, String> entry : playerCharacterMap.entrySet()) {
            int playerId = entry.getKey();
            String characterType = entry.getValue();
            Personajes character = null;

            switch (characterType) {
                case "Sonic":
                    character = new Sonic(playerId, assets.sonicAtlas);
                    break;
                case "Tails":
                    character = new Tails(playerId, assets.tailsAtlas);
                    break;
                case "Knuckles":
                    character = new Knockles(playerId, assets.knucklesAtlas);
                    break;
                default:
                    continue;
            }

            if (character != null) {
                characters.put(playerId, character);
                Vector2 spawn = MapUtil.findSpawnPoint(map, "SpawnEntidades", characterType);
                if (spawn == null) {
                    spawn = new Vector2(mapWidth * 0.1f + playerId * 50, mapHeight * 0.5f);
                }
                float groundY = collisionManager.getGroundY(new Rectangle(spawn.x, spawn.y, character.getWidth(), character.getHeight()));
                character.setPosition(spawn.x, groundY >= 0 ? groundY : spawn.y);
                character.setPreviousPosition(character.getX(), character.getY());
            }
        }
    }

    /**
     * Inicia el bucle principal del servidor en un nuevo hilo.
     */
    public void start() {
        running = true;
        Thread gameLoopThread = new Thread(this::serverGameLoop);
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();
    }

    /**
     * Detiene el bucle principal del servidor.
     */
    public void stop() {
        running = false;
    }

    /**
     * El bucle principal del servidor que actualiza el estado del juego a una tasa fija.
     */
    private void serverGameLoop() {
        final float FIXED_DELTA_TIME = 1f / 60f;
        long lastTime = System.nanoTime();
        float accumulator = 0f;

        while (running) {
            long now = System.nanoTime();
            float frameTime = (now - lastTime) / 1_000_000_000f;
            lastTime = now;
            accumulator += frameTime;

            while (accumulator >= FIXED_DELTA_TIME) {
                updateGameState(FIXED_DELTA_TIME);
                accumulator -= FIXED_DELTA_TIME;
            }

            long sleepTime = (long) ((FIXED_DELTA_TIME - accumulator) * 1000);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        }
    }

    /**
     * Actualiza el estado del juego, incluyendo la logica de personajes, objetos y colisiones.
     * Luego, crea y transmite el GameState a todos los clientes.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos (delta fijo).
     */
    private void updateGameState(float delta) {
        // Actualizar personajes
        for (Personajes character : characters.values()) {
            InputState input = playerInputs.get(character.getPlayerId());
            if (input != null) {
                character.setPreviousPosition(character.getX(), character.getY());
                character.update(delta, collisionManager);
                character.handleInput(input, collisionManager, delta);
                float newX = Math.max(0, Math.min(character.getX(), mapWidth - character.getWidth()));
                float newY = Math.max(0, Math.min(character.getY(), mapHeight - character.getHeight()));
                character.setPosition(newX, newY);
            }
        }

        // Actualizar objetos
        cargarObjetos.actualizar(delta);

        // Detectar colisiones
        detectCollisions();

        // Crear estados de jugadores
        ArrayList<PlayerState> playerStates = new ArrayList<>();
        for (Personajes character : characters.values()) {
            String characterType = game.networkManager.getSelectedCharacters().get(character.getPlayerId());
            playerStates.add(new PlayerState(
                character.getPlayerId(),
                character.getX(), character.getY(),
                character.isFacingRight(),
                character.getCurrentAnimationName(),
                character.getAnimationStateTime(),
                characterType,
                (character instanceof Tails) ? ((Tails) character).isFlying() : false
            ));
        }

        for (Personajes character : characters.values()) {
            if (character instanceof Tails) {
                // Asegurarse de que el personaje es realmente una instancia de Tails
                if (character.getClass().equals(Tails.class)) {
                    Tails tails = (Tails) character;
                    for (Robot robot : tails.getActiveRobots()) {
                        robot.update(delta, collisionManager);
                    }
                } else {
                    Gdx.app.error("GameServer", "Error de tipo inesperado: " + character.getClass().getName() + " no puede ser casteado a Tails.");
                }
            }
        }
        
        // Crear estados de objetos
        List<ObjectState> objectStates = new ArrayList<>();
        for (Objetos obj : gameObjects.values()) {
            if (obj instanceof Anillo) {
                objectStates.add(new ObjectState(
                    obj.getId(),
                    obj.x,
                    obj.y,
                    obj.estaActivo(),
                    "Anillo"
                ));
            }
        }

        // Actualizar y transmitir GameState
        GameState newGameState = new GameState(playerStates, objectStates, sequenceNumber++);
        game.networkManager.setCurrentGameState(newGameState);
        game.networkManager.broadcastUdpGameState();
    }

    /**
     * Detecta y maneja las colisiones entre personajes y objetos del juego.
     */
    private void detectCollisions() {
        for (Personajes character : characters.values()) {
            Rectangle charHitbox = new Rectangle(
                character.getX() - 5,
                character.getY() - 5,
                character.getWidth() + 10,
                character.getHeight() + 10
            );

            for (Objetos obj : gameObjects.values()) {
                if (obj.estaActivo() && charHitbox.overlaps(obj.getHitbox())) {
                    obj.setActivo(false);
                    Gdx.app.log("GameServer", "Anillo con ID: " + obj.getId() + " desactivado por el jugador " + character.getPlayerId());
                }
            }
        }
    }

    /**
     * Obtiene el estado actual del juego.
     * @return El objeto GameState actual.
     */
    public GameState getCurrentGameState() {
        return game.networkManager.getCurrentGameState();
    }
}
