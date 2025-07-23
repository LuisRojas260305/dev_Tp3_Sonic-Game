package com.miestudio.jsonic.Server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.miestudio.jsonic.Actores.Knockles;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Sistemas.PollutionSystem;
import com.miestudio.jsonic.Util.Assets;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.GameState;
import com.miestudio.jsonic.Util.InputState;
import com.miestudio.jsonic.Util.PlayerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {

    private final JuegoSonic game;
    private final ConcurrentHashMap<Integer, Personajes> characters;
    private final ConcurrentHashMap<Integer, InputState> playerInputs;
    private final CollisionManager collisionManager;
    private final PollutionSystem pollutionSystem;
    private final float mapWidth;
    private final float mapHeight;
    private final TiledMap map;

    private volatile boolean running = false;
    private long sequenceNumber = 0; // Contador de versiones del GameState

    public GameServer(JuegoSonic game, ConcurrentHashMap<Integer, InputState> playerInputs, TiledMap map, CollisionManager collisionManager, PollutionSystem pollutionSystem, float mapWidth, float mapHeight) {
        this.game = game;
        this.playerInputs = playerInputs;
        this.map = map;
        this.collisionManager = collisionManager;
        this.pollutionSystem = pollutionSystem;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.characters = new ConcurrentHashMap<>();

        initializeCharacters();
    }

    private void initializeCharacters() {
        Assets assets = game.getAssets();
        Map<Integer, String> playerCharacterMap = game.networkManager.getSelectedCharacters();

        // Establecer posiciones de spawn
        Map<String, Vector2> spawnPoints = findSpawnPoints();

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
                    character = new Knockles(playerId, assets.knocklesAtlas);
                    break;
                default:
                    Gdx.app.error("GameServer", "Tipo de personaje desconocido: " + characterType);
                    continue;
            }

            if (character != null) {
                characters.put(playerId, character);
                Vector2 spawn = spawnPoints.getOrDefault(characterType, new Vector2(mapWidth * 0.1f + playerId * 50, mapHeight * 0.5f));
                float groundY = collisionManager.getGroundY(new Rectangle(spawn.x, spawn.y, character.getWidth(), character.getHeight()));
                character.setPosition(spawn.x, groundY >= 0 ? groundY : spawn.y);
                character.setPreviousPosition(character.getX(), character.getY());
            }
        }

        // Generar contaminación inicial solo si GameServer es el responsable (es decir, si es el host)
        pollutionSystem.generarContaminacionInicial();
    }

    

    private Map<String, Vector2> findSpawnPoints() {
        Map<String, Vector2> spawnPoints = new HashMap<>();
        MapLayer layer = map.getLayers().get("SpawnJugadores");

        if (layer == null || !(layer instanceof TiledMapTileLayer)) {
            Gdx.app.error("GameServer", "No se encontró la capa de tiles 'SpawnJugadores'.");
            return spawnPoints;
        }

        TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
        float tileWidth = tileLayer.getTileWidth();
        float tileHeight = tileLayer.getTileHeight();

        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                com.badlogic.gdx.maps.MapProperties properties = cell.getTile().getProperties();
                if (properties.get("Spawn", false, Boolean.class)) {
                    String characterName = null;
                    if (properties.get("Sonic", false, Boolean.class)) {
                        characterName = "Sonic";
                    } else if (properties.get("Tails", false, Boolean.class)) {
                        characterName = "Tails";
                    } else if (properties.get("Knuckles", false, Boolean.class)) {
                        characterName = "Knuckles";
                    }

                    if (characterName != null) {
                        float spawnX = x * tileWidth;
                        float spawnY = y * tileHeight;
                        spawnPoints.put(characterName, new Vector2(spawnX, spawnY));
                        Gdx.app.log("GameServer", "Spawn encontrado para " + characterName + " en (" + spawnX + ", " + spawnY + ")");
                    }
                }
            }
        }
        return spawnPoints;
    }

    public void start() {
        running = true;
        Thread gameLoopThread = new Thread(this::serverGameLoop);
        gameLoopThread.setDaemon(true); // Asegura que el hilo se detenga con la aplicación
        gameLoopThread.start();
    }

    public void stop() {
        running = false;
    }

    private void serverGameLoop() {
        final float FIXED_DELTA_TIME = 1f / 60f; // 60 actualizaciones por segundo
        long lastTime = System.nanoTime();
        float accumulator = 0f;

        while (running) {
            long now = System.nanoTime();
            float frameTime = (now - lastTime) / 1_000_000_000f; // Tiempo transcurrido en segundos
            lastTime = now;

            accumulator += frameTime;

            while (accumulator >= FIXED_DELTA_TIME) {
                // Actualizar lógica del juego con FIXED_DELTA_TIME
                updateGameState(FIXED_DELTA_TIME);
                accumulator -= FIXED_DELTA_TIME;
            }

            // Dormir para no consumir CPU si hay tiempo de sobra
            long sleepTime = (long) ((FIXED_DELTA_TIME - accumulator) * 1000); // Convertir a milisegundos
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                    running = false; // Detiene el bucle
                }
            }
        }
    }

    private void updateGameState(float delta) {
        for (Personajes character : characters.values()) {
            InputState input = playerInputs.get(character.getPlayerId());

            if (input != null) {
                Gdx.app.log("GameServer", "Processing input for player " + character.getPlayerId() + ": " + input.isLeft() + ", " + input.isRight());
                // Guardar posición anterior para interpolación
                character.setPreviousPosition(character.getX(), character.getY());

                // Actualizar física básica
                character.update(delta, collisionManager);

                // Manejar input con colisiones
                character.handleInput(input, collisionManager, delta);

                // Limitar personajes dentro del mapa
                float newX = Math.max(0, Math.min(character.getX(), mapWidth - character.getWidth()));
                float newY = Math.max(0, Math.min(character.getY(), mapHeight - character.getHeight()));
                character.setPosition(newX, newY);
            }
        }

        pollutionSystem.update(delta);
        if (Math.random() < 0.1f) { // Lógica de propagación de contaminación
            pollutionSystem.propagarContaminacion();
        }

        // Incrementar número de secuencia
        sequenceNumber++;

        // Generar GameState con el nuevo sequenceNumber
        ArrayList<PlayerState> playerStates = new ArrayList<>();
        for (Personajes character : characters.values()) {
            playerStates.add(new PlayerState(
                character.getPlayerId(),
                character.getX(), character.getY(),
                character.isFacingRight(),
                character.getCurrentAnimationName(),
                character.getAnimationStateTime()));
        }

        // Convertir PuntosContaminacion a CorruptionState
        ArrayList<GameState.CorruptionState> corruptionStates = new ArrayList<>();
        for (PollutionSystem.PuntoContaminacion punto : pollutionSystem.getPuntosContaminacion()) {
            corruptionStates.add(new GameState.CorruptionState(punto.tileX, punto.tileY, punto.nivel));
        }

        // Actualizar el GameState actual del servidor
        game.networkManager.setCurrentGameState(new GameState(playerStates, corruptionStates, sequenceNumber));
    }

    public GameState getCurrentGameState() {
        // Asegurarse de que networkManager tenga un método setCurrentGameState
        return game.networkManager.getCurrentGameState();
    }
}
