package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Vector2;
import com.miestudio.jsonic.Util.CollisionManager;

public class EAvispa extends Personajes {
    private Vector2 targetPosition; // Posición del árbol objetivo
    private float speed = 100f; // Velocidad de movimiento de la avispa
    private float destroyTimer = 0; // Temporizador para autodestrucción
    private static final float DESTROY_DELAY = 2.0f; // Retraso antes de autodestruirse

    public EAvispa(float x, float y, TextureAtlas atlas, Vector2 targetPosition) {
        this.x = x;
        this.y = y;
        this.targetPosition = targetPosition;
        this.stateTime = 0;
        cargarAnimaciones(atlas);
        setCurrentAnimation(idleAnimation); // O una animación de vuelo si existe
    }

    private void cargarAnimaciones(TextureAtlas atlas) {
        // Asumiendo que AtlasEnemy tiene regiones para la avispa, por ejemplo, "AvispaFly0", "AvispaFly1", etc.
        Array<TextureRegion> flyFrames = new Array<>();
        for (int i = 0; i < 2; i++) { // Ajusta el número de frames según tu atlas
            flyFrames.add(atlas.findRegion("Enemy" + i)); // Usar "Enemy" como prefijo
        }
        idleAnimation = new Animation<>(0.1f, flyFrames, Animation.PlayMode.LOOP); // Usar idleAnimation como animación de vuelo
    }

    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);

        // Mover hacia el objetivo
        if (Vector2.dst(x, y, targetPosition.x, targetPosition.y) > 5) { // Si no ha llegado al objetivo
            Vector2 direction = new Vector2(targetPosition.x - x, targetPosition.y - y).nor();
            x += direction.x * speed * delta;
            y += direction.y * speed * delta;

            // Actualizar dirección para renderizado
            if (direction.x > 0) facingRight = true;
            else if (direction.x < 0) facingRight = false;
        } else {
            // Ha llegado al objetivo, iniciar temporizador de autodestrucción
            destroyTimer += delta;
            if (destroyTimer >= DESTROY_DELAY) {
                this.setActivo(false); // Marcar para destrucción
                Gdx.app.log("EAvispa", "Avispa destruida en " + targetPosition.x + ", " + targetPosition.y);
            }
        }
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public void useAbility() {
        // La avispa no tiene habilidad
    }

    @Override
    public void dispose() {
        // No hay recursos propios que liberar aquí, el atlas se gestiona en Assets
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }
}
