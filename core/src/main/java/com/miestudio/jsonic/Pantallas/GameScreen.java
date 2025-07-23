package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.miestudio.jsonic.Actores.Knockles;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Actores.Personajes.AnimationType;
import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
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

public class GameScreen implements Screen {

    private final JuegoSonic game;
    private final int localPlayerId;
    private final boolean isHost;

    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private final ConcurrentHashMap<Integer, Personajes> characters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, InputState> playerInputs;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CollisionManager collisionManager;

    private float mapWidth, mapHeight;

    public GameScreen(JuegoSonic game, int localPlayerId) {
        Gdx.app.log("GameScreen", "Constructor llamado para localPlayerId: " + localPlayerId);
        this.game = game;
        this.localPlayerId = localPlayerId;
        this.isHost = (localPlayerId == 0);

        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

        this.playerInputs = game.networkManager.getPlayerInputs();

        // Cargar el mapa
        map = new TmxMapLoader().load(Constantes.MAPA_PATH + "Mapa.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        // Obtener tamaño del mapa
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        mapWidth = layer.getWidth() * layer.getTileWidth();
        mapHeight = layer.getHeight() * layer.getTileHeight();

        // Configurar cámara
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        // Crear sistema de colisiones
        collisionManager = new CollisionManager(map, "Colisiones", mapWidth, mapHeight);
        collisionManager.addTileCollisions(map, "Colisiones");

        // Si es host, inicializar GameServer
        if (isHost) {
            game.networkManager.initializeGameServer(map, collisionManager, mapWidth, mapHeight);
        }
    }

    private void initializeCharacters() {
        // Vacío - los personajes se crearán bajo demanda
    }

    private void createCharacter(int playerId, String characterType) {
        if (characters.containsKey(playerId)) return;

        Assets assets = game.getAssets();
        Personajes character = null;

        // Asegurarnos de que characterType no sea nulo
        if (characterType == null) {
            Gdx.app.error("GameScreen", "Tipo de personaje nulo para jugador " + playerId);
            characterType = "Sonic"; // Valor por defecto
        }

        // Crear el personaje según el tipo seleccionado
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
                Gdx.app.error("GameScreen", "Tipo de personaje desconocido: " + characterType + ", usando Sonic por defecto");
                character = new Sonic(playerId, assets.sonicAtlas);
                break;
        }

        if (character != null) {
            characters.put(playerId, character);

            // Posición inicial basada en el jugador
            float x = mapWidth * 0.1f + playerId * 50;
            float y = mapHeight * 0.5f;

            // Ajustar al suelo si es posible
            float groundY = collisionManager.getGroundY(new Rectangle(x, y, 50, 50));
            character.setPosition(x, groundY >= 0 ? groundY : y);
            character.setPreviousPosition(x, y);
            character.setPredictedPosition(x, y);

            Gdx.app.log("GameScreen", "Personaje creado: " + characterType + " para jugador " + playerId);
        }
    }

    private void updateFromGameState() {
        GameState gameState = game.networkManager.getCurrentGameState();
        if (gameState == null) return;

        synchronized (characters) {
            for (PlayerState playerState : gameState.getPlayers()) {

                int playerId = playerState.getPlayerId();

                String characterType = playerState.getCharacterType();

                // Si no tenemos tipo para este jugador, intentar obtenerlo del jugador local
                if (characterType == null && playerId == localPlayerId) {
                    characterType = game.networkManager.getSelectedCharacterType();
                }

                // Si todavía no tenemos tipo, usar uno por defecto
                if (characterType == null) {
                    characterType = "Sonic";
                    Gdx.app.error("GameScreen", "Tipo desconocido para jugador " + playerId + ", usando Sonic");
                }

                // Crear personaje si no existe
                if (!characters.containsKey(playerId)) {
                    createCharacter(playerId, characterType);
                }

                Personajes character = characters.get(playerId);
                if (character == null) continue;

                // Para todos los jugadores remotos y para el host
                if (isHost || playerId != localPlayerId) {
                    // Interpolación suave
                    float interpolationFactor = 0.3f;
                    float newX = character.getX() + (playerState.getX() - character.getX()) * interpolationFactor;
                    float newY = character.getY() + (playerState.getY() - character.getY()) * interpolationFactor;

                    character.setPosition(newX, newY);
                    character.setFacingRight(playerState.isFacingRight());
                    character.setAnimationStateTime(playerState.getAnimationStateTime());

                    // Actualizar animación
                    try {
                        Personajes.AnimationType animation = Personajes.AnimationType.valueOf(
                            playerState.getCurrentAnimationName().toUpperCase()
                        );
                        character.setAnimation(animation);
                    } catch (IllegalArgumentException e) {
                        character.setAnimation(Personajes.AnimationType.IDLE);
                    }
                }
                // Reconciliación solo para jugador local en cliente
                else if (!isHost) {
                    // Solo corregir si hay gran discrepancia
                    if (Vector2.dst(character.getX(), character.getY(),
                        playerState.getX(), playerState.getY()) > 10f) {
                        character.setPosition(playerState.getX(), playerState.getY());
                        character.setPredictedPosition(playerState.getX(), playerState.getY());
                    }
                    // Actualizar animación y dirección desde el estado
                    character.setFacingRight(playerState.isFacingRight());
                    character.setAnimationStateTime(playerState.getAnimationStateTime());
                    try {
                        Personajes.AnimationType animation = Personajes.AnimationType.valueOf(
                            playerState.getCurrentAnimationName().toUpperCase()
                        );
                        character.setAnimation(animation);
                    } catch (IllegalArgumentException e) {
                        character.setAnimation(Personajes.AnimationType.IDLE);
                    }
                }
            }
        }
    }
    private Map<String, Vector2> findSpawnPoints() {
        Map<String, Vector2> spawnPoints = new HashMap<>();
        MapLayer layer = map.getLayers().get("SpawnJugadores");

        if (layer == null || !(layer instanceof TiledMapTileLayer)) {
            Gdx.app.error("GameScreen", "No se encontró la capa de tiles 'SpawnJugadores'.");
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
                    }
                }
            }
        }
        return spawnPoints;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Actualizar desde el estado del juego (crea/actualiza personajes)
        updateFromGameState();

        // 2. Procesar entrada del jugador local
        processInput(delta);

        // 3. Obtener jugador local para centrar cámara
        Personajes localPlayer = characters.get(localPlayerId);

        // 4. Actualizar cámara
        if (localPlayer != null) {
            // Para centrar la cámara, usamos la posición predicha en clientes
            float posX = !isHost ? localPlayer.getPredictedX() : localPlayer.getX();
            float posY = !isHost ? localPlayer.getPredictedY() : localPlayer.getY();

            float cameraHalfWidth = camera.viewportWidth / 2;
            float cameraHalfHeight = camera.viewportHeight / 2;

            float cameraX = Math.max(cameraHalfWidth, Math.min(posX, mapWidth - cameraHalfWidth));
            float cameraY = Math.max(cameraHalfHeight, Math.min(posY, mapHeight - cameraHalfHeight));

            camera.position.set((int)cameraX, (int)cameraY, 0);
        }
        camera.update();

        // 5. Renderizar mapa
        mapRenderer.setView(camera);
        mapRenderer.render();

        // 6. Renderizar personajes
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        synchronized (characters) {
            for (Personajes character : characters.values()) {
                // Para jugador local en cliente: usar posición predicha
                if (!isHost && character.getPlayerId() == localPlayerId) {
                    character.setPosition(
                        character.getPredictedX(),
                        character.getPredictedY()
                    );
                }

                // Actualizar tiempo de animación
                character.setAnimationStateTime(character.getAnimationStateTime() + delta);

                TextureRegion frame = character.getCurrentFrame();

                // Aplicar flip horizontal según dirección
                if (!character.isFacingRight() && !frame.isFlipX()) {
                    frame.flip(true, false);
                } else if (character.isFacingRight() && frame.isFlipX()) {
                    frame.flip(true, false);
                }

                batch.draw(frame, character.getX(), character.getY());
            }
        }
        batch.end();

        // 7. Renderizar colisiones para debug (opcional)
        // debugRenderCollisions();
    }

    private void processInput(float delta) {
        if (isHost) {
            // Host: registrar su propio input
            InputState hostInput = new InputState();
            hostInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            hostInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            hostInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            hostInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            hostInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            hostInput.setPlayerId(localPlayerId);
            playerInputs.put(localPlayerId, hostInput);
        } else {
            // Cliente: enviar input al servidor y hacer predicción local
            InputState localInput = new InputState();
            localInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            localInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            localInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            localInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            localInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            localInput.setPlayerId(localPlayerId);
            game.networkManager.sendInputState(localInput);

            Personajes localPlayer = characters.get(localPlayerId);
            if (localPlayer != null) {
                // Guardar posición actual (para interpolación)
                float currentX = localPlayer.getX();
                float currentY = localPlayer.getY();

                // Aplicar física y input (esto actualiza la posición)
                localPlayer.update(delta, collisionManager);
                localPlayer.handleInput(localInput, collisionManager, delta);

                // Guardar posición predicha
                localPlayer.setPredictedPosition(localPlayer.getX(), localPlayer.getY());

                // Restaurar posición actual (para interpolación)
                localPlayer.setPosition(currentX, currentY);
            }
        }
    }

    private void debugRenderCollisions(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        Array<Shape2D> shapesCopy = new Array<>();
        shapesCopy.addAll(collisionManager.getCollisionShapes());

        for (Shape2D shape : shapesCopy) {
            if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = mapWidth / mapHeight;
        float viewportWidth = width;
        float viewportHeight = width / aspectRatio;

        if (viewportHeight > height) {
            viewportHeight = height;
            viewportWidth = height * aspectRatio;
        }

        camera.viewportWidth = viewportWidth;
        camera.viewportHeight = viewportHeight;
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        mapRenderer.dispose();
        for (Personajes character : characters.values()) {
            character.dispose();
        }

        if (isHost) {
            map.dispose();
        }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
