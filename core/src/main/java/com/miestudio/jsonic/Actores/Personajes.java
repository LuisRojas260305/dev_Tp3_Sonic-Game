package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Server.domain.InputState;

/**
 * Clase base abstracta para todos los personajes jugables en el juego.
 * Proporciona la lógica fundamental para la física, el estado y la gestión de animaciones.
 */
public abstract class Personajes extends Actor {

    /**
     * Enumeración que define los tipos de animaciones disponibles para los personajes.
     * Utilizado para una gestión de animaciones más robusta y menos propensa a errores.
     */
    public enum AnimationType {
        /** Animación de inactividad. */
        IDLE,
        /** Animación de correr. */
        RUN,
        /** Animación de salto. */
        JUMP,
        /** Animación de rodar. */
        ROLL,
        /** Animación de habilidad especial. */
        ABILITY
    }

    /** Tiempo de estado actual de la animación. */
    public float stateTime;
    /** Posición X del personaje. */
    protected float x;
    /** Posición Y del personaje. */
    protected float y;
    /** Posición X previa del personaje para interpolación. */
    protected float prevX;
    /** Posición Y previa del personaje para interpolación. */
    protected float prevY; // Para interpolación
    /** Indica si el personaje está mirando a la derecha. */
    protected boolean facingRight = true;
    /** Indica si el personaje está en el suelo. */
    protected boolean isGrounded = true;
    /** La animación actual que se está reproduciendo. */
    public Animation<TextureRegion> currentAnimation;
    /** Animación de inactividad. */
    public Animation<TextureRegion> idleAnimation;
    /** Animación de correr. */
    public Animation<TextureRegion> runAnimation;
    /** Animación de salto. */
    public Animation<TextureRegion> jumpAnimation;
    /** Animación de rodar. */
    public Animation<TextureRegion> rollAnimation;

    /** Velocidad vertical del personaje. */
    protected float velocityY = 0;
    /** Fuerza de gravedad aplicada al personaje. */
    protected final float gravity = -800f;
    /** Fuerza de salto aplicada al personaje. */
    protected final float jumpForce = 500f;
    /** Indica si el personaje puede saltar. */
    protected boolean canJump = true;

    /** Indica si el personaje está rodando. */
    public boolean isRolling = false;

    protected boolean isAbilityActive = false;
    /** ID del jugador asociado a este personaje. */
    protected int playerId;
    /** Velocidad de movimiento horizontal del personaje. */
    protected float moveSpeed = 300f;

    private float predictedX;

    private float predictedY;

    /**
     * Método abstracto para que cada personaje implemente su habilidad especial.
     */
    public abstract void useAbility();

    /**
     * Libera los recursos asociados al personaje.
     * Las subclases deben implementar este método si tienen recursos propios que liberar.
     */
    public abstract void dispose();

    /**
     * Actualiza el estado del personaje, incluyendo la física y el tiempo de animación.
     * Este método debe ser llamado en cada fotograma del juego.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    public void update(float delta, CollisionManager collisionManager) {
        stateTime += delta;
        updatePhysics(delta, collisionManager);
    }

    /**
     * Aplica la física al personaje, incluyendo gravedad y detección de suelo.
     * Este método es llamado internamente por {@link #update(float, CollisionManager)}.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    private void updatePhysics(float delta, CollisionManager collisionManager){
        // Aplicar gravedad a la velocidad vertical
        velocityY += gravity * delta;

        // Calcular la posición Y potencial en el siguiente fotograma
        float nextY = y + velocityY * delta;

        // Crear un rectángulo en la posición actual para detectar el suelo debajo
        Rectangle currentBounds = new Rectangle(x, y, getWidth(), getHeight());
        float groundY = collisionManager.getGroundY(currentBounds);

        // Comprobar si hay suelo y si el personaje está a punto de atravesarlo
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
     * Este método es llamado por el servidor (Host) basado en los InputState recibidos,
     * o por el cliente para predicción local.
     * @param input El estado de los botones del jugador.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
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

        // Limitar posición dentro del mapa para evitar que el personaje salga de los límites
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
     * Establece la animación actual del personaje.
     * Reinicia el tiempo de estado de la animación si la nueva animación es diferente a la actual.
     * @param newAnimation La nueva animación a establecer.
     */
    public void setCurrentAnimation(Animation<TextureRegion> newAnimation) {
        if (currentAnimation != newAnimation) {
            currentAnimation = newAnimation;
            stateTime = 0f;
        }
    }

    /**
     * Establece la animación actual del personaje basándose en un tipo de animación predefinido.
     * @param animationType El tipo de animación a establecer (IDLE, RUN, JUMP, ROLL, ABILITY).
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
     * Obtiene la posición X actual del personaje.
     * @return La posición X del personaje.
     */
    public float getX() { return x; }
    /**
     * Establece la posición X del personaje.
     * @param x La nueva posición X.
     */
    public void setPlayerX(float x) { this.x = x; }
    /**
     * Obtiene la posición Y actual del personaje.
     * @return La posición Y del personaje.
     */
    public float getY() { return y; }
    /**
     * Establece la posición Y del personaje.
     * @param y La nueva posición Y.
     */
    public void setPlayerY(float y) { this.y = y; }
    /**
     * Obtiene la posición X previa del personaje.
     * @return La posición X previa del personaje.
     */
    public float getPrevX() { return prevX; }
    /**
     * Obtiene la posición Y previa del personaje.
     * @return La posición Y previa del personaje.
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
     * Verifica si el personaje está mirando a la derecha.
     * @return true si el personaje mira a la derecha, false en caso contrario.
     */
    public boolean isFacingRight() { return facingRight; }
    /**
     * Establece la dirección a la que mira el personaje.
     * @param facingRight true para mirar a la derecha, false para mirar a la izquierda.
     */
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    /**
     * Obtiene el ID del jugador asociado a este personaje.
     * @return El ID del jugador.
     */
    public int getPlayerId() { return playerId; }
    /**
     * Verifica si el personaje está en el suelo.
     * @return true si el personaje está en el suelo, false en caso contrario.
     */
    public boolean isGrounded() { return isGrounded; }
    /**
     * Establece si el personaje está en el suelo.
     * @param grounded true si el personaje está en el suelo, false en caso contrario.
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
     * Obtiene la animación de inactividad.
     * @return La animación de inactividad.
     */
    public Animation<TextureRegion> getIdleAnimation() { return idleAnimation; }
    /**
     * Obtiene la animación de correr.
     * @return La animación de correr.
     */
    public Animation<TextureRegion> getRunAnimation() { return runAnimation; }
    /**
     * Obtiene la animación de salto.
     * @return La animación de salto.
     */
    public Animation<TextureRegion> getJumpAnimation() { return jumpAnimation; }
    /**
     * Obtiene la animación de rodar.
     * @return La animación de rodar.
     */
    public Animation<TextureRegion> getRollAnimation() { return rollAnimation; }
    /**
     * Establece la posición del personaje.
     * @param x La nueva posición X.
     * @param y La nueva posición Y.
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Establece la posición previa del personaje.
     * @param x La posición X previa.
     * @param y La posición Y previa.
     */
    public void setPreviousPosition(float x, float y) {
        this.prevX = x;
        this.prevY = y;
    }

    /**
     * Establece la posición predicha del personaje.
     * @param x La posición X predicha.
     * @param y La posición Y predicha.
     */
    public void setPredictedPosition(float x, float y) {
        this.predictedX = x;
        this.predictedY = y;
    }

    /**
     * Obtiene la posición X predicha del personaje.
     * @return La posición X predicha.
     */
    public float getPredictedX() { return predictedX; }
    /**
     * Obtiene la posición Y predicha del personaje.
     * @return La posición Y predicha.
     */
    public float getPredictedY() { return predictedY; }

    /**
     * Obtiene el fotograma actual de la animación.
     * @return El TextureRegion del fotograma actual.
     */
    public TextureRegion getCurrentFrame() {
        return currentAnimation.getKeyFrame(stateTime, true);
    }

    /**
     * Devuelve el nombre de la animación actual.
     * @return El nombre de la animación actual como String (ej. "idle", "run").
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
     * Obtiene el tiempo de estado actual de la animación.
     * @return El tiempo de estado de la animación.
     */
    public float getAnimationStateTime() {
        return stateTime;
    }

    /**
     * Establece el tiempo de estado de la animación.
     * @param stateTime El nuevo tiempo de estado de la animación.
     */
    public void setAnimationStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    /**
     * Obtiene los límites de colisión del personaje.
     * @return Un objeto Rectangle que representa los límites del personaje.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }

    /**
     * Obtiene el ancho del fotograma actual de la animación.
     * @return El ancho del fotograma.
     */
    public float getWidth() {
        return currentAnimation.getKeyFrame(0).getRegionWidth();
    }

    /**
     * Obtiene la altura del fotograma actual de la animación.
     * @return La altura del fotograma.
     */
    public float getHeight() {
        return currentAnimation.getKeyFrame(0).getRegionHeight();
    }
}
