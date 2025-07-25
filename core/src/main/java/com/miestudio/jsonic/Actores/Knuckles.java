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
 * Representa al personaje Knuckles en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial de puñetazo cargado.
 */
public class Knuckles extends Personajes{
    private TextureAtlas atlasKnuckles;
    /** Animación de puñetazo de Knuckles. */
    public Animation<TextureRegion> PunchAnimation;
    /** Indica si Knuckles está realizando un puñetazo cargado. */
    public boolean isPunching = false;
    /** El poder actual del puñetazo cargado. */
    public float PunchPower = 0;
    /** El poder máximo que puede alcanzar el puñetazo cargado. */
    private final float MAX_PUNCH_POWER = 500f;
    
    public Knuckles(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.atlasKnuckles = atlas;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
        setAbilityAnimation(PunchAnimation);
    }

    @Override
    public void useAbility() {
        if (!isAbilityActive && isGrounded) {
            isAbilityActive = true;
            stateTime = 0f;

            // Forzar el cambio de animación
            currentAnimation = PunchAnimation;
            setCurrentAnimation(PunchAnimation);
           
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
            if (PunchAnimation.isAnimationFinished(stateTime)) {
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
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            idleFrames.add(atlasKnuckles.findRegion("KnucklesIdle" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            runFrames.add(atlasKnuckles.findRegion("KnucklesRun" + i));
        }

        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 2; i++){
            ballFrames.add(atlasKnuckles.findRegion("KnucklesHit" + i));
        }

        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 9; i++){
            jumpFrames.add(atlasKnuckles.findRegion("KnucklesJump" + i));
        }

        jumpAnimation = new Animation<>(0.2f, jumpFrames, Animation.PlayMode.NORMAL);

        Array<TextureRegion> PunchFrames = new Array<>();
        for (int i = 0; i < 10; i++){
            PunchFrames.add(atlasKnuckles.findRegion("KnucklesSkill" + i));
        }

        PunchAnimation = new Animation<>(0.07f, PunchFrames, Animation.PlayMode.LOOP);
    }

    @Override
    public TextureRegion getCurrentFrame() {
        // Simplifica esta lógica
        if (isAbilityActive) {
            return PunchAnimation.getKeyFrame(stateTime, true);
        }
        return currentAnimation.getKeyFrame(stateTime, true);
    }
    
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
