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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private static final int TRASH_EVENT_THRESHOLD = 5; // Cantidad de basura para activar el evento
    private final List<Vector2> treeSpawnPoints = new ArrayList<>();
    private final Map<Vector2, Boolean> treeSpawnPointsOccupancy = new HashMap<>(); // true si está ocupado
    private int treesOnMap = 0;

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
        spawnGameObjects("Basura", "SpawnObjetos");
        spawnGameObjects("Maquina", "SpawnObjetos");
        initializeTreeSpawnPoints();
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
                Objetos basura = new Objetos(centeredX, finalY, new TextureRegion(game.getAssets().trashTexture)) {
                    @Override
                    public void actualizar(float delta) {
                        // La basura no tiene animacion ni logica de actualizacion compleja
                    }
                };
                basura.setId(nextObjectId++);
                gameObjects.put(basura.getId(), basura);
                cargarObjetos.agregarObjeto(basura);
            } else if ("Maquina".equals(objectType)) {
                MaquinaReciclaje maquina = new MaquinaReciclaje(centeredX, finalY, game.getAssets().maquinaAtlas);
                maquina.setId(nextObjectId++);
                gameObjects.put(maquina.getId(), maquina);
                cargarObjetos.agregarObjeto(maquina);
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
                (character instanceof Tails) ? ((Tails) character).isFlying() : false,
                character.getCollectibles(),
                (character instanceof EAvispa), // isAvispa
                (character instanceof EAvispa) ? ((EAvispa) character).getTargetPosition().x : 0f, // targetX
                (character instanceof EAvispa) ? ((EAvispa) character).getTargetPosition().y : 0f, // targetY
                character.estaActivo() // active
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
            } else if (character instanceof EAvispa) {
                EAvispa avispa = (EAvispa) character;
                avispa.update(delta, collisionManager);
                if (!avispa.estaActivo()) {
                    // Avispa se autodestruyó, spawnear árbol
                    spawnTree(avispa.getTargetPosition());
                    characters.remove(avispa.getPlayerId()); // Eliminar avispa del mapa de personajes
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
                    if (obj instanceof Anillo) {
                        character.addCollectible(CollectibleType.RINGS);
                        obj.setActivo(false);
                        Gdx.app.log("GameServer", "Anillo con ID: " + obj.getId() + " desactivado por el jugador " + character.getPlayerId());
                    } else if (obj.getTexture().getTexture() == game.getAssets().trashTexture) {
                        character.addCollectible(CollectibleType.TRASH);
                        obj.setActivo(false);
                        Gdx.app.log("GameServer", "Basura con ID: " + obj.getId() + " desactivado por el jugador " + character.getPlayerId());
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
                                spawnAvispa();
                            }
                            // Reiniciar el contador de la máquina para el próximo ciclo del evento
                            maquina.addTrash(- (eventsTriggered * TRASH_EVENT_THRESHOLD)); // Restar la basura que activó el evento
                            maquina.addTrash(remainingTrash); // Añadir el remanente
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

    private void spawnAvispa() {
        // Encontrar un punto de spawn de avispa
        List<Vector2> avispaSpawns = MapUtil.findAllSpawnPoints(map, "SpawnEntidades", "Avispa");
        if (avispaSpawns.isEmpty()) {
            Gdx.app.error("GameServer", "No hay puntos de spawn para Avispa.");
            return;
        }
        Vector2 avispaSpawn = avispaSpawns.get(new Random().nextInt(avispaSpawns.size()));

        // Encontrar un punto de spawn de árbol desocupado
        Vector2 treeTargetSpawn = null;
        for (Map.Entry<Vector2, Boolean> entry : treeSpawnPointsOccupancy.entrySet()) {
            if (!entry.getValue()) { // Si no está ocupado
                treeTargetSpawn = entry.getKey();
                break;
            }
        }

        if (treeTargetSpawn == null) {
            Gdx.app.log("GameServer", "Todos los puntos de spawn de árboles están ocupados.");
            return;
        }

        // Marcar el punto de spawn del árbol como ocupado
        treeSpawnPointsOccupancy.put(treeTargetSpawn, true);

        // Crear la avispa
        EAvispa avispa = new EAvispa(
            avispaSpawn.x,
            avispaSpawn.y,
            game.getAssets().enemyAtlas,
            treeTargetSpawn
        );
        avispa.setPlayerId(nextObjectId++); // Usar el mismo contador para IDs de objetos y actores temporales
        characters.put(avispa.getPlayerId(), avispa);
        Gdx.app.log("GameServer", "Avispa spawneada en " + avispaSpawn + " con objetivo " + treeTargetSpawn);
    }

    private void spawnTree(Vector2 position) {
        Arbol arbol = new Arbol(position.x, position.y, new TextureRegion(game.getAssets().treeTexture));
        arbol.setId(nextObjectId++);
        gameObjects.put(arbol.getId(), arbol);
        cargarObjetos.agregarObjeto(arbol);
        treesOnMap++;
        Gdx.app.log("GameServer", "Árbol spawneado en " + position + ". Total de árboles: " + treesOnMap);

        // Marcar el punto de spawn como desocupado después de que el árbol aparece
        treeSpawnPointsOccupancy.put(position, false);
    }
}
