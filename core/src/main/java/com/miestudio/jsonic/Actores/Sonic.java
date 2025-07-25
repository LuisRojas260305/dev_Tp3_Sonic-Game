package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Sonic en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial Spin Dash.
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic;
    /** Animación de Spin Dash de Sonic. */
    public Animation<TextureRegion> spinKickAnimation;
    /** Indica si Sonic está realizando un Spin Dash. */
    public boolean isSpinning = false;
    /** El poder actual del Spin Dash cargado. */
    public float spinPower = 0;
    /** El poder máximo que puede alcanzar el Spin Dash cargado. */
    private final float MAX_SPIN_POWER = 500f;
    
    public Sonic(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        this.atlasSonic = atlas;
        this.moveSpeed = 400f;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
        setAbilityAnimation(spinKickAnimation);
    }

    @Override
    public void useAbility() {
        if (!isAbilityActive && isGrounded) {
            isAbilityActive = true;
            stateTime = 0f;

            // Forzar el cambio de animación
            currentAnimation = spinKickAnimation;
            setCurrentAnimation(spinKickAnimation);
            
        }
    }

    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);

        if (isAbilityActive) {
            // Solo detectar colisiones después de algunos frames
            
            if (stateTime > 0.1f) {
                Rectangle bounds = getBounds();
                for (Shape2D shape : collisionManager.getCollisionShapes()) {
                    if (shape instanceof Rectangle) {
                        Rectangle rect = (Rectangle) shape;
                        if (bounds.overlaps(rect)) {
                            
                        }
                    }
                }
            }

            // Finalizar la habilidad después de 0.5 segundos
            if (spinKickAnimation.isAnimationFinished(stateTime)) {
                isAbilityActive = false;
                // Restaurar animación apropiada
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                } else {
                    setCurrentAnimation(jumpAnimation);
                }
            }
        }
    }

    private void cargarAnimaciones() {
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasSonic.findRegion("SonicIdle" + i));
        }
        idleAnimation = new Animation<>(0.08f, idleFrames); // Frame time reducido

        // Animación correr
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            runFrames.add(atlasSonic.findRegion("SonicRun" + i));
        }
        runAnimation = new Animation<>(0.08f, runFrames); // Frame time reducido

        // Animación de bolita (roll)
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            TextureRegion region = atlasSonic.findRegion("SonicRoll" + i);
            if (region != null) ballFrames.add(region);
        }
        rollAnimation = new Animation<>(0.03f, ballFrames); // Frame time reducido

        // Animación saltar
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            TextureRegion region = atlasSonic.findRegion("SonicJump" + i);
            if (region != null) jumpFrames.add(region);
        }
        jumpAnimation = new Animation<>(0.2f, jumpFrames);

        // Animación de patada giratoria
        Array<TextureRegion> spinKickFrames = new Array<>();
        for (int i = 0; i < 10; i++) {
            TextureRegion region = atlasSonic.findRegion("SonicSkill" + i);
            if (region != null) spinKickFrames.add(region);
        }
        spinKickAnimation = new Animation<>(0.05f, spinKickFrames);
    
    }
    
    @Override
    public TextureRegion getCurrentFrame() {
        // Simplifica esta lógica
        if (isAbilityActive) {
            return spinKickAnimation.getKeyFrame(stateTime, true);
        }
        return currentAnimation.getKeyFrame(stateTime, true);
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
