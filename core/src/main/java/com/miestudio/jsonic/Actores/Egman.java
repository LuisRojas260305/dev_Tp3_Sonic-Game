package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Clase que representa al enemigo Egman en el juego.
 * Se desplaza horizontalmente a una altura fija.
 */
public class Egman {

    // Propiedades del enemigo
    private float x;
    private float y;
    private float width;
    private float height;
    private float velocityX;
    private float startX;
    private float endX;
    private boolean movingRight;
    
    // Animaciones
    private Animation<TextureRegion> walkAnimation;
    private float stateTime;
    
    // Estado
    private boolean isAttacking;

    /**
     * Constructor para crear una instancia de Egman.
     * 
     * @param startX Posición X inicial
     * @param endX Posición X final (límite de patrulla)
     * @param y Altura fija en la que se desplaza
     * @param walkAnimation Animación de caminata
     * @param attackAnimation Animación de ataque
     * @param speed Velocidad de movimiento
     */
    public Egman(float startX, float endX, float y, 
                 Animation<TextureRegion> walkAnimation, 
                 float speed) {
        this.x = startX;
        this.y = y;
        this.startX = startX;
        this.endX = endX;
        this.walkAnimation = walkAnimation;
        this.velocityX = speed;
        this.movingRight = true;
        
        // Calcular dimensiones basadas en la primera animación
        TextureRegion firstFrame = walkAnimation.getKeyFrame(0);
        this.width = firstFrame.getRegionWidth();
        this.height = firstFrame.getRegionHeight();
    }

    /**
     * Actualiza el estado del enemigo en cada fotograma.
     * 
     * @param delta Tiempo transcurrido desde el último fotograma
     * @param collisionManager Gestor de colisiones
     */
    public void update(float delta, CollisionManager collisionManager) {
        stateTime += delta;
        
        // Actualizar posición
        if (movingRight) {
            x += velocityX * delta;
            if (x >= endX) {
                x = endX;
                movingRight = false;
            }
        } else {
            x -= velocityX * delta;
            if (x <= startX) {
                x = startX;
                movingRight = true;
            }
        }
        
        // Verificar si debe atacar (implementar lógica según necesidad)
        checkAttackCondition(collisionManager);
    }

    /**
     * Verifica si el enemigo debe iniciar un ataque.
     * 
     * @param collisionManager Gestor de colisiones
     */
    private void checkAttackCondition(CollisionManager collisionManager) {
        // Lógica para determinar si el jugador está cerca y Egman debe atacar.
        // Actualmente es un marcador de posición; se necesita implementar la detección de proximidad del jugador
        // y la activación de la animación de ataque y la lógica de daño.
        isAttacking = false; // Implementar lógica real según requerimientos
    }

    /**
     * Obtiene la región de textura actual para renderizar.
     * 
     * @return La textura correspondiente al estado actual
     */
    public TextureRegion getCurrentFrame() {
        
        return walkAnimation.getKeyFrame(stateTime, true);
    }

    /**
     * Obtiene los límites del enemigo para detección de colisiones.
     * 
     * @return Rectángulo que representa los límites
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isMovingRight() { return movingRight; }
}
