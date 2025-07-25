package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

public class EAvispa extends Personajes {
    private TextureAtlas atlasEnemy;
    private Vector2 target;
    private float speed = 150f; // Velocidad de movimiento de la avispa

    public EAvispa(float x, float y, TextureAtlas atlas, Vector2 target) {
        this.x = x;
        this.y = y;
        this.atlasEnemy = atlas;
        this.target = target;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation); // Animación inicial
    }

    private void cargarAnimaciones() {
        // Animación de volar (EnemiVespoid1 a EnemiVespoid2)
        Array<TextureRegion> flyFrames = new Array<>();
        flyFrames.add(atlasEnemy.findRegion("EnemiVespoid1"));
        flyFrames.add(atlasEnemy.findRegion("EnemiVespoid2"));
        flyAnimation = new Animation<>(0.1f, flyFrames, Animation.PlayMode.LOOP);

        // Animación de estar quieta (EnemiVespoid4 a EnemiVespoid5)
        Array<TextureRegion> idleFrames = new Array<>();
        idleFrames.add(atlasEnemy.findRegion("EnemiVespoid4"));
        idleFrames.add(atlasEnemy.findRegion("EnemiVespoid5"));
        idleAnimation = new Animation<>(0.15f, idleFrames, Animation.PlayMode.LOOP);

        // Establecer las animaciones por defecto
        this.currentAnimation = idleAnimation;
        this.jumpAnimation = idleAnimation; // No salta
        this.runAnimation = flyAnimation; // Usar volar como correr
        this.rollAnimation = idleAnimation; // No rueda
        this.abilityAnimation = idleAnimation; // No tiene habilidad
    }

    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager); // Llama al update de Personajes para la física básica

        // Mover hacia el objetivo
        if (target != null) {
            float dx = target.x - x;
            float dy = target.y - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 5) { // Si la distancia es mayor a un umbral, moverse
                float moveX = (dx / distance) * speed * delta;
                float moveY = (dy / distance) * speed * delta;
                x += moveX;
                y += moveY;

                // Actualizar la dirección para el flip de la textura
                if (moveX > 0) {
                    facingRight = true;
                } else if (moveX < 0) {
                    facingRight = false;
                }
                setCurrentAnimation(flyAnimation); // Usar animación de volar cuando se mueve
            } else {
                setCurrentAnimation(idleAnimation); // Usar animación de quieta cuando llega al objetivo
            }
        }
    }

    @Override
    public void useAbility() {
        // La avispa no tiene una habilidad especial
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }

    public void setTarget(Vector2 target) {
        this.target = target;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Vector2 getTarget() {
        return target;
    }

    public boolean isAvispa() {
        return true;
    }
}
