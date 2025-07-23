package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Knockles en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial de puñetazo cargado.
 */
public class Knockles extends Personajes{
    private TextureAtlas atlasKnockles;
    /** Animación de puñetazo de Knockles. */
    public Animation<TextureRegion> PunchAnimation;
    /** Indica si Knockles está realizando un puñetazo cargado. */
    public boolean isPunching = false;
    /** El poder actual del puñetazo cargado. */
    public float PunchPower = 0;
    /** El poder máximo que puede alcanzar el puñetazo cargado. */
    private final float MAX_PUNCH_POWER = 500f;
    
    public Knockles(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.atlasKnockles = atlas;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);

    }

    @Override
    public void useAbility() {
        if (isGrounded && !isAbilityActive) {
            isPunching = true;
            isAbilityActive = true;
            PunchPower = 0;
            setCurrentAnimation(PunchAnimation);
        }
    }
    
    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);

        if (isPunching) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                // Cargando poder
                PunchPower = Math.min(PunchPower + 100 * delta, MAX_PUNCH_POWER);
            } else {
                // Liberar habilidad
                float impulso = PunchPower * delta;
                x += facingRight ? impulso : -impulso;

                isPunching = false;
                isAbilityActive = false;

                // Transición suave después de la habilidad
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }

    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            idleFrames.add(atlasKnockles.findRegion("KnucklesIdle" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            runFrames.add(atlasKnockles.findRegion("KnucklesRun" + i));
        }

        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 2; i++){
            ballFrames.add(atlasKnockles.findRegion("KnucklesHit" + i));
        }

        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 9; i++){
            jumpFrames.add(atlasKnockles.findRegion("KnucklesJump" + i));
        }

        jumpAnimation = new Animation<>(0.2f, jumpFrames, Animation.PlayMode.NORMAL);

        Array<TextureRegion> PunchFrames = new Array<>();
        for (int i = 0; i < 10; i++){
            PunchFrames.add(atlasKnockles.findRegion("KnucklesSkill" + i));
        }

        PunchAnimation = new Animation<>(0.17f, PunchFrames, Animation.PlayMode.LOOP);
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
