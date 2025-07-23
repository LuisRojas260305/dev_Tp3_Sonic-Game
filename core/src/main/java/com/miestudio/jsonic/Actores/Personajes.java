package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.InputState;

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
    
    /** ID del jugador asociado a este personaje. */
    protected int playerId;
    /** Velocidad de movimiento horizontal del personaje. */
    protected float moveSpeed = 300f;

    
    /**
     * Libera los recursos asociados al personaje.
     * Las subclases deben implementar este método si tienen recursos propios que liberar.
     */
    public abstract void dispose();

    /**
     * Actualiza el estado del personaje, incluyendo la física y el tiempo de animación.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    public void update(float delta, CollisionManager collisionManager) {
        stateTime += delta;
        updatePhysics(delta, collisionManager);
    }

    /**
     * Aplica la física al personaje, incluyendo gravedad y detección de suelo.
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
     * Este método es llamado por el servidor (Host) basado en los InputState recibidos.
     * @param input El estado de los botones del jugador.
     */
    public void handleInput(InputState input, CollisionManager collisionManager, float delta) {
        Gdx.app.log("Personajes", "Player " + playerId + " handleInput: " + input.isLeft() + ", " + input.isRight() + ", " + input.isUp() + ", " + input.isDown() + ", " + input.isAbility());

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

        // Limitar posición dentro del mapa
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

    public void setCurrentAnimation(Animation<TextureRegion> newAnimation) {
        if (currentAnimation != newAnimation) {
            currentAnimation = newAnimation;
            stateTime = 0f;
        }
    }

    

    /**
     * Establece la animación actual del personaje.
     * Reinicia el tiempo de estado de la animación si la nueva animación es diferente a la actual.
     * @param newAnimation La nueva animación a establecer.
     */
    public void setAnimation(AnimationType animationType) {
        switch (animationType) {
            case IDLE: setCurrentAnimation(idleAnimation); break;
            case RUN: setCurrentAnimation(runAnimation); break;
            case JUMP: setCurrentAnimation(jumpAnimation); break;
            case ROLL: setCurrentAnimation(rollAnimation); break;
            // Añadir casos para animaciones específicas de cada personaje si es necesario
        }
    }

    // Getters y Setters
    /**
     * Obtiene la posición X actual del personaje.
     * @return La posición X del personaje.
     */
    public float getX() { return x; }
    public void setPlayerX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setPlayerY(float y) { this.y = y; }
    public float getPrevX() { return prevX; }
    public float getPrevY() { return prevY; }
    public float getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(float speed) { this.moveSpeed = speed; }
    public boolean isFacingRight() { return facingRight; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public int getPlayerId() { return playerId; }
    public boolean isGrounded() { return isGrounded; }
    public void setGrounded(boolean grounded) { isGrounded = grounded; }
    public float getVelocityY() { return velocityY; }
    public void setVelocityY(float velocityY) { this.velocityY = velocityY; }
    public boolean getCanJump() { return canJump; }
    public void setCanJump(boolean canJump) { this.canJump = canJump; }
    public Animation<TextureRegion> getIdleAnimation() { return idleAnimation; }
    public Animation<TextureRegion> getRunAnimation() { return runAnimation; }
    public Animation<TextureRegion> getJumpAnimation() { return jumpAnimation; }
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
     * Obtiene el fotograma actual de la animación.
     * @return El TextureRegion del fotograma actual.
     */
    public TextureRegion getCurrentFrame() {
        return currentAnimation.getKeyFrame(stateTime, true);
    }

    /**
     * Devuelve el nombre de la animación actual.
     * @return El nombre de la animación actual como String.
     */
    public String getCurrentAnimationName() {
        if (currentAnimation == idleAnimation) return AnimationType.IDLE.name().toLowerCase();
        if (currentAnimation == runAnimation) return AnimationType.RUN.name().toLowerCase();
        if (currentAnimation == jumpAnimation) return AnimationType.JUMP.name().toLowerCase();
        if (currentAnimation == rollAnimation) return AnimationType.ROLL.name().toLowerCase();
        return "unknown"; // O manejar de otra forma
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
