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
import com.miestudio.jsonic.Objetos.MaquinaReciclaje;
import com.miestudio.jsonic.Objetos.Arbol;
import com.miestudio.jsonic.Server.domain.InputState;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
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
    private float gameTimeRemaining = 600f; // 10 minutos en segundos
    private static final int TRASH_EVENT_THRESHOLD = 5; // Cantidad de basura para activar el evento
    private final List<Vector2> treeSpawnPoints = new ArrayList<>();
    private final Map<Vector2, Boolean> treeSpawnPointsOccupancy = new HashMap<>(); // true si está ocupado
    private int treesOnMap = 0;
    private int totalTreeSpawnPoints = 0;
    private boolean egmanEventTriggered = false;
    private final List<Vector2> trashSpawnPoints = new ArrayList<>();
    private final Map<Vector2, Boolean> trashSpawnPointsOccupancy = new HashMap<>();
    private float trashSpawnTimer = 0f;
    private static final float INITIAL_TRASH_DELAY = 90f; // 1 minuto y 30 segundos
    private static final float TRASH_SPAWN_INTERVAL = 2f; // 2 segundos
    private static final float TRASH_SPAWN_ACCELERATION_TIME = 300f; // 5 minutos en segundos
    private boolean initialTrashSpawned = false;
    private boolean trashSpawnAccelerated = false;
    private int totalTrashSpawnPoints; // Total de puntos de spawn de basura
    private int occupiedTrashSpawnPoints; // Puntos de spawn de basura ocupados
    private final Array<Robot> activeRobots = new Array<>();
    private Egman egman; // Referencia a Egman
    private List<MapUtil.EgmanPathPoint> egmanPath;
    private int currentPathIndex;
    private boolean egmanMoving;
    private Vector2 egmanTargetPosition;
    private static final float EGMAN_SPEED = 100f; // Velocidad de Egman
    private Random random = new Random();
    private GameState.GameStatus currentGameStatus = GameState.GameStatus.PLAYING;

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
        this.cargarObjetos = new CargarObjetos(game.getAssets().objetosAtlas, game.getAssets());
        initializeCharacters();
        spawnGameObjects("Anillo", "SpawnObjetos");
        initializeTrashSpawn();
        spawnGameObjects("Maquina", "SpawnEntidades");
        spawnGameObjects("Roca", "SpawnObjetos");
        initializeTreeSpawnPoints();

        // Instanciar Egman fuera de la vista
        Animation<TextureRegion> egmanWalk = new Animation<>(
            0.1f,
            game.getAssets().egmanAtlas.findRegions("EgE0"),
            Animation.PlayMode.LOOP
        );
        egman = new Egman(mapWidth + 100f, mapWidth + 200f, 100f, egmanWalk, 60f); // Posición inicial fuera de la vista
        egmanMoving = false;
        initializeEgmanPath();
    }

    private void initializeEgmanPath() {
        List<MapUtil.EgmanPathPoint> allPoints = MapUtil.findAllEgmanPathPoints(map, "Egman");
        egmanPath = new ArrayList<>();

        // Encontrar el punto de inicio
        MapUtil.EgmanPathPoint startPoint = null;
        for (MapUtil.EgmanPathPoint point : allPoints) {
            if ("Inicio".equals(point.type)) {
                startPoint = point;
                break;
            }
        }

        if (startPoint == null) {
            Gdx.app.error("GameServer", "No se encontró el punto de inicio de Egman.");
            return;
        }
        egmanPath.add(startPoint);

        // Añadir puntos de recorrido
        List<MapUtil.EgmanPathPoint> routePoints = new ArrayList<>();
        for (MapUtil.EgmanPathPoint point : allPoints) {
            if ("Recorrido".equals(point.type)) {
                routePoints.add(point);
            }
        }
        // Ordenar los puntos de recorrido si es necesario (por ejemplo, por X o Y si la ruta es lineal)
        // Por ahora, los añadimos tal cual
        egmanPath.addAll(routePoints);

        // Encontrar el punto final
        MapUtil.EgmanPathPoint endPoint = null;
        for (MapUtil.EgmanPathPoint point : allPoints) {
            if ("Fin".equals(point.type)) {
                endPoint = point;
                break;
            }
        }

        if (endPoint == null) {
            Gdx.app.error("GameServer", "No se encontró el punto final de Egman.");
            return;
        }
        egmanPath.add(endPoint);

        Gdx.app.log("GameServer", "Ruta de Egman inicializada con " + egmanPath.size() + " puntos.");
    }

    private void triggerEgmanEvent() {
        Gdx.app.log("GameServer", "Evento de Egman activado. Egman añadido a characters.");
        egmanMoving = true;
        currentPathIndex = 0;
        egman.setPosition(egmanPath.get(currentPathIndex).position.x, egmanPath.get(currentPathIndex).position.y);
        egmanTargetPosition = egmanPath.get(currentPathIndex).position;
        characters.put(egman.getPlayerId(), egman); // Añadir Egman a los personajes activos
    }

    private void initializeTrashSpawn() {
        List<Vector2> allTrashSpawns = MapUtil.findAllSpawnPoints(map, "SpawnObjetos", "Basura");
        Collections.shuffle(allTrashSpawns);

        totalTrashSpawnPoints = allTrashSpawns.size();
        occupiedTrashSpawnPoints = 0;

        int numToSpawn = allTrashSpawns.size() / 2; // 50% de la basura

        for (int i = 0; i < allTrashSpawns.size(); i++) {
            Vector2 spawnPoint = allTrashSpawns.get(i);
            trashSpawnPoints.add(spawnPoint);
            if (i < numToSpawn) {
                spawnTrash(spawnPoint);
                trashSpawnPointsOccupancy.put(spawnPoint, true);
                occupiedTrashSpawnPoints++;
            } else {
                trashSpawnPointsOccupancy.put(spawnPoint, false);
            }
        }
        Gdx.app.log("GameServer", "Basura inicial spawneada: " + numToSpawn + ". Puntos de spawn de basura inicializados: " + trashSpawnPoints.size());
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
            float objectWidth = 0;
            float objectHeight = 0;

            if ("Anillo".equals(objectType)) {
                objectWidth = 15f; // Tamaño fijo para anillos
                objectHeight = 15f;
            } else if ("Basura".equals(objectType)) {
                objectWidth = game.getAssets().trashTexture.getWidth();
                objectHeight = game.getAssets().trashTexture.getHeight();
            } else if ("Maquina".equals(objectType)) {
                objectWidth = 64f; // 2x2 tiles (32x32 por tile)
                objectHeight = 64f;
            }

            float centeredX = spawnPoint.x + (tileWigth - objectWidth) / 2;
            float finalY;

            if ("Anillo".equals(objectType)) {
                // Anillos centrados verticalmente en el tile
                finalY = spawnPoint.y + (tileHeight - objectHeight) / 2;
            } else {
                // Basura y MaquinaReciclaje en la base del tile
                finalY = spawnPoint.y;
            }

            // Calcular la posición Y final para que el objeto esté sobre el suelo
            Rectangle tempRect = new Rectangle(centeredX, finalY, objectWidth, objectHeight);
            float groundY = collisionManager.getGroundY(tempRect);
            finalY = (groundY >= 0) ? groundY : finalY;

            if ("Anillo".equals(objectType)) {
                Anillo anillo = new Anillo(centeredX, finalY, cargarObjetos.animacionAnillo);
                anillo.setId(nextObjectId++);
                gameObjects.put(anillo.getId(), anillo);
                cargarObjetos.agregarObjeto(anillo);
            } else if ("Basura".equals(objectType)) {
                // La basura ahora se spawnea a través de initializeTrashSpawn y spawnTrash
            } else if ("Maquina".equals(objectType)) {
                MaquinaReciclaje maquina = new MaquinaReciclaje(centeredX, finalY, game.getAssets().maquinaAtlas, game.getAssets());
                maquina.setId(nextObjectId++);
                gameObjects.put(maquina.getId(), maquina);
                cargarObjetos.agregarObjeto(maquina);
            } else if ("Roca".equals(objectType)) {
                Objetos roca = new Objetos(centeredX, finalY, new TextureRegion(game.getAssets().rockTexture)) {
                    @Override
                    public void actualizar(float delta) {
                        // La roca no tiene animación ni lógica de actualización compleja
                    }
                };
                roca.setId(nextObjectId++);
                gameObjects.put(roca.getId(), roca);
                cargarObjetos.agregarObjeto(roca);
            }
        }
        Gdx.app.log("GameServer", "Se han creado " + spawnPoints.size() + " " + objectType + "s.");
    }

    private void initializeTreeSpawnPoints() {
        List<Vector2> spawns = MapUtil.findAllSpawnPoints(map, "SpawnObjetos", "Arbol");
        for (Vector2 spawn : spawns) {
            treeSpawnPoints.add(spawn);
            treeSpawnPointsOccupancy.put(spawn, false); // Inicialmente desocupado
        }
        totalTreeSpawnPoints = treeSpawnPoints.size();
        Gdx.app.log("GameServer", "Puntos de spawn de árboles inicializados: " + treeSpawnPoints.size());
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
                    character = new Tails(playerId, assets.tailsAtlas, this);
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
                character.setLives(3); // Establecer vidas iniciales
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

        // Decrementar tiempo de juego
        gameTimeRemaining -= delta;
        if (gameTimeRemaining < 0) {
            gameTimeRemaining = 0; // Asegurarse de que no sea negativo
        }

        // Lógica de victoria/derrota
        if (currentGameStatus == GameState.GameStatus.PLAYING) {
            if (treesOnMap == totalTreeSpawnPoints && gameTimeRemaining > 0) {
                currentGameStatus = GameState.GameStatus.WON;
                Gdx.app.log("GameServer", "¡VICTORIA! Todos los árboles plantados a tiempo.");
            } else if (gameTimeRemaining <= 0 && treesOnMap < totalTreeSpawnPoints) {
                currentGameStatus = GameState.GameStatus.LOST;
                Gdx.app.log("GameServer", "¡DERROTA! Tiempo agotado y no todos los árboles plantados.");
            } else if (occupiedTrashSpawnPoints == totalTrashSpawnPoints) {
                currentGameStatus = GameState.GameStatus.LOST;
                Gdx.app.log("GameServer", "¡DERROTA! Todos los puntos de basura están llenos.");
            }
        }

        // Lógica de aparición gradual de basura
        trashSpawnTimer += delta;
        if (!initialTrashSpawned) {
            if (trashSpawnTimer >= INITIAL_TRASH_DELAY) {
                initialTrashSpawned = true;
                trashSpawnTimer = 0; // Reiniciar para el intervalo de 2s
                Gdx.app.log("GameServer", "Inicio de aparición gradual de basura.");
            }
        } else {
            if (!trashSpawnAccelerated && gameTimeRemaining <= (600f - TRASH_SPAWN_ACCELERATION_TIME)) { // 5 minutos restantes
                trashSpawnAccelerated = true;
                Gdx.app.log("GameServer", "Acelerando aparición de basura.");
            }

            float currentSpawnInterval = trashSpawnAccelerated ? 1.0f : TRASH_SPAWN_INTERVAL; // 1 segundo si acelerado, 2 segundos si no
            int spawnCount = trashSpawnAccelerated ? 2 : 1; // 2 basuras si acelerado, 1 si no

            if (trashSpawnTimer >= currentSpawnInterval) {
                spawnTrashAtRandomAvailablePoints(spawnCount);
                trashSpawnTimer = 0; // Reiniciar para el próximo intervalo
            }
        }

        // Lógica de activación del evento de Egman
        Gdx.app.log("EgmanEvent", "treesOnMap: " + treesOnMap + ", totalTreeSpawnPoints: " + totalTreeSpawnPoints + ", egmanEventTriggered: " + egmanEventTriggered);
        if (!egmanEventTriggered && totalTreeSpawnPoints > 0 && (float) treesOnMap / totalTreeSpawnPoints >= 0.75f) {
            triggerEgmanEvent();
            egmanEventTriggered = true;
        }

        // Lógica de movimiento de Egman
        if (egmanMoving && egman != null) {
            float oldX = egman.getX();
            float oldY = egman.getY();

            Vector2 currentTarget = egmanPath.get(currentPathIndex).position;
            float distance = Vector2.dst(egman.getX(), egman.getY(), currentTarget.x, currentTarget.y);

            if (distance > EGMAN_SPEED * delta) {
                Vector2 direction = currentTarget.cpy().sub(egman.getX(), egman.getY()).nor();
                egman.setPosition(egman.getX() + direction.x * EGMAN_SPEED * delta, egman.getY() + direction.y * EGMAN_SPEED * delta);
            } else {
                egman.setPosition(currentTarget.x, currentTarget.y);
                currentPathIndex++;
                if (currentPathIndex < egmanPath.size()) {
                    egmanTargetPosition = egmanPath.get(currentPathIndex).position;
                } else {
                    // Egman ha llegado al final de su ruta
                    egmanMoving = false;
                    egman.setActivo(false); // Desactivar Egman
                    characters.remove(egman.getPlayerId()); // Eliminar de la lista de personajes activos
                    Gdx.app.log("GameServer", "Egman ha completado su ruta y ha sido desactivado.");
                }
            }

            // Lógica de destrucción de árboles al pasar por un spawnpoint
            for (Map.Entry<Vector2, Boolean> entry : treeSpawnPointsOccupancy.entrySet()) {
                if (entry.getValue() && egman.getBounds().contains(entry.getKey())) { // Si el spawnpoint está ocupado y Egman lo pisa
                    if (random.nextFloat() < 0.35f) { // 35% de probabilidad
                        destroyTreeAtSpawnPoint(entry.getKey());
                        spawnTrashAtRandomAvailablePoints((int) (trashSpawnPoints.size() * 0.20f)); // 20% de los puntos de basura totales
                    }
                }
            }
        }

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
                (character instanceof Tails) ? ((Tails) character).isFlying() : false,
                character.getCollectibles(),
                false, // isAvispa
                0f, // targetX
                0f, // targetY
                character.estaActivo(), // active
                character.getLives() // lives
            ));
        }

        // Eliminar avispas inactivas

        // Actualizar robots activos
        for (int i = activeRobots.size - 1; i >= 0; i--) {
            Robot robot = activeRobots.get(i);
            robot.update(delta, collisionManager);
            if (!robot.isActive()) {
                activeRobots.removeIndex(i);
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
                    "Anillo",
                    0 // Anillos no tienen totalCollectedTrash
                ));
            } else if (obj.getTexture().getTexture() == game.getAssets().trashTexture) {
                objectStates.add(new ObjectState(
                    obj.getId(),
                    obj.x,
                    obj.y,
                    obj.estaActivo(),
                    "Basura",
                    0 // Basura no tiene totalCollectedTrash
                ));
            } else if (obj instanceof MaquinaReciclaje) {
                objectStates.add(new ObjectState(
                    obj.getId(),
                    obj.x,
                    obj.y,
                    obj.estaActivo(),
                    "MaquinaReciclaje",
                    ((MaquinaReciclaje) obj).getTotalCollectedTrash()
                ));
            } else if (obj instanceof Arbol) {
                objectStates.add(new ObjectState(
                    obj.getId(),
                    obj.x,
                    obj.y,
                    obj.estaActivo(),
                    "Arbol",
                    0 // Los árboles no tienen totalCollectedTrash
                ));
            } else if (obj.getTexture().getTexture() == game.getAssets().rockTexture) {
                objectStates.add(new ObjectState(
                    obj.getId(),
                    obj.x,
                    obj.y,
                    obj.estaActivo(),
                    "Roca",
                    0 // Las rocas no tienen totalCollectedTrash
                ));
            }
        }

        // Actualizar y transmitir GameState
        GameState newGameState = new GameState(playerStates, objectStates, sequenceNumber++, gameTimeRemaining, currentGameStatus);
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
                    if (obj instanceof Anillo) {
                        character.addCollectible(CollectibleType.RINGS);
                        obj.setActivo(false);
                        Gdx.app.log("GameServer", "Anillo con ID: " + obj.getId() + " desactivado por el jugador " + character.getPlayerId());
                    } else if (obj.getTexture().getTexture() == game.getAssets().trashTexture) {
                        if (character.getCollectibleCount(CollectibleType.TRASH) < 50) {
                            character.addCollectible(CollectibleType.TRASH);
                            obj.setActivo(false);
                            Gdx.app.log("GameServer", "Basura con ID: " + obj.getId() + " desactivado por el jugador " + character.getPlayerId());
                        } else {
                            Gdx.app.log("GameServer", "Jugador " + character.getPlayerId() + " tiene la basura llena. No puede recoger más.");
                        }
                    } else if (obj instanceof MaquinaReciclaje) {
                        MaquinaReciclaje maquina = (MaquinaReciclaje) obj;
                        int playerTrash = character.getCollectibleCount(CollectibleType.TRASH);

                        if (playerTrash > 0) {
                            maquina.addTrash(playerTrash);
                            character.setCollectibleCount(CollectibleType.TRASH, 0);
                            Gdx.app.log("GameServer", "Jugador " + character.getPlayerId() + " depositó " + playerTrash + " de basura en la máquina.");

                            // Lógica del evento de la máquina
                            int eventsTriggered = maquina.getTotalCollectedTrash() / TRASH_EVENT_THRESHOLD;
                            int remainingTrash = maquina.getTotalCollectedTrash() % TRASH_EVENT_THRESHOLD;

                            for (int i = 0; i < eventsTriggered; i++) {
                                Gdx.app.log("GameServer", "¡Evento de máquina de reciclaje activado! Basura total: " + maquina.getTotalCollectedTrash());
                                spawnTreeAtAvailablePoint();
                                character.setLives(character.getLives() + 1); // Incrementar vidas
                                Gdx.app.log("GameServer", "Vidas de jugador " + character.getPlayerId() + " aumentadas a " + character.getLives());
                            }
                            // Reiniciar el contador de la máquina para el próximo ciclo del evento
                            maquina.addTrash(- (eventsTriggered * TRASH_EVENT_THRESHOLD)); // Restar la basura que activó el evento
                            maquina.addTrash(remainingTrash); // Añadir el remanente
                        }
                    } else if (obj.getTexture().getTexture() == game.getAssets().rockTexture) {
                        // Lógica para la interacción con la roca (ej. Knuckles la destruye)
                        if (character instanceof Knuckles && ((Knuckles) character).isAbilityActive()) {
                            obj.setActivo(false);
                            Gdx.app.log("GameServer", "Roca con ID: " + obj.getId() + " destruida por Knuckles.");
                        }
                    }
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

    private void spawnTreeAtAvailablePoint() {
        Vector2 availableSpawnPoint = null;
        for (Map.Entry<Vector2, Boolean> entry : treeSpawnPointsOccupancy.entrySet()) {
            if (!entry.getValue()) { // Si no está ocupado
                availableSpawnPoint = entry.getKey();
                break;
            }
        }

        if (availableSpawnPoint == null) {
            Gdx.app.log("GameServer", "Todos los puntos de spawn de árboles están ocupados.");
            return;
        }

        // Marcar el punto de spawn como ocupado
        treeSpawnPointsOccupancy.put(availableSpawnPoint, true);

        // Spawnea el árbol
        spawnTree(availableSpawnPoint);
    }

    private void spawnTree(Vector2 position) {
        Arbol arbol = new Arbol(position.x, position.y, new TextureRegion(game.getAssets().treeTexture));
        arbol.setId(nextObjectId++);
        gameObjects.put(arbol.getId(), arbol);
        cargarObjetos.agregarObjeto(arbol);
        treesOnMap++;
        Gdx.app.log("GameServer", "Árbol spawneado en " + position + ". Total de árboles: " + treesOnMap);
    }

    public void freeTreeSpawnPoint(Vector2 position) {
        if (treeSpawnPointsOccupancy.containsKey(position)) {
            treeSpawnPointsOccupancy.put(position, false);
            Gdx.app.log("GameServer", "Punto de spawn de árbol liberado: " + position);
        }
    }

    public void spawnRobot(float x, float y, boolean facingRight, float speed, TextureRegion texture) {
        // Encontrar la máquina de reciclaje más cercana para el robot
        MaquinaReciclaje nearestMachine = getRecyclingMachine();
        if (nearestMachine == null) {
            Gdx.app.error("GameServer", "No se encontró una máquina de reciclaje para el robot.");
            return;
        }

        Robot robot = new Robot(x, y, facingRight, speed, texture, this, null, nearestMachine);
        activeRobots.add(robot);
        Gdx.app.log("GameServer", "Robot spawneado en (" + x + ", " + y + ")");
    }

    public Objetos getNearestTrash(Vector2 position) {
        Objetos nearestTrash = null;
        float minDist = Float.MAX_VALUE;
        for (Objetos obj : gameObjects.values()) {
            if (obj.estaActivo() && obj.getTexture().getTexture() == game.getAssets().trashTexture) {
                float dist = Vector2.dst(position.x, position.y, obj.x, obj.y);
                if (dist < minDist) {
                    minDist = dist;
                    nearestTrash = obj;
                }
            }
        }
        return nearestTrash;
    }

    public MaquinaReciclaje getRecyclingMachine() {
        for (Objetos obj : gameObjects.values()) {
            if (obj instanceof MaquinaReciclaje) {
                return (MaquinaReciclaje) obj;
            }
        }
        return null;
    }

    public void removeGameObject(int id) {
        Objetos removedObject = gameObjects.remove(id);
        if (removedObject instanceof Arbol) {
            // Si el objeto removido es un árbol, liberar su punto de spawn
            // Necesitamos encontrar el punto de spawn asociado al árbol
            for (Map.Entry<Vector2, Boolean> entry : treeSpawnPointsOccupancy.entrySet()) {
                if (entry.getKey().epsilonEquals(removedObject.x, removedObject.y, 1.0f)) { // Usar epsilonEquals para comparar floats
                    freeTreeSpawnPoint(entry.getKey());
                    break;
                }
            }
        } else if (removedObject != null && removedObject.getTexture().getTexture() == game.getAssets().trashTexture) {
            // Si el objeto removido es basura, liberar su punto de spawn
            for (Map.Entry<Vector2, Boolean> entry : trashSpawnPointsOccupancy.entrySet()) {
                if (entry.getKey().epsilonEquals(removedObject.x, removedObject.y, 1.0f)) {
                    freeTrashSpawnPoint(entry.getKey());
                    break;
                }
            }
        }
        // Eliminar también de cargarObjetos.objetos
        for (int i = cargarObjetos.objetos.size - 1; i >= 0; i--) {
            if (cargarObjetos.objetos.get(i).getId() == id) {
                cargarObjetos.objetos.removeIndex(i);
                break;
            }
        }
    }

    private void spawnTrash(Vector2 position) {
        float objectWidth = game.getAssets().trashTexture.getWidth();
        float objectHeight = game.getAssets().trashTexture.getHeight();

        MapLayer layer = map.getLayers().get("SpawnObjetos");
        float tileWidth = 32;
        float tileHeight = 32;

        if (layer instanceof TiledMapTileLayer) {
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
            tileWidth = tileLayer.getTileWidth();
            tileHeight = tileLayer.getTileHeight();
        }

        float centeredX = position.x + (tileWidth - objectWidth) / 2;
        float finalY;

        // Basura en la base del tile
        finalY = position.y;

        // Calcular la posición Y final para que el objeto esté sobre el suelo
        Rectangle tempRect = new Rectangle(centeredX, finalY, objectWidth, objectHeight);
        float groundY = collisionManager.getGroundY(tempRect);
        finalY = (groundY >= 0) ? groundY : finalY;

        Objetos basura = new Objetos(centeredX, finalY, new TextureRegion(game.getAssets().trashTexture)) {
            @Override
            public void actualizar(float delta) {
                // La basura no tiene animacion ni logica de actualizacion compleja
            }
        };
        basura.setId(nextObjectId++);
        gameObjects.put(basura.getId(), basura);
        cargarObjetos.agregarObjeto(basura);
        Gdx.app.log("GameServer", "Basura spawneada en " + position);
        occupiedTrashSpawnPoints++;
    }

    private void freeTrashSpawnPoint(Vector2 position) {
        if (trashSpawnPointsOccupancy.containsKey(position)) {
            trashSpawnPointsOccupancy.put(position, false);
            Gdx.app.log("GameServer", "Punto de spawn de basura liberado: " + position);
            occupiedTrashSpawnPoints--;
        }
    }

    public void addTrashToMachine(MaquinaReciclaje machine, int amount) {
        machine.addTrash(amount);
        Gdx.app.log("GameServer", "Robot entregó " + amount + " de basura a la máquina. Total: " + machine.getTotalCollectedTrash());
    }

    private void destroyTreeAtSpawnPoint(Vector2 spawnPoint) {
        Objetos treeToDestroy = null;
        for (Objetos obj : gameObjects.values()) {
            if (obj instanceof Arbol && obj.x == spawnPoint.x && obj.y == spawnPoint.y) {
                treeToDestroy = obj;
                break;
            }
        }

        if (treeToDestroy != null) {
            removeGameObject(treeToDestroy.getId());
            freeTreeSpawnPoint(spawnPoint);
            treesOnMap--;
            Gdx.app.log("GameServer", "Árbol destruido en " + spawnPoint + ". Árboles restantes: " + treesOnMap);
        }
    }

    private void spawnTrashAtRandomAvailablePoints(int count) {
        List<Vector2> availableSpawns = new ArrayList<>();
        for (Map.Entry<Vector2, Boolean> entry : trashSpawnPointsOccupancy.entrySet()) {
            if (!entry.getValue()) {
                availableSpawns.add(entry.getKey());
            }
        }
        Collections.shuffle(availableSpawns);

        int spawnedCount = 0;
        for (Vector2 spawnPoint : availableSpawns) {
            if (spawnedCount >= count) break;
            spawnTrash(spawnPoint);
            trashSpawnPointsOccupancy.put(spawnPoint, true);
            spawnedCount++;
        }
        Gdx.app.log("GameServer", "Spawneada " + spawnedCount + " basura adicional.");
    }

    public Array<Robot> getActiveRobots() {
        return activeRobots;
    }
}
