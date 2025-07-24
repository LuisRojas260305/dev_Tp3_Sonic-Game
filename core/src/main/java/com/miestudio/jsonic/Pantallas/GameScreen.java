package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
import com.miestudio.jsonic.Actores.Robot;
import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Server.domain.GameState;
import com.miestudio.jsonic.Server.domain.InputState;
import com.miestudio.jsonic.Server.domain.PlayerState;
import com.miestudio.jsonic.Util.*;


import java.util.concurrent.ConcurrentHashMap;

import com.miestudio.jsonic.Objetos.Anillo;
import com.miestudio.jsonic.Objetos.Objetos;
import com.miestudio.jsonic.Server.domain.ObjectState;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pantalla principal del juego donde se desarrolla la accion. Gestiona la logica de renderizado,
 * la entrada del jugador, la actualizacion del estado del juego y la sincronizacion de red.
 */
public class GameScreen implements Screen {

    private final JuegoSonic game; /** Instancia principal del juego. */
    private final int localPlayerId; /** ID del jugador local (0 para el host, >0 para clientes). */
    private final boolean isHost; /** Indica si esta instancia del juego es el host. */

    private final OrthographicCamera camera; /** Cámara ortográfica para la vista del juego. */
    private final SpriteBatch batch; /** Batch para renderizar texturas. */
    private final ShapeRenderer shapeRenderer; /** Renderer para dibujar formas de depuración. */

    private final ConcurrentHashMap<Integer, Personajes> characters; /** Mapa de personajes activos en el juego, indexados por ID de jugador. */
    private final ConcurrentHashMap<Integer, InputState> playerInputs; /** Mapa de estados de entrada de los jugadores. */

    private TiledMap map; /** El mapa del juego cargado. */
    private OrthogonalTiledMapRenderer mapRenderer; /** Renderer para el mapa de tiles. */
    private CollisionManager collisionManager; /** Gestor de colisiones del juego. */

    private float mapWidth, mapHeight; /** Dimensiones del mapa en píxeles. */

    private Texture[] parallaxLayers; /** Capas de textura para el efecto parallax de fondo. */
    private float[] parallaxSpeeds; /** Velocidades de desplazamiento para cada capa parallax. */
    private float parallaxWidth; /** Ancho de las capas parallax. */
    private float parallaxHeight; /** Altura de las capas parallax. */

    private final ConcurrentHashMap<Integer, Objetos> clientObjects = new ConcurrentHashMap<>(); /** Mapa de objetos del juego en el cliente, indexados por ID de objeto. */
    private TextureRegion anilloTexture; /** Textura del anillo (no utilizada directamente, se obtiene de Assets). */
    private float objectStateTime = 0; /** Tiempo de estado para la animacion de los objetos. */

    /**
     * Constructor de la pantalla de juego.
     * @param game La instancia principal del juego.
     * @param localPlayerId El ID del jugador local (0 para el host, >0 para clientes).
     */
    public GameScreen(JuegoSonic game, int localPlayerId) {
        this.game = game;
        this.localPlayerId = localPlayerId;
        this.isHost = (localPlayerId == 0);

        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

        this.characters = new ConcurrentHashMap<>();
        this.playerInputs = game.networkManager.getPlayerInputs();

        // Cargar el mapa del juego
        map = new TmxMapLoader().load(Constantes.MAPA_PATH + "Mapa.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        // Obtener las dimensiones del mapa a partir de la primera capa de tiles
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        mapWidth = layer.getWidth() * layer.getTileWidth();
        mapHeight = layer.getHeight() * layer.getTileHeight();

        // Configurar la camara del juego
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        // Inicializar el gestor de colisiones con el mapa y la capa de colisiones
        collisionManager = new CollisionManager(map, "Colisiones", mapWidth, mapHeight);
        collisionManager.addTileCollisions(map, "Colisiones");

        // Si esta instancia es el host, inicializar el GameServer
        if (isHost) {
            game.networkManager.initializeGameServer(map, collisionManager, mapWidth, mapHeight);
        }

        initParallaxBackground();
    }

    /**
     * Inicializa las capas y propiedades para el efecto de fondo parallax.
     */
    private void initParallaxBackground() {

        parallaxLayers = new Texture[] {
            new Texture(Gdx.files.internal(Constantes.BACKGROUND_GAMESCREEN_PATH + "Background5.png")),
            new Texture(Gdx.files.internal(Constantes.BACKGROUND_GAMESCREEN_PATH + "Background4.png")),
            new Texture(Gdx.files.internal(Constantes.BACKGROUND_GAMESCREEN_PATH + "Background3.png")),
            new Texture(Gdx.files.internal(Constantes.BACKGROUND_GAMESCREEN_PATH + "Background2.png")),
            new Texture(Gdx.files.internal(Constantes.BACKGROUND_GAMESCREEN_PATH + "Background1.png"))
        };

        parallaxSpeeds = new float[] {0.1f, 0.25f, 0.5f, 0.75f, 1.0f};

        parallaxWidth = mapWidth * 2;
        parallaxHeight = mapHeight;

        for (Texture texture : parallaxLayers){
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    /**
     * Renderiza las capas de fondo parallax, ajustando su posicion segun la camara
     * para crear el efecto de profundidad.
     */
    private void renderParallaxBackground() {
        if (parallaxLayers == null) return;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int i = 0; i < parallaxLayers.length; i++) {
            Texture layer = parallaxLayers[i];
            float speed = parallaxSpeeds[i];

            float x = -camera.position.x * speed % parallaxWidth;
            float y = camera.position.y - camera.viewportHeight/2;

            for (float offsetx = x; offsetx < camera.viewportWidth; offsetx += parallaxWidth) {
                batch.draw(
                    layer,
                    offsetx,
                    y,
                    parallaxWidth,
                    camera.viewportHeight
                );
            }
        }

        batch.end();
    }

    /**
     * Crea una instancia de personaje para un jugador dado, si aun no existe.
     * @param playerId El ID del jugador para el que se creara el personaje.
     * @param characterType El tipo de personaje a crear (ej. "Sonic", "Tails", "Knuckles").
     */
    private void createCharacter(int playerId, String characterType) {
        // Evitar crear el personaje si ya existe
        if (characters.containsKey(playerId)) return;

        Assets assets = game.getAssets();
        Personajes character = null;

        // Asegurarse de que el tipo de personaje no sea nulo; usar Sonic por defecto si lo es.
        if (characterType == null) {
            Gdx.app.error("GameScreen", "Tipo de personaje nulo para jugador " + playerId + ", usando Sonic por defecto.");
            characterType = "Sonic"; // Valor por defecto
        }

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
                // En caso de tipo desconocido, usar Sonic y registrar un error
                Gdx.app.error("GameScreen", "Tipo de personaje desconocido: " + characterType + ", usando Sonic por defecto.");
                character = new Sonic(playerId, assets.sonicAtlas);
                break;
        }

        // Si el personaje se creó correctamente, añadirlo al mapa de personajes
        if (character != null) {
            characters.put(playerId, character);

            // Establecer una posición inicial básica para el personaje (será sobrescrita por el GameState)
            character.setPosition(0, 0);
            character.setPreviousPosition(0, 0);
            character.setPredictedPosition(0, 0);

            Gdx.app.log("GameScreen", "Personaje creado: " + characterType + " para jugador " + playerId + ".");
        }
    }

    /**
     * Actualiza el estado de todos los personajes en la pantalla de juego basandose en el GameState
     * recibido del servidor. Realiza interpolacion para jugadores remotos y reconciliacion
     * para el jugador local en el cliente.
     */
    private void updateFromGameState() {
        GameState gameState = game.networkManager.getCurrentGameState();
        // Si no hay GameState disponible, no hacer nada
        if (gameState == null) return;

        synchronized (characters) {
            for (PlayerState playerState : gameState.getPlayers()) {
                int playerId = playerState.getPlayerId();
                String characterType = playerState.getCharacterType();

                // Si el tipo de personaje no está en el PlayerState (versiones antiguas o error),
                // intentar obtenerlo del NetworkManager para el jugador local.
                if (characterType == null && playerId == localPlayerId) {
                    characterType = game.networkManager.getSelectedCharacterType();
                }

                // Si aun no se tiene un tipo de personaje, usar "Sonic" como valor por defecto
                if (characterType == null) {
                    characterType = "Sonic";
                    Gdx.app.error("GameScreen", "Tipo de personaje desconocido para jugador " + playerId + ", usando Sonic por defecto.");
                }

                
                
                // Crear el personaje si aún no existe en el mapa de personajes
                if (!characters.containsKey(playerId)) {
                    createCharacter(playerId, characterType);
                }

                Personajes character = characters.get(playerId);
                // Si el personaje no se pudo crear o es nulo por alguna razón, saltar al siguiente
                if (character == null) continue;

                if (playerState.isFlying() && character instanceof Tails) {
                    ((Tails) character).setFlying(true);
                    character.setAnimation(Personajes.AnimationType.FLY);
                }
                
                // Lógica de actualización para jugadores remotos (clientes) y para el host
                if (isHost || playerId != localPlayerId) {
                    // Aplicar interpolación suave para un movimiento más fluido de los personajes remotos
                    float interpolationFactor = 0.3f; // Factor de interpolación (0.0 - 1.0)
                    float newX = character.getX() + (playerState.getX() - character.getX()) * interpolationFactor;
                    float newY = character.getY() + (playerState.getY() - character.getY()) * interpolationFactor;

                    character.setPosition(newX, newY);
                    character.setFacingRight(playerState.isFacingRight());
                    character.setAnimationStateTime(playerState.getAnimationStateTime());

                    // Actualizar la animación del personaje
                    try {
                        Personajes.AnimationType animation = Personajes.AnimationType.valueOf(
                            playerState.getCurrentAnimationName().toUpperCase()
                        );
                        character.setAnimation(animation);
                    } catch (IllegalArgumentException e) {
                        // Si el nombre de la animación no es válido, establecer la animación de inactividad
                        Gdx.app.error("GameScreen", "Animacion desconocida: " + playerState.getCurrentAnimationName() + ", estableciendo IDLE.");
                        character.setAnimation(Personajes.AnimationType.IDLE);
                    }
                }
                // Lógica de reconciliación para el jugador local en el cliente
                else if (!isHost) {
                    // Corregir la posición del jugador local solo si hay una discrepancia significativa
                    if (Vector2.dst(character.getX(), character.getY(),
                        playerState.getX(), playerState.getY()) > 10f) { // Umbral de 10 píxeles
                        character.setPosition(playerState.getX(), playerState.getY());
                        character.setPredictedPosition(playerState.getX(), playerState.getY());
                    }
                    // Actualizar la animación y la dirección del personaje local desde el estado del servidor
                    character.setFacingRight(playerState.isFacingRight());
                    character.setAnimationStateTime(playerState.getAnimationStateTime());
                    try {
                        Personajes.AnimationType animation = Personajes.AnimationType.valueOf(
                            playerState.getCurrentAnimationName().toUpperCase()
                        );
                        character.setAnimation(animation);
                    } catch (IllegalArgumentException e) {
                        // Si el nombre de la animación no es válido, establecer la animación de inactividad
                        Gdx.app.error("GameScreen", "Animacion desconocida: " + playerState.getCurrentAnimationName() + ", estableciendo IDLE.");
                        character.setAnimation(Personajes.AnimationType.IDLE);
                    }
                }
            }
        }
    }

    /**
     * Actualiza el estado de los objetos del juego en el cliente basandose en el GameState
     * recibido del servidor. Agrega nuevos objetos, actualiza existentes y elimina los que ya no estan activos.
     */
    private void updateObjectsFromGameState() {
        GameState gameState = game.networkManager.getCurrentGameState();
        if (gameState == null) return;

        synchronized (clientObjects) {
            // Actualizar objetos existentes y agregar nuevos
            for (ObjectState objState : gameState.getObjects()) {
                Objetos obj = clientObjects.get(objState.getId());
                if (obj == null) {
                    if ("Anillo".equals(objState.getType())) {
                        obj = new Anillo(objState.getX(), objState.getY(), game.getAssets().anilloAnimation);
                        obj.setId(objState.getId());
                        clientObjects.put(obj.getId(), obj);
                        Gdx.app.log("GameScreen", "Nuevo anillo creado: " + obj.getId());
                    }
                }

                if (obj != null) {
                    obj.x = objState.getX();
                    obj.y = objState.getY();
                    obj.setActivo(objState.isActive());
                }
            }

            // Eliminar objetos que ya no existen
            Iterator<ConcurrentHashMap.Entry<Integer, Objetos>> it = clientObjects.entrySet().iterator();
            while (it.hasNext()) {
                ConcurrentHashMap.Entry<Integer, Objetos> entry = it.next();
                boolean exists = false;
                for (ObjectState objState : gameState.getObjects()) {
                    if (objState.getId() == entry.getKey()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    it.remove();
                    Gdx.app.log("GameScreen", "Anillo removido: " + entry.getKey());
                }
            }
        }
    }

    /**
     * Actualiza la logica de los objetos del juego en el cliente.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos.
     */
    private void updateObjects(float delta){
        objectStateTime += delta;
        synchronized (clientObjects) {
            for (Objetos obj : clientObjects.values()){
                obj.actualizar(delta);
            }
        }
    }

    /**
     * Renderiza todos los objetos del juego en el cliente.
     */
    private void renderObjects() {
        batch.begin();
        synchronized (clientObjects) {
            for (Objetos obj : clientObjects.values()) {
                if (obj.estaActivo()) {
                    batch.draw(obj.getTexture(), obj.x, obj.y, 15f, 15f);
                }
            }
        }
        batch.end();
    }
    
    private void renderRobots() {
        batch.begin();
        for (Personajes character : characters.values()) {
            if (character instanceof Tails) {
                Tails tails = (Tails) character;
                for (Robot robot : tails.getActiveRobots()) {
                    TextureRegion frame = robot.getTexture();
                    if (!robot.isFacingRight() && !frame.isFlipX()) {
                        frame.flip(true, false);
                    } else if (robot.isFacingRight() && frame.isFlipX()) {
                        frame.flip(true, false);
                    }
                    batch.draw(frame, robot.getX(), robot.getY());
                }
            }
        }
        batch.end();
    }

    @Override
    public void render(float delta) {
        // Limpiar la pantalla con un color de fondo
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Actualizar el estado del juego a partir del GameState recibido (crea/actualiza personajes)
        updateFromGameState();

        // 2. Procesar la entrada del jugador local
        processInput(delta);

        // 3. Obtener la instancia del personaje local para centrar la cámara
        Personajes localPlayer = characters.get(localPlayerId);
        
        // 4. Actualizar la posición de la cámara
        if (localPlayer != null) {
            // Para centrar la cámara, se usa la posición predicha en clientes para suavizar el movimiento
            float posX = !isHost ? localPlayer.getPredictedX() : localPlayer.getX();
            float posY = !isHost ? localPlayer.getPredictedY() : localPlayer.getY();

            // Calcular los límites de la cámara para que no se salga del mapa
            float cameraHalfWidth = camera.viewportWidth / 2;
            float cameraHalfHeight = camera.viewportHeight / 2;

            float cameraX = Math.max(cameraHalfWidth, Math.min(posX, mapWidth - cameraHalfWidth));
            float cameraY = Math.max(cameraHalfHeight, Math.min(posY, mapHeight - cameraHalfHeight));

            // Establecer la posición de la cámara (usando enteros para evitar artefactos visuales)
            camera.position.set((int)cameraX, (int)cameraY, 0);
        }

        renderParallaxBackground();
        renderRobots();
        camera.update();

        // 5. Renderizar el mapa del juego
        mapRenderer.setView(camera);
        mapRenderer.render();

        updateObjectsFromGameState();
        updateObjects(delta);
        renderObjects();

        // 6. Renderizar todos los personajes
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        synchronized (characters) {
            for (Personajes character : characters.values()) {
                // Para el jugador local en el cliente, usar la posición predicha para el renderizado
                if (!isHost && character.getPlayerId() == localPlayerId) {
                    character.setPosition(
                        character.getPredictedX(),
                        character.getPredictedY()
                    );
                }

                // Actualizar el tiempo de estado de la animación para el renderizado
                character.setAnimationStateTime(character.getAnimationStateTime() + delta);

                TextureRegion frame = character.getCurrentFrame();

                // Aplicar flip horizontal a la textura si el personaje mira a la izquierda
                if (!character.isFacingRight() && !frame.isFlipX()) {
                    frame.flip(true, false);
                } else if (character.isFacingRight() && frame.isFlipX()) {
                    frame.flip(true, false);
                }

                batch.draw(frame, character.getX(), character.getY());
            }
        }
        batch.end();

        // 7. Renderizar colisiones para depuración (descomentar para activar)
        // debugRenderCollisions();
    }

    /**
     * Procesa la entrada del usuario.
     * Si es el host, registra su propio input. Si es un cliente, envía el input al servidor
     * y realiza una predicción local del movimiento del personaje.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     */
    private void processInput(float delta) {
        if (isHost) {
            // Host: Capturar y registrar el input del jugador local
            InputState hostInput = new InputState();
            hostInput.setLeft(Gdx.input.isKeyPressed(Input.Keys.A));
            hostInput.setRight(Gdx.input.isKeyPressed(Input.Keys.D));
            hostInput.setUp(Gdx.input.isKeyPressed(Input.Keys.W));
            hostInput.setDown(Gdx.input.isKeyPressed(Input.Keys.S));
            hostInput.setAbility(Gdx.input.isKeyPressed(Input.Keys.E));
            hostInput.setPlayerId(localPlayerId);
            playerInputs.put(localPlayerId, hostInput);
        } else {
            // Cliente: Capturar input, enviarlo al servidor y realizar predicción local
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
                // Guardar la posición actual del personaje antes de aplicar la física
                // Esto es para poder restaurarla después y usar la posición predicha para el renderizado
                float currentX = localPlayer.getX();
                float currentY = localPlayer.getY();

                // Aplicar la física y el input al personaje local para la predicción
                localPlayer.update(delta, collisionManager);
                localPlayer.handleInput(localInput, collisionManager, delta);

                // Guardar la posición resultante como la posición predicha
                localPlayer.setPredictedPosition(localPlayer.getX(), localPlayer.getY());

                // Restaurar la posición original para que el renderizado use la posición predicha
                localPlayer.setPosition(currentX, currentY);
            }
        }
    }

    /**
     * Renderiza las formas de colision para depuracion visual.
     * Este metodo es opcional y solo debe usarse para fines de depuracion.
     * Se recomienda mantenerlo comentado en versiones de produccion.
     */
    private void debugRenderCollisions(){
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Crear una copia de las formas de colisión para evitar problemas de concurrencia
        Array<Shape2D> shapesCopy = new Array<>();
        shapesCopy.addAll(collisionManager.getCollisionShapes());

        // Dibujar cada forma de colisión como un rectángulo
        for (Shape2D shape : shapesCopy) {
            if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        shapeRenderer.end();
    }

    /**
     * Se llama cuando la pantalla del juego cambia de tamaño.
     * Ajusta el viewport de la camara para mantener la relacion de aspecto del mapa.
     * @param width El nuevo ancho de la pantalla.
     * @param height La nueva altura de la pantalla.
     */
    @Override
    public void resize(int width, int height) {
        // Calcular la relación de aspecto del mapa
        float aspectRatio = mapWidth / mapHeight;
        float viewportWidth = width;
        float viewportHeight = width / aspectRatio;

        // Ajustar el viewport para que el mapa se ajuste a la pantalla sin distorsión
        if (viewportHeight > height) {
            viewportHeight = height;
            viewportWidth = height * aspectRatio;
        }

        // Actualizar la cámara con las nuevas dimensiones del viewport y centrarla
        camera.viewportWidth = viewportWidth;
        camera.viewportHeight = viewportHeight;
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    /**
     * Libera todos los recursos utilizados por la pantalla de juego.
     * Este metodo es llamado automaticamente cuando la pantalla es destruida.
     */
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        mapRenderer.dispose();
        // Disponer de los recursos de cada personaje
        for (Personajes character : characters.values()) {
            character.dispose();
        }

        // Si esta instancia es el host, también dispone del mapa
        if (isHost) {
            map.dispose();
        }
    }

    /**
     * Se llama cuando esta pantalla se convierte en la pantalla activa del juego.
     * No se utiliza actualmente.
     */
    @Override
    public void show() {}

    /**
     * Se llama cuando el juego se pausa.
     * No se utiliza actualmente.
     */
    @Override
    public void pause() {}

    /**
     * Se llama cuando el juego se reanuda desde un estado de pausa.
     * No se utiliza actualmente.
     */
    @Override
    public void resume() {}

    /**
     * Se llama cuando esta pantalla deja de ser la pantalla activa del juego.
     * No se utiliza actualmente.
     */
    @Override
    public void hide() {}
}
