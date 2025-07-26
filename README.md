# Sonic Game

| **Nombre** | Cedula |
|------------|--------|
|**Luis Rojas**| 30931891 |
|**Abdel Licones**| 31445619 |
|**Rayc Yanez** | 28215618 |
|**Felix Figuera** | 28500894 |
|**Mchalaxk Franco** | 30365867 |

# Capitolo I - Introduccion y analisis

## ANALISIS

### Planteamiento del Sistema:
Sonic Project es un entusiasmado juego multijugador que se sumerge en el animado universo de Sonic the Hedgehog, con foco en programación a objetos y modelado UML. La narrativa se basa en la feroz batalla de Sonic y sus amigos, Tails y Knuckles, contra el malvado Dr. Robotnik, quien ha dejado su marca tóxica en la Green Hill Zone. Los jugadores se deben unir para limpiar la zona, reciclar residuos y recuperar el medio ambiente, todo mientras superan toda clase de obstáculos y entes hostiles.
Funcionalidades Principales:

1- Módulo Jugar
- Multijugador: 3 jugadores pueden jugar a Sonic, Tails y Knuckles, cada uno con sus propias habilidades:
- Sonic: Corre rápidamente para recoger trastos y puede hacer un "Tornado de Limpieza"
- Tails: Aprovecha su habilidad para volar para transportar materiales reciclados y puede hacer un "Dron Reciclador".
- Knuckles: Aprovecha su musculación para destruir desechos nocivos con sus horribles puños.
- Contaminación progresiva: Si no se actúa a tiempo, la zona se degrada, afectando a la fauna y flora.
- Combate contra Robotnik: Al final de cada nivel, Robotnik salta en su Eggmobile con el fin de sabotear el progreso de los jugadores.

2- Módulo Estadísticas:

- Ranking individual: Tiene una tabla de clasificación que refleja las contribuciones de cada uno de los jugadores:
- Puntos por limpiar áreas, reciclar materiales y derrotar enemigos.

Competencia sana: Fomenta la colaboración y el rendimiento individual.

3-Módulo Ayuda y Acerca De:
- Ayuda: Explica las reglas del juego, controles y objetivos de una manera clara.
- Acerca De: Ofrece detalles sobre el lenguaje de programación, las librerías empleadas, los desarrolladores y la versión del juego.
Requisitos Adicionales:
- Desarrollo en NetBeans con documentación Javadoc.
- Entrega ordenada que contenga el código fuente y un informe.
- Trabajo en equipo con penalización por faltas durante la corrección.

# Capito II UML

## Casos de uso

## 1.-Casos de uso externo

### Caso de Uso 1: Iniciar Partida

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Iniciar Partida |
| **Actores**        | Jugador (Principal), Sistema (Secundario, al usar gameManager) |
| **Propósito**      | Permite a los jugadores iniciar una nueva partida. |
| **Precondiciones** | 1. El jugador se encuentra en el menú del juego. |
| **Flujo Principal** | 1. Un jugador selecciona la opción de Iniciar Partida, esta elección es enviada a gameManager para que llame a iniciarPartida().<br>2. gameManager hace un cambio en pantallaActual para que muestre la pantalla del juego.<br>3. gameManager inicializa el EstadisticasManager y que este anote las estadísticas obtenidas durante la partida.<br>4. En el caso de que la partida sea Multijugador, gameManager llama a multijugadorManager para preparar la sala.<br>5. El juego muestra en pantalla el nivel. |
| **Flujo Alternativo** | **Falla al empezar partida:**<br>1. Ocurre un error durante el inicio de la partida.<br>2. El sistema notificará al jugador de la ocurrencia del error y lo regresará al menú principal. |

---

### Caso de Uso 2: Cambiar Nivel

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Cambiar Nivel |
| **Actores**        | Sistema (Principal), Jugador (Secundario) |
| **Propósito**      | El sistema realizará las gestiones correspondientes para hacer el cambio de nivel al siguiente, haciendo las cargas de texturas y recursos. |
| **Precondiciones** | 1. Ya hay un juego iniciado.<br>2. Hay más niveles disponibles para jugar. |
| **Flujo Principal** | 1. El sistema va a detectar si el jugador ha logrado cumplir las condiciones de victoria para pasar de nivel.<br>2. En caso de que sea verificado como cierto, gameManager llama a cambiarNivel(), del cual lograría realizar la carga del siguiente nivel.<br>3. gameManager haría actualización de la pantallaActual y que muestre el nuevo nivel.<br>4. El jugador podrá continuar su sesión de juego en el nuevo nivel. |
| **Flujo Alternativo** | **Error de carga o no existen más niveles:**<br>1. gameManager detecta la existencia de un error o la finalización de los niveles jugables.<br>2. gameManager muestra la "Finalización del Juego". |

---

### Caso de Uso 3: Finalización del Juego

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Finalización del Juego |
| **Actores**        | Jugador y Sistema |
| **Propósito**      | El juego llega a su conclusión por diferentes opciones (error de carga, falta de niveles, o decisión del usuario). Muestra resultados y regresa al menú principal. |
| **Precondiciones** | 1. El juego está en proceso o en medio de Cambiar Nivel. |
| **Flujo Principal** | **Game Over o Partida Ganada:**<br>1. El sistema detecta el cumplimiento de alguna condición de victoria o derrota.<br>2. gameManager es llamado para ejecutar finalizarJuego().<br>3. gameManager, mediante EstadisticasManager, recibe las estadísticas del jugador.<br>4. gameManager cambia a la pantalla de resultados.<br>5. En sesión multijugador, se cierra la sala.<br>6. El sistema regresa al jugador al menú principal.<br><br>**Salida del juego por el jugador:**<br>1. El jugador selecciona "Salir del Nivel".<br>2. gameManager recibe la entrada y llama a finalizarJuego().<br>3. gameManager recibe las estadísticas del jugador.<br>4. gameManager muestra pantalla de resultados.<br>5. En sesión multijugador, se cierra la sala.<br>6. El sistema regresa al menú principal. |
| **Flujo Alternativo** | Sin flujos alternativos. |

## 2.-Casos de uso interno

### Caso de Uso 1: Movimiento del Personaje

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Movimiento del Personaje |
| **Actores**        | Jugador |
| **Propósito**      | Proporcionar al jugador la habilidad de mover a su personaje dentro de los niveles. |
| **Precondiciones** | 1. El personaje se encuentra dentro de un nivel.<br>2. Se tiene un control o teclado para realizar los movimientos. |
| **Flujo Principal** | 1. El jugador moverá a su personaje con las teclas direccionales.<br>2. Se tomará como entrada la dirección con HandleInput.<br>3. Se calcula la nueva posición del personaje.<br>4. Se comprueba si el personaje choca con algún objeto o parte del escenario.<br>5. Si no ocurren colisiones, se actualiza en pantalla la posición del personaje.<br>6. Mientras el personaje esté en movimiento, su animación cambia a la de correr. |
| **Flujo Alternativo** | **Si se suelta la tecla de movimiento:**<br>1. Se detecta que ya no hay entrada de movimiento.<br>2. Se define que el personaje está quieto.<br>3. La animación se actualiza a estado sin movimiento.<br><br>**Si el personaje colisiona con el escenario:**<br>1. Se detecta colisión con el escenario.<br>2. El personaje no actualiza su posición. |

---

### Caso de Uso 2: Salto del Personaje

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Salto del Personaje |
| **Actores**        | Jugador |
| **Propósito**      | Proporcionar al jugador la habilidad de hacer saltar a su personaje dentro de los niveles. |
| **Precondiciones** | 1. El personaje se encuentra dentro de un nivel.<br>2. El personaje se encuentra en el suelo.<br>3. Se tiene un control o teclado para realizar los movimientos. |
| **Flujo Principal** | 1. El jugador presiona la tecla para "Saltar".<br>2. Se verifica con HandleInput si se está en el suelo.<br>3. Se aplica "JumpForce" y se actualiza que el personaje no está en el suelo.<br>4. Se actualiza la animación a estado de salto.<br>5. Se aplica gravedad con updatePhysics para hacer caer al personaje. |
| **Flujo Alternativo** | **Si el personaje no se encuentra en el suelo:**<br>1. Se detecta entrada de salto.<br>2. Se verifica que el personaje ya está en el aire.<br>3. No se realizan los pasos para saltar. |

---

### Caso de Uso 3: Recolección de Objetos

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Recolección de Objetos |
| **Actores**        | Jugador |
| **Propósito**      | Añadir al contador y estadísticas del personaje el objeto con el que colisione. |
| **Precondiciones** | 1. El personaje y el objeto están dentro de un nivel.<br>2. Existe CollisionManager para determinar colisiones con objetos. |
| **Flujo Principal** | 1. El personaje está en movimiento.<br>2. CollisionManager detecta colisión con un objeto.<br>3. Se llama a addCollectible para incrementar objetos recolectados.<br>4. Si es tipo "TRASH" y cantidad < 50, aumenta el contador. |
| **Flujo Alternativo** | **Si se lleva el máximo de TRASH (50):**<br>1. El sistema indica contador en 50.<br>2. El objeto TRASH no se añade. |

---

### Caso de Uso 4: Habilidad Especial (Sonic y Knuckles)

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Habilidad Especial de los Personajes (Sonic y Knuckles) |
| **Actores**        | Jugador |
| **Propósito**      | Permitir al jugador activar la habilidad especial del personaje. |
| **Precondiciones** | 1. Personaje dentro de nivel.<br>2. Personaje en suelo.<br>3. Habilidad disponible.<br>4. Personaje tiene vidas necesarias.<br>5. Control/teclado disponible. |
| **Flujo Principal** | 1. Jugador presiona tecla de habilidad.<br>2. Sistema detecta entrada con handleInput/handleAbilityInput.<br>3. Se llama a useAbility y resta lives.<br>4. isAbilityActive cambia a true.<br>5. Animación cambia a estado de habilidad.<br>6. Al terminar duración, isAbilityActive cambia a false. |
| **Flujo Alternativo** | **Sin vidas necesarias:**<br>1. lives = 0, no se activa habilidad.<br><br>**Habilidad ya activa:**<br>1. isAbilityActive = true, entrada ignorada.<br><br>**Personaje no en suelo:**<br>1. isGrounded = false, entrada ignorada. |

---

### Caso de Uso 5: Habilidad de Volar (Tails)

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Habilidad de Volar (Tails) |
| **Actores**        | Jugador |
| **Propósito**      | Permitir al jugador hacer que Tails vuele con tiempo limitado. |
| **Precondiciones** | 1. Tails dentro de nivel.<br>2. Tails no en suelo.<br>3. Tails no volando.<br>4. MAX_TIME_FLY no excedido. |
| **Flujo Principal** | 1. Jugador mantiene tecla arriba en aire.<br>2. Sistema llama a startFlying().<br>3. isFlying = true, animación a volar, velocidad vertical = flySpeed.<br>4. Tails sube mientras se mantenga tecla y tiempo < máximo.<br>5. Al soltar tecla o exceder tiempo, se llama stopFlying(). |
| **Flujo Alternativo** | **Tails en suelo:**<br>1. isGrounded = true, realiza salto común con JumpForce.<br><br>**Tails ya volando:**<br>1. isFlying = true, no inicia vuelo.<br><br>**Choca con suelo volando:**<br>1. CollisionManager detecta colisión, isGrounded = true.<br>2. Se llama stopFlying().<br><br>**Excede tiempo máximo:**<br>1. flyTime alcanza MAX_FLY_TIME.<br>2. Se llama stopFlying(). |

---

### Caso de Uso 6: Habilidad Especial de Tails (Invocación de Robot)

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Habilidad Especial de Tails (Invocación de Robot) |
| **Actores**        | Jugador |
| **Propósito**      | Permitir al jugador invocar robot que brinde ayuda. |
| **Precondiciones** | 1. Tails dentro de nivel.<br>2. isAbilityActive = False.<br>3. Robot listo (no en cooldown).<br>4. Existe gameServer. |
| **Flujo Principal** | 1. Jugador presiona tecla de habilidad.<br>2. Sistema llama a useAbility().<br>3. isAbilityActive = true.<br>4. Animación cambia a estado con robot.<br>5. gameServer crea robot en posición de Tails.<br>6. Al terminar timer, isAbilityActive = false y animación vuelve a normal. |
| **Flujo Alternativo** | **Habilidad ya activa:**<br>1. isAbilityActive = true, entrada ignorada.<br><br>**En cooldown:**<br>1. cooldown no terminado, entrada ignorada. |

---

### Caso de Uso 7: Robot de Tails - Recolección de Basura

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Robot de Tails – Recolección de Basura |
| **Actores**        | Sistema |
| **Propósito**      | Robot recolecta basura automáticamente sin intervención jugador. |
| **Precondiciones** | 1. Robot dentro de nivel.<br>2. Existen objetos TRASH.<br>3. Se usa gameServer y CollisionManager. |
| **Flujo Principal** | 1. Sistema hace update() del robot.<br>2. En estado "moverse a basura": calcula dirección y posición.<br>3. Si choca con basura: estado a "recolectando" y reinicia timer.<br>4. En "recolectando": incrementa timer y busca basura cercana.<br>5. Si capacidad máxima: estado a "Moviéndose a máquina reciclaje". |
| **Flujo Alternativo** | **No hay basura inicialmente:**<br>1. Usa targetTrash(), si no encuentra, estado a "Autodestrucción".<br><br>**Excede tiempo recolección:**<br>1. Si basura > 0: mueve a máquina cercana.<br>2. Si basura = 0: estado a "Autodestrucción".<br><br>**Basura desaparece:**<br>1. Busca otra basura cercana.<br>2. Si no encuentra: mueve a máquina. |

---

### Caso de Uso 8: Robot de Tails - Depositar Basura

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Robot de Tails – Depositar Basura |
| **Actores**        | Sistema |
| **Propósito**      | Robot deposita basura recolectada en máquina reciclaje. |
| **Precondiciones** | 1. Robot dentro de nivel.<br>2. Contador basura > 0.<br>3. Se usa gameServer y CollisionManager. |
| **Flujo Principal** | 1. Sistema hace update() del robot.<br>2. En estado "Moviéndose a máquina": calcula dirección.<br>3. Si choca con máquina: estado a "Entregando basura".<br>4. En "Entregando": añade basura a máquina, contador = 0, autodestrucción. |
| **Flujo Alternativo** | **No hay máquina reciclaje:**<br>1. Estado a "Autodestrucción". |

---

### Caso de Uso 9: Robot de Tails - Autodestrucción

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Robot de Tails – Autodestrucción |
| **Actores**        | Sistema |
| **Propósito**      | Robot se autodestruye al completar trabajo. |
| **Precondiciones** | 1. Robot dentro de nivel.<br>2. Trabajo completado (no más basura/máquinas, tiempo acabado).<br>3. Se usa gameServer y CollisionManager. |
| **Flujo Principal** | 1. Sistema hace update() del robot.<br>2. Si cumple condiciones: llama a selfDestruct().<br>3. active = false.<br>4. Se elimina de lista robots activos de Tails. |
| **Flujo Alternativo** | Sin flujos alternativos. |

---

### Caso de Uso 10: Eggman - Movimiento

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Eggman – Movimiento |
| **Actores**        | Sistema |
| **Propósito**      | Eggman se mueve horizontalmente entre puntos definidos. |
| **Precondiciones** | 1. Eggman dentro de nivel.<br>2. startX y endX definidos con valores distintos.<br>3. Se usa gameServer y CollisionManager. |
| **Flujo Principal** | 1. Sistema hace update() de Eggman.<br>2. Calcula nueva posición x con velocidad y delta tiempo.<br>3. Verifica si llegó a endX.<br>4. Si alcanza endX: invierte dirección hacia startX.<br>5. Reproduce animación de caminar. |
| **Flujo Alternativo** | Sin flujos alternativos. |

---

### Caso de Uso 11: Eggman - Ataque

| Campo              | Descripción |
|--------------------|-------------|
| **Caso de Uso**    | Eggman – Ataque |
| **Actores**        | Sistema |
| **Propósito**      | Eggman determina si puede atacar a jugador. |
| **Precondiciones** | 1. Eggman y al menos un jugador en nivel.<br>2. Método para detectar jugador cercano.<br>3. Se usa gameServer y CollisionManager. |
| **Flujo Principal** | 1. Sistema hace update() de Eggman.<br>2. Revisa checkAttackCondition().<br>3. Con CollisionManager determina condiciones de ataque.<br>4. Si se cumplen: isAttacking = true, sino false. |
| **Flujo Alternativo** | **No hay jugadores cerca o no cumple condiciones:**<br>1. isAttacking = false. |

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
