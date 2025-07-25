package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.miestudio.jsonic.Util.CollisionManager;
import com.badlogic.gdx.Gdx;

/**
 * Clase que representa al enemigo Egman en el juego.
 * Se desplaza horizontalmente a una altura fija.
 */
public class Egman extends Personajes {

    private float velocityX;
    private float startX;
    private float endX;
    
    // Estado
    private boolean isAttacking;

    /**
     * Constructor para crear una instancia de Egman.
     * 
     * @param startX Posición X inicial
     * @param endX Posición X final (límite de patrulla)
     * @param y Altura fija en la que se desplaza
     * @param walkAnimation Animación de caminata
     * @param speed Velocidad de movimiento
     */
    public Egman(float startX, float endX, float y, 
                 Animation<TextureRegion> walkAnimation, 
                 float speed) {
        // No llamar a super() con argumentos, ya que Personajes no tiene un constructor con argumentos
        this.setPosition(startX, y);
        this.startX = startX;
        this.endX = endX;
        this.idleAnimation = walkAnimation; // Usar la animación de caminata como idle por simplicidad
        this.currentAnimation = idleAnimation;
        this.velocityX = speed;
        this.facingRight = true; // Inicializar facingRight
        
        // Las dimensiones se obtienen de la animación
        TextureRegion firstFrame = walkAnimation.getKeyFrame(0);
        setWidth(firstFrame.getRegionWidth());
        setHeight(firstFrame.getRegionHeight());
    }

    /**
     * Actualiza el estado del enemigo en cada fotograma.
     * 
     * @param delta Tiempo transcurrido desde el último fotograma
     * @param collisionManager Gestor de colisiones
     */
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager); // Llama a la lógica de física de Personajes
        
        // Actualizar posición
        if (facingRight) {
            x += velocityX * delta;
            if (x >= endX) {
                x = endX;
                facingRight = false;
            }
        } else {
            x -= velocityX * delta;
            if (x <= startX) {
                x = startX;
                facingRight = true;
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

    @Override
    public void useAbility() {
        // Egman no tiene una habilidad especial de jugador
    }

    @Override
    public void dispose() {
        // No hay recursos específicos de Egman que liberar aquí, ya que las animaciones se gestionan en Assets
    }
}