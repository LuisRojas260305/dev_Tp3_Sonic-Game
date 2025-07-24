package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Knuckles en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones especificas y la logica para su habilidad especial de punetazo cargado.
 */
public class Knuckles extends Personajes {
    private TextureAtlas atlasKnuckles; /** Atlas de texturas específico para Knuckles. */
    /** Animacion de punetazo cargado de Knuckles. */
    public Animation<TextureRegion> PunchAnimation;
    /** Indica si Knuckles esta realizando un punetazo cargado. */
    public boolean isPunching = false;
    /** El poder actual acumulado del punetazo cargado. */
    public float PunchPower = 0;
    /** El poder maximo que puede alcanzar el punetazo cargado. */
    private final float MAX_PUNCH_POWER = 500f;

    /**
     * Constructor para el personaje Knuckles.
     * @param playerId El ID del jugador asociado a este Knuckles.
     * @param atlas El TextureAtlas que contiene las texturas de Knuckles.
     */
    public Knuckles(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.atlasKnuckles = atlas;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);

    }

    /**
     * Activa la habilidad especial de punetazo cargado de Knuckles.
     * Solo se puede activar si esta en el suelo y no hay otra habilidad activa.
     */
    @Override
    public void useAbility() {
        if (isGrounded && !isAbilityActive) {
            isPunching = true;
            isAbilityActive = true;
            PunchPower = 0;
            setCurrentAnimation(PunchAnimation);
        }
    }

    /**
     * Actualiza el estado de Knuckles en cada fotograma.
     * Incluye la logica para cargar y liberar el punetazo cargado.
     * @param delta El tiempo transcurrido desde el último fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);

        if (isPunching) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                // Cargando poder: incrementa PunchPower hasta el máximo.
                PunchPower = Math.min(PunchPower + 100 * delta, MAX_PUNCH_POWER);
            } else {
                // Liberar habilidad: aplica un impulso horizontal basado en el poder cargado.
                float impulso = PunchPower * delta;
                x += facingRight ? impulso : -impulso;

                isPunching = false;
                isAbilityActive = false;

                // Transicion suave a la animacion de inactividad o rodar despues de la habilidad.
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }

    /**
     * Carga todas las animaciones específicas de Knuckles desde su TextureAtlas.
     */
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

        PunchAnimation = new Animation<>(0.17f, PunchFrames, Animation.PlayMode.LOOP);
    }

    /**
     * Libera los recursos específicos de Knuckles.
     * En este caso, el TextureAtlas se gestiona centralmente en la clase Assets, por lo que no se libera aquí.
     */
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }

}
