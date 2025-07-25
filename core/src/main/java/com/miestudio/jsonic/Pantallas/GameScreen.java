package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.miestudio.jsonic.Actores.Egman;
import com.miestudio.jsonic.Actores.Knuckles;


import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Actores.Robot;
import com.miestudio.jsonic.Actores.Sonic;
import com.miestudio.jsonic.Actores.Tails;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Server.domain.GameState;
import com.miestudio.jsonic.Server.domain.InputState;
import com.miestudio.jsonic.Server.domain.PlayerState;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Util.CollectibleType;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.Assets;
import java.util.Map;


import java.util.concurrent.ConcurrentHashMap;

import com.miestudio.jsonic.Objetos.Anillo;
import com.miestudio.jsonic.Objetos.Objetos;
import com.miestudio.jsonic.Objetos.MaquinaReciclaje;
import com.miestudio.jsonic.Objetos.Arbol;
import com.miestudio.jsonic.Actores.EAvispa;
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

    private Array<Egman> egmans = new Array<>();

    private GameHub gameHub;
    private Vector2 cameraPosition = new Vector2();
    private Vector2 viewportSize = new Vector2();

    /**
     *
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
        float zoomLevel = 0.00025f;
        camera.setToOrtho(false, Gdx.graphics.getWidth() * zoomLevel, Gdx.graphics.getHeight() * zoomLevel);
        camera.update();

        // Inicializar el gestor de colisiones con el mapa y la capa de colisiones
        collisionManager = new CollisionManager(map, "Colisiones", mapWidth, mapHeight);
        collisionManager.addTileCollisions(map, "Colisiones");

        // Si esta instancia es el host, inicializar el GameServer
        if (isHost) {
            game.networkManager.initializeGameServer(map, collisionManager, mapWidth, mapHeight);
        }

        initGameHub();
        initEnemies();
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

    private void initGameHub() {
        gameHub = new GameHub();

        // Cargar texturas
        Texture bgTexture = new Texture(Gdx.files.internal("ui/Hub0.png"));
        Texture timeIcon = new Texture(Gdx.files.internal("ui/HubT.png"));
        Texture ringsIcon = new Texture(Gdx.files.internal("ui/HubR.png"));
        Texture trashIcon = new Texture(Gdx.files.internal("ui/HubB.png"));
        Texture recordIcon = new Texture(Gdx.files.internal("ui/HubE.png"));
        Texture livesIcon = new Texture(Gdx.files.internal("ui/HubLS.png"));

        // Crear componentes con posiciones relativas (0-1)
        // Tiempo (arriba a la izquierda)
        GameHub.HubComponent timeComp = new GameHub.HubComponent(
            GameHub.ComponentType.TIME,
            0.87f, 0.9f, // 5% desde izquierda, 90% desde abajo
            200, 40,
            40,
            bgTexture,
            timeIcon,
            gameHub.systemFont // Usar fuente del sistema
        );

        // Anillos (a la derecha del tiempo)
        GameHub.HubComponent ringsComp = new GameHub.HubComponent(
            GameHub.ComponentType.RINGS,
            0.87f, 0.8f, // 20% desde izquierda, 90% desde abajo
            200, 40,
            40,
            bgTexture,
            ringsIcon,
            gameHub.systemFont
        );

        // Basura (a la derecha de anillos)
        GameHub.HubComponent trashComp = new GameHub.HubComponent(
            GameHub.ComponentType.TRASH,
            0.87f, 0.7f, // 35% desde izquierda, 90% desde abajo
            200, 40,
            40,
            bgTexture,
            trashIcon,
            gameHub.systemFont
        );

        // Récord (arriba a la derecha)
        GameHub.HubComponent recordComp = new GameHub.HubComponent(
            GameHub.ComponentType.RECORD,
            0.87f, 0.6f, // 75% desde izquierda, 90% desde abajo
            200, 40,
            40,
            bgTexture,
            recordIcon,
            gameHub.systemFont
        );

        // Vidas (a la izquierda del récord)
        GameHub.HubComponent livesComp = new GameHub.HubComponent(
            GameHub.ComponentType.LIVES,
            0.87f, 0.5f, // 60% desde izquierda, 90% desde abajo
            200, 40,
            40,
            bgTexture,
            livesIcon,
            gameHub.systemFont
        );
        livesComp.valueColor = Color.GREEN;

        // Agregar componentes al hub
        gameHub.addComponent(timeComp);
        gameHub.addComponent(ringsComp);
        gameHub.addComponent(trashComp);
        gameHub.addComponent(recordComp);
        gameHub.addComponent(livesComp);

        // Establecer valores iniciales
        gameHub.updateRecord(1500);
        gameHub.updateLives(3);
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
                character = new Tails(playerId, assets.tailsAtlas, game.networkManager.getGameServer());
                break;
            case "Knuckles":
                character = new Knuckles(playerId, assets.knucklesAtlas);
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

                // Actualizar los coleccionables del personaje
                for (Map.Entry<CollectibleType, Integer> entry : playerState.getCollectibles().entrySet()) {
                    character.setCollectibleCount(entry.getKey(), entry.getValue());
                }
                character.setLives(playerState.getLives()); // Sincronizar vidas
                } else if (playerState.isAvispa()) {
                    // Si es una avispa, actualizar su estado
                    if (!characters.containsKey(playerId)) {
                        // Crear la avispa si no existe
                        EAvispa avispa = new EAvispa(
                            playerState.getX(),
                            playerState.getY(),
                            game.getAssets().enemyAtlas,
                            new Vector2(playerState.getTargetX(), playerState.getTargetY())
                        );
                        avispa.setPlayerId(playerId);
                        characters.put(playerId, avispa);
                    }
                    EAvispa avispa = (EAvispa) characters.get(playerId);
                    avispa.setPosition(playerState.getX(), playerState.getY());
                    avispa.setFacingRight(playerState.isFacingRight());
                    avispa.setAnimationStateTime(playerState.getAnimationStateTime());
                    avispa.setActivo(playerState.isActive()); // Sincronizar estado activo
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
                    // Actualizar los coleccionables del personaje local
                    for (Map.Entry<CollectibleType, Integer> entry : playerState.getCollectibles().entrySet()) {
                        character.setCollectibleCount(entry.getKey(), entry.getValue());
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
                    } else if ("Basura".equals(objState.getType())) {
                        obj = new Objetos(objState.getX(), objState.getY(), new TextureRegion(game.getAssets().trashTexture)) {
                            @Override
                            public void actualizar(float delta) {
                                // La basura no tiene animacion ni logica de actualizacion compleja
                            }
                        };
                        obj.setId(objState.getId());
                        clientObjects.put(obj.getId(), obj);
                        Gdx.app.log("GameScreen", "Nueva basura creada: " + obj.getId());
                    } else if ("MaquinaReciclaje".equals(objState.getType())) {
                        obj = new MaquinaReciclaje(objState.getX(), objState.getY(), game.getAssets().maquinaAtlas);
                        obj.setId(objState.getId());
                        clientObjects.put(obj.getId(), obj);
                        Gdx.app.log("GameScreen", "Nueva MaquinaReciclaje creada: " + obj.getId());
                    } else if ("Arbol".equals(objState.getType())) {
                        obj = new Arbol(objState.getX(), objState.getY(), new TextureRegion(game.getAssets().treeTexture));
                        obj.setId(objState.getId());
                        clientObjects.put(obj.getId(), obj);
                        Gdx.app.log("GameScreen", "Nuevo Arbol creado: " + obj.getId());
                    } else if ("Roca".equals(objState.getType())) {
                        obj = new Objetos(objState.getX(), objState.getY(), new TextureRegion(game.getAssets().rockTexture)) {
                            @Override
                            public void actualizar(float delta) {
                                // La roca no tiene animación ni lógica de actualización compleja
                            }
                        };
                        obj.setId(objState.getId());
                        clientObjects.put(obj.getId(), obj);
                        Gdx.app.log("GameScreen", "Nueva Roca creada: " + obj.getId());
                    }
                }

                if (obj != null) {
                    obj.x = objState.getX();
                    obj.y = objState.getY();
                    obj.setActivo(objState.isActive());
                    if (obj instanceof MaquinaReciclaje) {
                        ((MaquinaReciclaje) obj).setTotalCollectedTrash(objState.getTotalCollectedTrash());
                    }
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
                    batch.draw(obj.getTexture(), obj.x, obj.y, obj.getWidth(), obj.getHeight());
                }
            }
        }
        batch.end();
    }

    private void renderRobots() {
        batch.begin();
        // Obtener la lista de robots activos del GameServer
        if (game.networkManager.getGameServer() != null) {
            for (Robot robot : game.networkManager.getGameServer().getActiveRobots()) {
                TextureRegion frame = robot.getTexture();
                if (!robot.isFacingRight() && !frame.isFlipX()) {
                    frame.flip(true, false);
                } else if (robot.isFacingRight() && frame.isFlipX()) {
                    frame.flip(true, false);
                }
                batch.draw(frame, robot.getX(), robot.getY());
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

        if (localPlayer != null) {
            cameraPosition.set(camera.position.x, camera.position.y);
            viewportSize.set(camera.viewportWidth, camera.viewportHeight);

            // Actualizar el HUD con los valores del jugador local
            gameHub.updateCollectibleCount(GameHub.ComponentType.RINGS, localPlayer.getCollectibleCount(CollectibleType.RINGS));
            gameHub.updateCollectibleCount(GameHub.ComponentType.TRASH, localPlayer.getCollectibleCount(CollectibleType.TRASH));
            gameHub.updateLives(localPlayer.getLives()); // Actualizar vidas en el HUD
        }

        // Actualizar el hub con la posición de la cámara




        renderParallaxBackground();
        renderRobots();
        camera.update();

        // 5. Renderizar el mapa del juego
        mapRenderer.setView(camera);
        mapRenderer.render();

        updateObjectsFromGameState();
        updateObjects(delta);
        renderObjects();

        updateEnemies(delta);
        renderEnemies();

        // 6. Renderizar todos los personajes
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        synchronized (characters) {
            gameHub.render(batch, cameraPosition, viewportSize);
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
        // Mantener el zoom actual al redimensionar
        float currentZoom = camera.zoom;

        // Calcular la relación de aspecto del mapa
        float aspectRatio = mapWidth / mapHeight;
        float viewportWidth = width * currentZoom;
        float viewportHeight = width * currentZoom / aspectRatio;

        // Ajustar el viewport para que el mapa se ajuste a la pantalla sin distorsión
        if (viewportHeight > height * currentZoom) {
            viewportHeight = height * currentZoom;
            viewportWidth = height * aspectRatio * currentZoom;
        }

        // Actualizar la cámara con las nuevas dimensiones del viewport y centrarla
        camera.viewportWidth = viewportWidth;
        camera.viewportHeight = viewportHeight;
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    private void initEnemies() {
        // Cargar animaciones
        Animation<TextureRegion> egmanWalk = new Animation<>(
            0.1f,
            game.getAssets().egmanAtlas.findRegions("EgE0"),
            Animation.PlayMode.LOOP
        );




        egmans.add(new Egman(800f, 1200f, 250f, egmanWalk, 60f));
    }

    private void updateEnemies(float delta) {
        for (Egman egman : egmans) {
            egman.update(delta, collisionManager);
        }
    }

    private void renderEnemies() {
        batch.begin();
        for (Egman egman : egmans) {
            TextureRegion frame = egman.getCurrentFrame();

            // Voltear la animación según la dirección
            if (!egman.isMovingRight() && !frame.isFlipX()) {
                frame.flip(true, false);
            } else if (egman.isMovingRight() && frame.isFlipX()) {
                frame.flip(true, false);
            }

            batch.draw(frame, egman.getX(), egman.getY());
        }
        batch.end();
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

        gameHub.dispose();
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
