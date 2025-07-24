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

public class GameServer {

    private final JuegoSonic game;
    private final ConcurrentHashMap<Integer, Personajes> characters;
    private final ConcurrentHashMap<Integer, InputState> playerInputs;
    private final CollisionManager collisionManager;
    private final CargarObjetos cargarObjetos;
    private final ConcurrentHashMap<Integer, Objetos> gameObjects = new ConcurrentHashMap<>();
    private int nextObjectId = 0;
    private final float mapWidth;
    private final float mapHeight;
    private final TiledMap map;
    private volatile boolean running = false;
    private long sequenceNumber = 0;

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
        spawnGameObjects(-1, "Anillo", "SpawnObjetos");
    }

    private void spawnGameObjects(int count, String objectType, String layerName) {

        List<Vector2> spawnPoints = MapUtil.findAllSpawnPoints(map, layerName, objectType);

        if (spawnPoints.isEmpty()) {
            Gdx.app.log("GameServer", "No spawn points found for " + objectType);
            return;
        }

        if (count == -1 || count > spawnPoints.size()) {
            count = spawnPoints.size();
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
                Gdx.app.log("GameServer", "Spawned " + objectType + " at " + spawnPoint);
            }
        }
    }

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
                    character = new Knuckles(playerId, assets.knucklesAtlas);
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

    public void start() {
        running = true;
        Thread gameLoopThread = new Thread(this::serverGameLoop);
        gameLoopThread.setDaemon(true);
        gameLoopThread.start();
    }

    public void stop() {
        running = false;
    }

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
                characterType
            ));
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

    public GameState getCurrentGameState() {
        return game.networkManager.getCurrentGameState();
    }
}
