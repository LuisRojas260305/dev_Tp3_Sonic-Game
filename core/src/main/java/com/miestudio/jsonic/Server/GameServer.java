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
    private TiledMap map;

    private volatile boolean running = false;
    private long sequenceNumber = 0; // Contador de versiones del GameState

    public GameServer(JuegoSonic game, ConcurrentHashMap<Integer, InputState> playerInputs) {
        this.game = game;
        this.playerInputs = playerInputs;
        this.characters = new ConcurrentHashMap<>();

        // Cargar el mapa y configurar sistemas
        map = new TmxMapLoader().load(Constantes.MAPA_PATH + "Mapa.tmx");
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        mapWidth = layer.getWidth() * layer.getTileWidth();
        mapHeight = layer.getHeight() * layer.getTileHeight();

        collisionManager = new CollisionManager(map, "Colisiones", mapWidth, mapHeight);
        collisionManager.addTileCollisions(map, "Colisiones");

        pollutionSystem = new PollutionSystem(map, "Capa1");
        pollutionSystem.generarContaminacionInicial();

        initializeCharacters();
    }

    private void initializeCharacters() {
        Assets assets = game.getAssets();

        characters.put(0, new Sonic(0, assets.sonicAtlas));
        characters.put(1, new Tails(1, assets.tailsAtlas));
        characters.put(2, new Knockles(2, assets.knocklesAtlas));

        // Establecer posiciones de spawn
        Map<String, Vector2> spawnPoints = findSpawnPoints();

        // Sonic
        Personajes sonic = characters.get(0);
        Vector2 sonicSpawn = spawnPoints.getOrDefault("Sonic", new Vector2(mapWidth * 0.1f, mapHeight * 0.5f));
        float groundYSonic = collisionManager.getGroundY(new Rectangle(sonicSpawn.x, sonicSpawn.y, sonic.getWidth(), sonic.getHeight()));
        sonic.setPosition(sonicSpawn.x, groundYSonic >= 0 ? groundYSonic : sonicSpawn.y);
        sonic.setPreviousPosition(sonic.getX(), sonic.getY());

        // Tails
        Personajes tails = characters.get(1);
        Vector2 tailsSpawn = spawnPoints.getOrDefault("Tails", new Vector2(mapWidth * 0.2f, mapHeight * 0.5f));
        float groundYTails = collisionManager.getGroundY(new Rectangle(tailsSpawn.x, tailsSpawn.y, tails.getWidth(), tails.getHeight()));
        tails.setPosition(tailsSpawn.x, groundYTails >= 0 ? groundYTails : tailsSpawn.y);
        tails.setPreviousPosition(tails.getX(), tails.getY());

        // Knuckles
        Personajes knuckles = characters.get(2);
        Vector2 knucklesSpawn = spawnPoints.getOrDefault("Knuckles", new Vector2(mapWidth * 0.3f, mapHeight * 0.5f));
        float groundYKnuckles = collisionManager.getGroundY(new Rectangle(knucklesSpawn.x, knucklesSpawn.y, knuckles.getWidth(), knuckles.getHeight()));
        knuckles.setPosition(knucklesSpawn.x, groundYKnuckles >= 0 ? groundYKnuckles : knucklesSpawn.y);
        knuckles.setPreviousPosition(knuckles.getX(), knuckles.getY());
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
