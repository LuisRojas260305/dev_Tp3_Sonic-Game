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

/**
 * Pantalla principal del juego donde se desarrolla la acción.
 * Se encarga de renderizar el estado del juego, gestionar los inputs del jugador,
 * y sincronizar el estado entre el host y los clientes.
 */
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



    /**
     * Constructor de la pantalla de juego.
     *
     * @param game La instancia principal del juego.
     * @param localPlayerId El ID del jugador local (0 para el host, >0 para clientes).
     */
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

        // Si es host, inicializar GameServer con las instancias creadas aquí
        if (isHost) {
            game.networkManager.initializeGameServer(map, collisionManager, mapWidth, mapHeight);
        }
    }

    /**
     * Inicializa las instancias de los personajes para todos los jugadores.
     * Asigna un personaje a cada ID de jugador y establece sus posiciones iniciales.
     */
    private void initializeCharacters() {

    }

    // Método nuevo para crear personajes bajo demanda
    private void createCharacter(int playerId, String characterType) {
        if (characters.containsKey(playerId)) return;

        Assets assets = game.getAssets();
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
                Gdx.app.error("GameScreen", "Tipo de personaje desconocido: " + characterType);
                return;
        }

        if (character != null) {
            characters.put(playerId, character);

            // Establecer posición inicial (será actualizada por el primer GameState)
            Vector2 spawn = new Vector2(mapWidth * 0.1f + playerId * 50, mapHeight * 0.5f);
            character.setPosition(spawn.x, spawn.y);
            character.setPreviousPosition(spawn.x, spawn.y);
        }
    }

    // Método nuevo para actualizar desde GameState
    private void updateFromGameState() {
        GameState gameState = game.networkManager.getCurrentGameState();
        if (gameState != null) {
            synchronized (characters) {
                for (PlayerState playerState : gameState.getPlayers()) {
                    int playerId = playerState.getPlayerId();
                    String characterType = game.networkManager.getSelectedCharacters().get(playerId);

                    // Crear personaje si no existe
                    if (!characters.containsKey(playerId) && characterType != null) {
                        createCharacter(playerId, characterType);
                    }

                    Personajes character = characters.get(playerId);
                    if (character == null) continue;

                    // Solo aplicar estado completo para jugadores remotos
                    if (playerId != localPlayerId) {
                        character.setPosition(playerState.getX(), playerState.getY());
                        character.setFacingRight(playerState.isFacingRight());
                        character.setAnimationStateTime(playerState.getAnimationStateTime());

                        // Actualizar animación
                        try {
                            Personajes.AnimationType animation = Personajes.AnimationType.valueOf(
                                playerState.getCurrentAnimationName().toUpperCase()
                            );
                            character.setAnimation(animation);
                        } catch (IllegalArgumentException e) {
                            Gdx.app.error("GameScreen", "Animación desconocida: " + playerState.getCurrentAnimationName());
                        }
                    }
                    // Para el jugador local solo hacemos reconciliación si hay discrepancia
                    else if (Vector2.dst(character.getX(), character.getY(),
                        playerState.getX(), playerState.getY()) > 5f) {
                        character.setPosition(playerState.getX(), playerState.getY());
                    }
                }
            }
        }
    }

    /**
     * Encuentra los puntos de spawn de los jugadores en la capa "SpawnJugadores" del mapa.
     * Los puntos de spawn se definen como tiles con la propiedad "Spawn" establecida a true,
     * y una propiedad adicional ("Sonic", "Tails", "Knuckles") para identificar al personaje.
     * @return Un mapa donde la clave es el nombre del personaje y el valor es su posición de spawn (Vector2).
     */
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
                        Gdx.app.log("GameScreen", "Spawn encontrado para " + characterName + " en (" + spawnX + ", " + spawnY + ")");
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

        // 1. Actualizar desde el estado del juego (esto crea/actualiza los personajes)
        updateFromGameState();

        // 2. Procesar entrada del jugador local
        processInput(delta);

        // 3. Obtener jugador local para centrar cámara
        Personajes localPlayer = characters.get(localPlayerId);

        // 4. Actualizar cámara
        if (localPlayer != null) {
            // Centrar cámara en el jugador local
            float cameraHalfWidth = camera.viewportWidth / 2;
            float cameraHalfHeight = camera.viewportHeight / 2;

            float cameraX = Math.max(cameraHalfWidth,
                Math.min(localPlayer.getX(), mapWidth - cameraHalfWidth));
            float cameraY = Math.max(cameraHalfHeight,
                Math.min(localPlayer.getY(), mapHeight - cameraHalfHeight));

            // Usar coordenadas enteras para evitar artefactos visuales
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
                // Solo actualizar animaciones para jugadores remotos
                if (character.getPlayerId() != localPlayerId) {
                    character.setAnimationStateTime(character.getAnimationStateTime() + delta);
                }

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

        // 8. Actualizar estado de animación local (si es cliente)
        if (!isHost && localPlayer != null) {
            localPlayer.setAnimationStateTime(localPlayer.getAnimationStateTime() + delta);
        }
    }

    /**
     * Procesa la entrada del usuario, ya sea para el host (registrando el input) o para el cliente
     * (enviando el input al servidor y realizando predicción local).
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     */
    private void processInput(float delta) {
        if (isHost) {
            // El Host solo necesita registrar su propio input para que el GameServer lo procese.
            InputState hostInput = new InputState();
            hostInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            hostInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            hostInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            hostInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            hostInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            hostInput.setPlayerId(localPlayerId);
            playerInputs.put(localPlayerId, hostInput);
        } else {
            // El Cliente realiza la predicción local y envía su input al servidor.
            InputState localInput = new InputState();
            localInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            localInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            localInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            localInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            localInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            localInput.setPlayerId(localPlayerId);
            game.networkManager.sendInputState(localInput);

            // Predicción del lado del cliente: aplica la física y el input al jugador local.
            Personajes localPlayer = characters.get(localPlayerId);
            if (localPlayer != null) {
                localPlayer.update(delta, collisionManager);
                localPlayer.handleInput(localInput, collisionManager, delta);
            }

            // Actualiza los demás personajes basándose en el estado del servidor (interpolación).
            updateCharactersFromState();
        }
    }

    /**
     * Renderiza las formas de colisión para depuración visual.
     * Este método es opcional y solo debe usarse para fines de depuración.
     */
    private void debugRenderCollisions(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Crear copia para evitar problemas de concurrencia
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

    /**
     * Actualiza las posiciones y estados de los personajes en el cliente según el GameState recibido del servidor.
     * Realiza reconciliación para el jugador local y interpolación para los jugadores remotos.
     */
    private void updateCharactersFromState() {
        GameState gameState = game.networkManager.getCurrentGameState();
        if (gameState != null) {
            synchronized (characters) {
                for (PlayerState playerState : gameState.getPlayers()) {
                    Personajes character = characters.get(playerState.getPlayerId());
                    if (character == null) continue;

                    if (playerState.getPlayerId() == localPlayerId) {
                        // Reconciliación para el jugador local
                        float errorMargin = 0.5f; // Pequeño margen de error
                        if (Vector2.dst(character.getX(), character.getY(), playerState.getX(), playerState.getY()) > errorMargin) {
                            // Corrección suave si la predicción fue muy diferente
                            character.setPosition(playerState.getX(), playerState.getY());
                        }
                        character.setFacingRight(playerState.isFacingRight());
                        character.setAnimation(Personajes.AnimationType.valueOf(playerState.getCurrentAnimationName().toUpperCase()));
                        // Ajustar el stateTime de la animación al valor del servidor
                        character.setAnimationStateTime(playerState.getAnimationStateTime());
                    } else {
                        // Interpolación para los otros jugadores
                        float interpolationFactor = 0.2f; // Ajusta este valor para un movimiento más suave o más rápido
                        character.setPosition(
                            character.getX() + (playerState.getX() - character.getX()) * interpolationFactor,
                            character.getY() + (playerState.getY() - character.getY()) * interpolationFactor
                        );
                        character.setFacingRight(playerState.isFacingRight());
                        character.setAnimation(Personajes.AnimationType.valueOf(playerState.getCurrentAnimationName().toUpperCase()));
                        character.setAnimationStateTime(playerState.getAnimationStateTime());
                    }
                }


            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // Mantener relación de aspecto del mapa
        float aspectRatio = mapWidth / mapHeight;
        float viewportWidth = width;
        float viewportHeight = width / aspectRatio;

        if (viewportHeight > height) {
            viewportHeight = height;
            viewportWidth = height * aspectRatio;
        }

        // Centrar la cámara
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

        // Si es host, GameScreen es responsable de disponer el mapa y los sistemas
        if (isHost) {
            map.dispose();
        }
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
