package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Server.domain.InputState;

/**
 * Clase base abstracta para todos los personajes jugables en el juego.
 * Proporciona la logica fundamental para la fisica, el estado y la gestion de animaciones.
 */
public abstract class Personajes extends Actor {

    /**
     * Enumeracion que define los tipos de animaciones disponibles para los personajes.
     * Utilizado para una gestion de animaciones mas robusta y menos propensa a errores.
     */
    public enum AnimationType {
        /** Animacion de inactividad. */
        IDLE,
        /** Animacion de correr. */
        RUN,
        /** Animacion de salto. */
        JUMP,
        /** Animacion de rodar. */
        ROLL,
        /** Animacion de habilidad especial. */
        ABILITY
    }

    /** Tiempo de estado actual de la animacion. */
    public float stateTime;
    /** Posicion X del personaje. */
    protected float x;
    /** Posicion Y del personaje. */
    protected float y;
    /** Posicion X previa del personaje para interpolacion. */
    protected float prevX;
    /** Posicion Y previa del personaje para interpolacion. */
    protected float prevY; // Para interpolacion
    /** Indica si el personaje esta mirando a la derecha. */
    protected boolean facingRight = true;
    /** Indica si el personaje esta en el suelo. */
    protected boolean isGrounded = true;
    /** La animacion actual que se esta reproduciendo. */
    public Animation<TextureRegion> currentAnimation;
    /** Animacion de inactividad. */
    public Animation<TextureRegion> idleAnimation;
    /** Animacion de correr. */
    public Animation<TextureRegion> runAnimation;
    /** Animacion de salto. */
    public Animation<TextureRegion> jumpAnimation;
    /** Animacion de rodar. */
    public Animation<TextureRegion> rollAnimation;

    /** Velocidad vertical del personaje. */
    protected float velocityY = 0;
    /** Fuerza de gravedad aplicada al personaje. */
    protected final float gravity = -800f;
    /** Fuerza de salto aplicada al personaje. */
    protected final float jumpForce = 500f;
    /** Indica si el personaje puede saltar. */
    protected boolean canJump = true;

    /** Indica si el personaje esta rodando. */
    public boolean isRolling = false;

    protected boolean isAbilityActive = false; /** Indica si la habilidad especial del personaje esta activa. */
    /** ID del jugador asociado a este personaje. */
    protected int playerId;
    /** Velocidad de movimiento horizontal del personaje. */
    protected float moveSpeed = 300f;

    private float predictedX; /** Posicion X predicha del personaje para suavizar el movimiento en el cliente. */
    private float predictedY; /** Posicion Y predicha del personaje para suavizar el movimiento en el cliente. */

    /**
     * Metodo abstracto para que cada personaje implemente su habilidad especial.
     */
    public abstract void useAbility();

    /**
     * Libera los recursos asociados al personaje.
     * Las subclases deben implementar este metodo si tienen recursos propios que liberar.
     */
    public abstract void dispose();

    /**
     * Actualiza el estado del personaje, incluyendo la fisica y el tiempo de animacion.
     * Este metodo debe ser llamado en cada fotograma del juego.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    public void update(float delta, CollisionManager collisionManager) {
        stateTime += delta;
        updatePhysics(delta, collisionManager);
    }

    /**
     * Aplica la fisica al personaje, incluyendo gravedad y deteccion de suelo.
     * Este metodo es llamado internamente por {@link #update(float, CollisionManager)}.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    private void updatePhysics(float delta, CollisionManager collisionManager){
        // Aplicar gravedad a la velocidad vertical
        velocityY += gravity * delta;

        // Calcular la posicion Y potencial en el siguiente fotograma
        float nextY = y + velocityY * delta;

        // Crear un rectangulo en la posicion actual para detectar el suelo debajo
        Rectangle currentBounds = new Rectangle(x, y, getWidth(), getHeight());
        float groundY = collisionManager.getGroundY(currentBounds);

        // Comprobar si hay suelo y si el personaje esta a punto de atravesarlo
        if (groundY >= 0 && y >= groundY && nextY <= groundY) {
            // Aterrizar en el suelo
            y = groundY;
            velocityY = 0;
            isGrounded = true;
        } else {
            // Estar en el aire (cayendo o saltando)
            y = nextY;
            isGrounded = false;
        }
    }

    /**
     * Maneja los inputs del jugador para actualizar el estado del personaje.
     * Este metodo es llamado por el servidor (Host) basado en los InputState recibidos,
     * o por el cliente para prediccion local.
     * @param input El estado de los botones del jugador.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos.
     */
    public void handleInput(InputState input, CollisionManager collisionManager, float delta) {
        boolean isMoving = false;

        if (input.isRight()){
            float nextX = x + moveSpeed * delta;
            Rectangle horizontalCheck = new Rectangle(
                nextX, y, getWidth(), getHeight()
            );

            if (!collisionManager.collides(horizontalCheck)){
                x = nextX;
                facingRight = true;
            }

            isMoving = true;
        }

        if (input.isLeft()){
            float nextX = x - moveSpeed * delta;
            Rectangle horizontalCheck = new Rectangle(
                nextX, y, getWidth(), getHeight()
            );

            if (!collisionManager.collides(horizontalCheck)){
                x = nextX;
                facingRight = false;
            }

            isMoving = true;
        }

        // Limitar posicion dentro del mapa para evitar que el personaje salga de los limites
        x = Math.max(0, Math.min(x, collisionManager.getMapWidth() - getWidth()));
        y = Math.max(0, Math.min(y, collisionManager.getMapHeight() - getHeight()));

        isRolling = input.isDown();

        Rectangle characterBounds = new Rectangle(x, y, getWidth(), getHeight());

        isGrounded = collisionManager.isOnGround(characterBounds);

        if (input.isUp() && isGrounded){
            velocityY = jumpForce;
            isGrounded = false;
            setCurrentAnimation(jumpAnimation);
        }

        if (isRolling && isGrounded){
            setCurrentAnimation(rollAnimation);
        } else if (isMoving && isGrounded) {
            setCurrentAnimation(runAnimation);
        } else if (!isGrounded) {
            setCurrentAnimation(jumpAnimation);
        } else {
            setCurrentAnimation(idleAnimation);
        }
    }

    /**
     * Establece la animacion actual del personaje.
     * Reinicia el tiempo de estado de la animacion si la nueva animacion es diferente a la actual.
     * @param newAnimation La nueva animacion a establecer.
     */
    public void setCurrentAnimation(Animation<TextureRegion> newAnimation) {
        if (currentAnimation != newAnimation) {
            currentAnimation = newAnimation;
            stateTime = 0f;
        }
    }

    /**
     * Establece la animacion actual del personaje basandose en un tipo de animacion predefinido.
     * @param animationType El tipo de animacion a establecer (IDLE, RUN, JUMP, ROLL, ABILITY).
     */
    public void setAnimation(AnimationType animationType) {
        switch (animationType) {
            case IDLE: setCurrentAnimation(idleAnimation); break;
            case RUN: setCurrentAnimation(runAnimation); break;
            case JUMP: setCurrentAnimation(jumpAnimation); break;
            case ROLL: setCurrentAnimation(rollAnimation); break;
            case ABILITY: /* Manejar animación de habilidad si es genérica, o dejar que la subclase la establezca */ break;
        }
    }

    /**
     * Obtiene la posicion X actual del personaje.
     * @return La posicion X del personaje.
     */
    public float getX() { return x; }
    /**
     * Establece la posicion X del personaje.
     * @param x La nueva posicion X.
     */
    public void setPlayerX(float x) { this.x = x; }
    /**
     * Obtiene la posicion Y actual del personaje.
     * @return La posicion Y del personaje.
     */
    public float getY() { return y; }
    /**
     * Establece la posicion Y del personaje.
     * @param y La nueva posicion Y.
     */
    public void setPlayerY(float y) { this.y = y; }
    /**
     * Obtiene la posicion X previa del personaje.
     * @return La posicion X previa del personaje.
     */
    public float getPrevX() { return prevX; }
    /**
     * Obtiene la posicion Y previa del personaje.
     * @return La posicion Y previa del personaje.
     */
    public float getPrevY() { return prevY; }
    /**
     * Obtiene la velocidad de movimiento horizontal del personaje.
     * @return La velocidad de movimiento.
     */
    public float getMoveSpeed() { return moveSpeed; }
    /**
     * Establece la velocidad de movimiento horizontal del personaje.
     * @param speed La nueva velocidad de movimiento.
     */
    public void setMoveSpeed(float speed) { this.moveSpeed = speed; }
    /**
     * Verifica si el personaje esta mirando a la derecha.
     * @return true si el personaje mira a la derecha, false en caso contrario.
     */
    public boolean isFacingRight() { return facingRight; }
    /**
     * Establece la direccion a la que mira el personaje.
     * @param facingRight true para mirar a la derecha, false para mirar a la izquierda.
     */
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    /**
     * Obtiene el ID del jugador asociado a este personaje.
     * @return El ID del jugador.
     */
    public int getPlayerId() { return playerId; }
    /**
     * Verifica si el personaje esta en el suelo.
     * @return true si el personaje esta en el suelo, false en caso contrario.
     */
    public boolean isGrounded() { return isGrounded; }
    /**
     * Establece si el personaje esta en el suelo.
     * @param grounded true si el personaje esta en el suelo, false en caso contrario.
     */
    public void setGrounded(boolean grounded) { isGrounded = grounded; }
    /**
     * Obtiene la velocidad vertical actual del personaje.
     * @return La velocidad vertical.
     */
    public float getVelocityY() { return velocityY; }
    /**
     * Establece la velocidad vertical del personaje.
     * @param velocityY La nueva velocidad vertical.
     */
    public void setVelocityY(float velocityY) { this.velocityY = velocityY; }
    /**
     * Verifica si el personaje puede saltar.
     * @return true si el personaje puede saltar, false en caso contrario.
     */
    public boolean getCanJump() { return canJump; }
    /**
     * Establece si el personaje puede saltar.
     * @param canJump true si el personaje puede saltar, false en caso contrario.
     */
    public void setCanJump(boolean canJump) { this.canJump = canJump; }
    /**
     * Obtiene la animacion de inactividad.
     * @return La animacion de inactividad.
     */
    public Animation<TextureRegion> getIdleAnimation() { return idleAnimation; }
    /**
     * Obtiene la animacion de correr.
     * @return La animacion de correr.
     */
    public Animation<TextureRegion> getRunAnimation() { return runAnimation; }
    /**
     * Obtiene la animacion de salto.
     * @return La animacion de salto.
     */
    public Animation<TextureRegion> getJumpAnimation() { return jumpAnimation; }
    /**
     * Obtiene la animacion de rodar.
     * @return La animacion de rodar.
     */
    public Animation<TextureRegion> getRollAnimation() { return rollAnimation; }
    /**
     * Establece la posicion del personaje.
     * @param x La nueva posicion X.
     * @param y La nueva posicion Y.
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Establece la posicion previa del personaje.
     * @param x La posicion X previa.
     * @param y La posicion Y previa.
     */
    public void setPreviousPosition(float x, float y) {
        this.prevX = x;
        this.prevY = y;
    }

    /**
     * Establece la posicion predicha del personaje.
     * @param x La posicion X predicha.
     * @param y La posicion Y predicha.
     */
    public void setPredictedPosition(float x, float y) {
        this.predictedX = x;
        this.predictedY = y;
    }

    /**
     * Obtiene la posicion X predicha del personaje.
     * @return La posicion X predicha.
     */
    public float getPredictedX() { return predictedX; }
    /**
     * Obtiene la posicion Y predicha del personaje.
     * @return La posicion Y predicha.
     */
    public float getPredictedY() { return predictedY; }

    /**
     * Obtiene el fotograma actual de la animacion.
     * @return El TextureRegion del fotograma actual.
     */
    public TextureRegion getCurrentFrame() {
        return currentAnimation.getKeyFrame(stateTime, true);
    }

    /**
     * Devuelve el nombre de la animacion actual.
     * @return El nombre de la animacion actual como String (ej. "idle", "run").
     */
    public String getCurrentAnimationName() {
        if (currentAnimation == idleAnimation) return AnimationType.IDLE.name().toLowerCase();
        if (currentAnimation == runAnimation) return AnimationType.RUN.name().toLowerCase();
        if (currentAnimation == jumpAnimation) return AnimationType.JUMP.name().toLowerCase();
        if (currentAnimation == rollAnimation) return AnimationType.ROLL.name().toLowerCase();
        // Si se añade una animación de habilidad, se podría añadir aquí
        return "unknown"; // O manejar de otra forma si la animación no es reconocida
    }

    /**
     * Obtiene el tiempo de estado actual de la animacion.
     * @return El tiempo de estado de la animacion.
     */
    public float getAnimationStateTime() {
        return stateTime;
    }

    /**
     * Establece el tiempo de estado de la animacion.
     * @param stateTime El nuevo tiempo de estado de la animacion.
     */
    public void setAnimationStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    /**
     * Obtiene los limites de colision del personaje.
     * @return Un objeto Rectangle que representa los limites del personaje.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    /**
     * Obtiene el ancho del fotograma actual de la animacion.
     * @return El ancho del fotograma.
     */
    public float getWidth() {
        return currentAnimation.getKeyFrame(0).getRegionWidth();
    }

    /**
     * Obtiene la altura del fotograma actual de la animacion.
     * @return La altura del fotograma.
     */
    public float getHeight() {
        return currentAnimation.getKeyFrame(0).getRegionHeight();
    }
}
