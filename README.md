# Sonic Game

| **Nombre** | Cedula |
|------------|--------|
|**Luis Rojas**| 30931891 |
|**Abdel Licones**| 31445619 |
|**Rayc Yanez** | 28215618 |
|**Felix Figuera** | 28500894 |
|**Mchalaxk Franco** | 30365867 |

# Capito II UML

## Clases

``` mermaid
  classDiagram
    direction LR

    %% -- Actores --
    class Personajes {
        <<abstract>>
        #int playerId
        #float x
        #float y
        #float velocityY
        #boolean isGrounded
        #boolean isFlying
        #Animation currentAnimation
        #int lives
        #Map collectibles
        +void update(float delta, CollisionManager collisionManager)
        +void handleInput(InputState input, CollisionManager collisionManager, float delta)
        +abstract void useAbility()
        +abstract void dispose()
        +void setAnimation(AnimationType animationType)
        +float getX()
        +float getY()
        +boolean isFacingRight()
        +int getLives()
        +void addCollectible(CollectibleType type)
    }

    class Sonic {
        -boolean isSpinning
        -float spinPower
        +void useAbility()
        +void update(float delta, CollisionManager collisionManager)
    }

    class Tails {
        -boolean isFlying
        -float flyTime
        -Array~Robot~ activeRobots
        +void useAbility()
        +void update(float delta, CollisionManager collisionManager)
        +void startFlying()
        +void stopFlying()
    }

    class Knuckles {
        -boolean isPunching
        -float punchPower
        +void useAbility()
        +void update(float delta, CollisionManager collisionManager)
    }

    class Robot {
        -float x
        -float y
        -boolean facingRight
        -float speed
        -TextureRegion texture
        +void update(float delta, CollisionManager collisionManager)
        +boolean isActive()
    }

    %% -- Objetos --
    class Objetos {
        <<abstract>>
        #int id
        #float x
        #float y
        #TextureRegion textura
        #boolean activo
        +void actualizar(float delta)
        +void renderizar(SpriteBatch batch)
        +Rectangle getHitbox()
        +boolean estaActivo()
        +void setActivo(boolean activo)
    }

    class Anillo {
        +void actualizar(float delta)
    }

    class MaquinaReciclaje {
        -int totalCollectedTrash
        +void actualizar(float delta)
        +void addTrash(int amount)
    }

    class Arbol {
        +void actualizar(float delta)
    }

    class CargarObjetos {
        -Array~Objetos~ objetos
        +void agregarObjeto(Objetos obj)
        +void actualizar(float delta)
        +void renderizar(SpriteBatch batch)
        +Array~Objetos~ getObjetos()
    }

    %% -- Pantallas --
    class GameScreen {
        -JuegoSonic game
        -int localPlayerId
        -OrthographicCamera camera
        -ConcurrentHashMap~int, Personajes~ characters
        -CollisionManager collisionManager
        -GameHub gameHub
        +void render(float delta)
        +void updateFromGameState()
        +void processInput(float delta)
    }

    class MainScreen {
        +void setupUI()
    }

    class CharacterSelectionScreen {
        +void updateButtonState(TextButton button, String characterName)
    }

    class LobbyScreen {
    }

    class HelpScreen {
    }

    %% -- Server --
    class NetworkManager {
        -JuegoSonic game
        -boolean isHost
        -int localPlayerId
        -GameServer gameServer
        +void startHost()
        +void connectAsClient(String ip, int port)
        +void sendCharacterSelection(String characterType)
        +void broadcastUdpGameState()
        +GameState getCurrentGameState()
    }

    class GameServer {
        -JuegoSonic game
        -ConcurrentHashMap~int, Personajes~ characters
        -CargarObjetos cargarObjetos
        -CollisionManager collisionManager
        +void start()
        +void stop()
        +void updateGameState(float delta)
        +void spawnTree(Vector2 position)
        +void spawnTrash(Vector2 position)
    }

    class NetworkHelper {
        +String getIpLocal()
    }

    %% -- Server.domain --
    class InputState {
        -boolean up
        -boolean down
        -boolean left
        -boolean right
        -boolean ability
        -int playerId
        +boolean isUp()
        +void setUp(boolean up)
        +boolean isDown()
        +void setDown(boolean down)
        +boolean isLeft()
        +void setLeft(boolean left)
        +boolean isRight()
        +void setRight(boolean right)
        +boolean isAbility()
        +void setAbility(boolean ability)
    }

    class PlayerState {
        -int playerId
        -float x
        -float y
        -boolean facingRight
        -String currentAnimationName
        -float animationStateTime
        -String characterType
        -Map~CollectibleType, Integer~ collectibles
        -int lives
        +int getPlayerId()
        +float getX()
        +float getY()
        +boolean isFacingRight()
        +String getCurrentAnimationName()
    }

    class GameState {
        -long sequenceNumber
        -ArrayList~PlayerState~ players
        -List~ObjectState~ objects
        -float gameTimeRemaining
        -GameStatus gameStatus
        +long getSequenceNumber()
        +ArrayList~PlayerState~ getPlayers()
        +List~ObjectState~ getObjects()
    }

    class ObjectState {
        -int id
        -float x
        -float y
        -boolean active
        -String type
        -int totalCollectedTrash
        +int getId()
        +float getX()
        +float getY()
        +boolean isActive()
        +String getType()
    }

    %% -- Util --
    class CollisionManager {
        -Array~CollisionShape~ collisionShapes
        -float mapWidth
        -float mapHeight
        +boolean collides(Rectangle characterRect)
        +boolean isOnGround(Rectangle characterRect)
        +float getGroundY(Rectangle characterRect)
    }

    %% -- Relaciones --
    Personajes <|-- Sonic
    Personajes <|-- Tails
    Personajes <|-- Knuckles

    Objetos <|-- Anillo
    Objetos <|-- MaquinaReciclaje
    Objetos <|-- Arbol

    CargarObjetos "1" *-- "*" Objetos : contiene
    
    Tails "1" *-- "*" Robot : activeRobots
    
    GameServer "1" *-- "1" CollisionManager
    GameServer "1" *-- "1" CargarObjetos
    GameServer "1" *-- "*" Personajes : characters
    
    NetworkManager "1" *-- "0..1" GameServer : gameServer
    
    GameScreen "1" *-- "1" JuegoSonic : game
    GameScreen "1" *-- "1" CollisionManager : collisionManager
    GameScreen "1" *-- "*" Personajes : characters
    GameScreen "1" *-- "1" GameHub : gameHub
    
    GameState "1" *-- "*" PlayerState : players
    GameState "1" *-- "*" ObjectState : objects
    
    GameServer ..> GameState : crea
    NetworkManager ..> GameState : transmite
    GameScreen ..> GameState : recibe
    
    CollisionManager "1" *-- "*" CollisionShape : collisionShapes
    
    JuegoSonic "1" *-- "1" NetworkManager : networkManager
```

### 1. Tails
| **Clase** | Tails |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Actores` |
| **Propósito** | Implementa el personaje Tails con habilidades de vuelo y creación de robots. |
| **Atributos** | <ul><li>`TextureAtlas tailsAtlas`</li><li>`Animation<TextureRegion> flyAnimation, fallAnimation, robotAnimation`</li><li>`boolean isFlying`</li><li>`float flySpeed, flyTime, MAX_FLY_TIME`</li><li>`Array<Robot> activeRobots`</li><li>`float robotCooldown, ROBOT_COOLDOWN_TIME`</li><li>`float abilityActiveTimer, ABILITY_DURATION`</li><li>`GameServer gameServer`</li></ul> |
| **Métodos** | <ul><li>`Tails(int, TextureAtlas, GameServer)`</li><li>`void cargarAnimaciones()`</li><li>`void update(float, CollisionManager)`</li><li>`void handleInput(InputState, CollisionManager, float)`</li><li>`void useAbility()`</li><li>`void startFlying()`</li><li>`void stopFlying()`</li><li>`void resetFlyTime()`</li><li>`boolean isFlying()`</li><li>`float getRobotCooldown()`</li><li>`Array<Robot> getActiveRobots()`</li><li>`String getCurrentAnimationName()`</li><li>`void setAnimation(AnimationType)`</li><li>`void dispose()`</li></ul> |
### 2. Objetos
| **Clase** | Objetos |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Objetos` |
| **Propósito** | Clase base abstracta para todos los objetos del juego (anillos, basura, máquinas, etc.). |
| **Atributos** | <ul><li>`int id`</li><li>`float x, y`</li><li>`TextureRegion textura`</li><li>`Rectangle hitbox`</li><li>`boolean activo`</li></ul> |
| **Métodos** | <ul><li>`Objetos(float, float, TextureRegion)`</li><li>`abstract void actualizar(float)`</li><li>`void renderizar(SpriteBatch)`</li><li>`Rectangle getHitbox()`</li><li>`TextureRegion getTexture()`</li><li>`boolean estaActivo()`</li><li>`void setActivo(boolean)`</li><li>`void setId(int)`</li><li>`int getId()`</li><li>`float getWidth()`</li><li>`float getHeight()`</li></ul> |
### 3. CargarObjetos
| **Clase** | CargarObjetos |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Objetos` |
| **Propósito** | Gestiona la carga, actualización y renderización de los objetos del juego. |
| **Atributos** | <ul><li>`Array<Objetos> objetos`</li><li>`TextureAtlas atlasObjetos`</li><li>`Animation<TextureRegion> animacionAnillo`</li><li>`Texture basuraTexture`</li></ul> |
| **Métodos** | <ul><li>`CargarObjetos(TextureAtlas, Assets)`</li><li>`void cargarRecursos()`</li><li>`void agregarAnillo(float, float)`</li><li>`void agregarAnillo(Anillo)`</li><li>`void agregarBasura(float, float)`</li><li>`void agregarObjeto(Objetos)`</li><li>`void actualizar(float)`</li><li>`void renderizar(SpriteBatch)`</li><li>`Array<Objetos> getObjetos()`</li></ul> |
### 4. Personajes
| **Clase** | Personajes |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Actores` |
| **Propósito** | Clase base abstracta para todos los personajes jugables. |
| **Atributos** | <ul><li>`int playerId`</li><li>`float x, y, prevX, prevY, predictedX, predictedY, velocityY`</li><li>`boolean facingRight, isGrounded, isRolling, isAbilityActive, isFlying, activo`</li><li>`float stateTime, moveSpeed, gravity, jumpForce`</li><li>`int lives`</li><li>`Map<CollectibleType, Integer> collectibles`</li><li>`Animation<TextureRegion> currentAnimation, idleAnimation, runAnimation, jumpAnimation, rollAnimation, abilityAnimation, flyAnimation`</li></ul> |
| **Métodos** | <ul><li>`abstract void useAbility()`</li><li>`abstract void dispose()`</li><li>`void update(float, CollisionManager)`</li><li>`void updatePhysics(float, CollisionManager)`</li><li>`void handleInput(InputState, CollisionManager, float)`</li><li>`boolean handleHorizontalMovement(InputState, CollisionManager, float)`</li><li>`void handleAbilityInput(InputState)`</li><li>`void updateAnimationState(boolean)`</li><li>`void setCurrentAnimation(Animation<TextureRegion>)`</li><li>`void setAnimation(AnimationType)`</li><li>`TextureRegion getCurrentFrame()`</li><li>`String getCurrentAnimationName()`</li><li>`Rectangle getBounds()`</li><li>`void addCollectible(CollectibleType)`</li><li>`int getCollectibleCount(CollectibleType)`</li><li>`void setCollectibleCount(CollectibleType, int)`</li><li>`Vector2 getPosition()`</li><li>Getters y setters para múltiples atributos</li></ul> |
### 5. Sonic
| **Clase** | Sonic |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Actores` |
| **Propósito** | Implementa el personaje Sonic con su habilidad Spin Dash. |
| **Atributos** | <ul><li>`TextureAtlas atlasSonic`</li><li>`Animation<TextureRegion> spinKickAnimation`</li><li>`boolean isSpinning`</li><li>`float spinPower, MAX_SPIN_POWER`</li></ul> |
| **Métodos** | <ul><li>`Sonic(int, TextureAtlas)`</li><li>`void useAbility()`</li><li>`void update(float, CollisionManager)`</li><li>`void cargarAnimaciones()`</li><li>`TextureRegion getCurrentFrame()`</li><li>`void dispose()`</li></ul> |
### 6. NetworkManager
| **Clase** | NetworkManager |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Server` |
| **Propósito** | Gestiona la comunicación de red (tanto para host como para cliente). |
| **Atributos** | <ul><li>`JuegoSonic game`</li><li>`ServerSocket serverTcpSocket`</li><li>`DatagramSocket udpSocket`</li><li>`List<ClientConnection> clientConnections`</li><li>`AtomicInteger nextPlayerId`</li><li>`volatile GameState currentGameState`</li><li>`Socket clientTcpSocket`</li><li>`ObjectOutputStream clientTcpOut`</li><li>`ObjectInputStream clientTcpIn`</li><li>`InetAddress serverAddress`</li><li>`int serverUdpPort`</li><li>`ConcurrentHashMap<Integer, InputState> playerInputs`</li><li>`ConcurrentHashMap<Integer, String> selectedCharacters`</li><li>`volatile boolean isHost`</li><li>`int localPlayerId`</li><li>`String selectedCharacterType`</li><li>`Thread hostDiscoveryThread, clientTcpReceiveThread, udpReceiveThread`</li><li>`GameServer gameServer`</li></ul> |
| **Métodos** | <ul><li>`NetworkManager(JuegoSonic)`</li><li>`void checkNetworkStatus()`</li><li>`void sendCharacterSelection(String)`</li><li>`void sendTcpMessageToServer(Object)`</li><li>`void processCharacterSelection(int, String)`</li><li>`Color getColorForCharacter(String)`</li><li>`String discoverServer()`</li><li>`void startHost()`</li><li>`void initializeGameServer(TiledMap, CollisionManager, float, float)`</li><li>`void announceServer()`</li><li>`void acceptClients()`</li><li>`void connectAsClient(String, int)`</li><li>`void startClientTcpListener(ObjectInputStream, int)`</li><li>`void startUdpListener()`</li><li>`void startGame()`</li><li>`void broadcastTcpMessage(Object)`</li><li>`void broadcastUdpGameState()`</li><li>`void sendInputState(InputState)`</li><li>`GameState getCurrentGameState()`</li><li>`void setCurrentGameState(GameState)`</li><li>`ConcurrentHashMap<Integer, InputState> getPlayerInputs()`</li><li>`boolean isHost()`</li><li>`ConcurrentHashMap<Integer, String> getSelectedCharacters()`</li><li>`void dispose()`</li><li>`String getSelectedCharacterType()`</li><li>`GameServer getGameServer()`</li></ul> |
### 7. NetworkHelper
| **Clase** | NetworkHelper |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Server` |
| **Propósito** | Proporciona utilidades de red, como obtener la dirección IP local. |
| **Atributos** | Ninguno |
| **Métodos** | <ul><li>`static String getIpLocal()`</li></ul> |
### 8. JuegoSonic
| **Clase** | JuegoSonic |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic` |
| **Propósito** | Clase principal del juego que inicia la aplicación y gestiona las pantallas. |
| **Atributos** | <ul><li>`NetworkManager networkManager`</li><li>`Assets assets`</li><li>`ConcurrentHashMap<String, Boolean> selectedCharacters`</li></ul> |
| **Métodos** | <ul><li>`void create()`</li><li>`void dispose()`</li><li>`Assets getAssets()`</li><li>`boolean isCharacterTaken(String)`</li><li>`void setCharacterTaken(String, boolean)`</li></ul> |
### 9. Knuckles
| **Clase** | Knuckles |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Actores` |
| **Propósito** | Implementa el personaje Knuckles con su habilidad de puñetazo. |
| **Atributos** | <ul><li>`TextureAtlas atlasKnuckles`</li><li>`Animation<TextureRegion> PunchAnimation`</li><li>`boolean isPunching`</li><li>`float PunchPower, MAX_PUNCH_POWER`</li></ul> |
| **Métodos** | <ul><li>`Knuckles(int, TextureAtlas)`</li><li>`void useAbility()`</li><li>`void update(float, CollisionManager)`</li><li>`void cargarAnimaciones()`</li><li>`TextureRegion getCurrentFrame()`</li><li>`void dispose()`</li></ul> |
### 10. LobbyScreen
| **Clase** | LobbyScreen |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Pantallas` |
| **Propósito** | Pantalla que muestra el lobby mientras los jugadores esperan a que el host inicie el juego. |
| **Atributos** | <ul><li>`JuegoSonic game`</li><li>`Stage stage`</li><li>`Color playerColor`</li><li>`boolean isHost`</li></ul> |
| **Métodos** | <ul><li>`LobbyScreen(JuegoSonic, Color, boolean)`</li><li>`void setupHostUI()`</li><li>`void render(float)`</li><li>`void resize(int, int)`</li><li>`void dispose()`</li></ul> |
### 11. GameScreen
| **Clase** | GameScreen |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Pantallas` |
| **Propósito** | Pantalla principal del juego donde se desarrolla la acción. |
| **Atributos** | <ul><li>`JuegoSonic game`</li><li>`int localPlayerId`</li><li>`OrthographicCamera camera`</li><li>`SpriteBatch batch`</li><li>`ShapeRenderer shapeRenderer`</li><li>`ConcurrentHashMap<Integer, Personajes> characters`</li><li>`TiledMap map`</li><li>`OrthogonalTiledMapRenderer mapRenderer`</li><li>`CollisionManager collisionManager`</li><li>`float mapWidth, mapHeight`</li><li>`Texture[] parallaxLayers`</li><li>`float[] parallaxSpeeds`</li><li>`float parallaxWidth, parallaxHeight`</li><li>`ConcurrentHashMap<Integer, Objetos> clientObjects`</li><li>`float objectStateTime`</li><li>`GameHub gameHub`</li><li>`BitmapFont messageFont`</li></ul> |
| **Métodos** | <ul><li>`GameScreen(JuegoSonic, int)`</li><li>`void initParallaxBackground()`</li><li>`void renderParallaxBackground()`</li><li>`void initGameHub()`</li><li>`void createCharacter(int, String)`</li><li>`void updateFromGameState()`</li><li>`void updateObjectsFromGameState()`</li><li>`void updateObjects(float)`</li><li>`void renderObjects()`</li><li>`void renderRobots()`</li><li>`void render(float)`</li><li>`void processInput(float)`</li><li>`void debugRenderCollisions()`</li><li>`void resize(int, int)`</li><li>`void dispose()`</li></ul> |
### 12. HelpScreen
| **Clase** | HelpScreen |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Pantallas` |
| **Propósito** | Pantalla que muestra información de ayuda (controles, personajes, objetivos). |
| **Atributos** | <ul><li>`JuegoSonic game`</li><li>`Stage stage`</li><li>`Texture backgroundTexture, titleBackgroundTexture, controlsImage, objectivesImage, charactersImage`</li><li>`SpriteBatch batch`</li></ul> |
| **Métodos** | <ul><li>`HelpScreen(JuegoSonic)`</li><li>`void setupUI()`</li><li>`void render(float)`</li><li>`void resize(int, int)`</li><li>`void dispose()`</li><li>`void show()`</li></ul> |
### 13. CharacterSelectionScreen
| **Clase** | CharacterSelectionScreen |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Pantallas` |
| **Propósito** | Pantalla donde los jugadores seleccionan su personaje. |
| **Atributos** | <ul><li>`JuegoSonic game`</li><li>`Stage stage`</li><li>`ConcurrentHashMap<String, Boolean> selectedCharacters`</li><li>`TextButton sonicButton, tailsButton, knucklesButton`</li></ul> |
| **Métodos** | <ul><li>`CharacterSelectionScreen(JuegoSonic, ConcurrentHashMap)`</li><li>`void createUI()`</li><li>`void updateButtonState(TextButton, String)`</li><li>`void render(float)`</li><li>`void resize(int, int)`</li><li>`void dispose()`</li></ul> |
### 14. MainScreen
| **Clase** | MainScreen |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Pantallas` |
| **Propósito** | Pantalla principal del menú con opciones para jugar, ayuda y estadísticas. |
| **Atributos** | <ul><li>`JuegoSonic game`</li><li>`Stage stage`</li><li>`SpriteBatch batch`</li><li>`ScrollingBackground background`</li></ul> |
| **Métodos** | <ul><li>`MainScreen(JuegoSonic)`</li><li>`void setupUI()`</li><li>`void render(float)`</li><li>`void resize(int, int)`</li><li>`void dispose()`</li></ul> |
### 15. CollisionManager
| **Clase** | CollisionManager |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Util` |
| **Propósito** | Gestiona las colisiones entre objetos y el mapa. |
| **Atributos** | <ul><li>`Array<CollisionShape> collisionShapes`</li><li>`float mapWidth, mapHeight`</li></ul> |
| **Métodos** | <ul><li>`CollisionManager(TiledMap, String, float, float)`</li><li>`void addTileCollisions(TiledMap, String)`</li><li>`boolean collides(Rectangle)`</li><li>`boolean checkPlatformCollision(Rectangle, float)`</li><li>`boolean isOnGround(Rectangle)`</li><li>`float getGroundY(Rectangle)`</li><li>`float getMapWidth()`</li><li>`float getMapHeight()`</li><li>`Array<Shape2D> getCollisionShapes()`</li></ul> |
### 16. InputState
| **Clase** | InputState |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Server.domain` |
| **Propósito** | Representa el estado de entrada de un jugador (teclas presionadas). |
| **Atributos** | <ul><li>`boolean up, down, left, right, ability`</li><li>`int playerId`</li></ul> |
| **Métodos** | <ul><li>Getters y setters para cada atributo booleano</li><li>`int getPlayerId()`</li><li>`void setPlayerId(int)`</li></ul> |
### 17. PlayerState
| **Clase** | PlayerState |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Server.domain` |
| **Propósito** | Representa el estado de un jugador para la sincronización en red. |
| **Atributos** | <ul><li>`int playerId`</li><li>`float x, y`</li><li>`boolean facingRight`</li><li>`String currentAnimationName`</li><li>`float animationStateTime`</li><li>`String characterType`</li><li>`Map<CollectibleType, Integer> collectibles`</li><li>`boolean flying`</li><li>`boolean isAvispa`</li><li>`float targetX, targetY`</li><li>`boolean active`</li><li>`int lives`</li></ul> |
| **Métodos** | <ul><li>`PlayerState(int, float, float, boolean, String, float, String, boolean, Map, boolean, float, float, boolean, int)`</li><li>`int getPlayerId()`</li><li>`float getX()`</li><li>`float getY()`</li><li>`boolean isFacingRight()`</li><li>`String getCurrentAnimationName()`</li><li>`float getAnimationStateTime()`</li><li>`String getCharacterType()`</li><li>`Map<CollectibleType, Integer> getCollectibles()`</li><li>`boolean isFlying()`</li><li>`boolean isAvispa()`</li><li>`float getTargetX()`</li><li>`float getTargetY()`</li><li>`boolean isActive()`</li><li>`int getLives()`</li></ul> |
### 18. GameState
| **Clase** | GameState |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Server.domain` |
| **Propósito** | Representa el estado completo del juego para sincronización en red. |
| **Atributos** | <ul><li>`long sequenceNumber`</li><li>`ArrayList<PlayerState> players`</li><li>`List<ObjectState> objects`</li><li>`float gameTimeRemaining`</li><li>`GameStatus gameStatus`</li></ul> |
| **Métodos** | <ul><li>`GameState(ArrayList, List, long, float, GameStatus)`</li><li>`long getSequenceNumber()`</li><li>`ArrayList<PlayerState> getPlayers()`</li><li>`List<ObjectState> getObjects()`</li><li>`float getGameTimeRemaining()`</li><li>`GameStatus getGameStatus()`</li></ul> |
### 19. ObjectState
| **Clase** | ObjectState |
|-----------|-------|
| **Paquete** | `com.miestudio.jsonic.Server.domain` |
| **Propósito** | Representa el estado de un objeto del juego para sincronización en red. |
| **Atributos** | <ul><li>`int id`</li><li>`float x, y`</li><li>`boolean active`</li><li>`String type`</li><li>`int totalCollectedTrash`</li></ul> |
| **Métodos** | <ul><li>`ObjectState(int, float, float, boolean, String, int)`</li><li>`int getId()`</li><li>`float getX()`</li><li>`float getY()`</li><li>`boolean isActive()`</li><li>`String getType()`</li><li>`int getTotalCollectedTrash()`</li></ul> |
