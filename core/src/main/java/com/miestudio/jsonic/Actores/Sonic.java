package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Sonic en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones especificas y la logica para su habilidad especial Spin Dash.
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic; /** Atlas de texturas específico para Sonic. */
    /**
     * Animacion de Spin Dash de Sonic.
     */
    public Animation<TextureRegion> spinDashAnimation;
    /**
     * Indica si Sonic esta realizando un Spin Dash.
     */
    public boolean isSpinning = false; /** Indica si Sonic esta realizando un Spin Dash. */
    /**
     * El poder actual del Spin Dash cargado.
     */
    public float spinPower = 0; /** El poder actual del Spin Dash cargado. */
    /**
     * El poder maximo que puede alcanzar el Spin Dash cargado.
     */
    private final float MAX_SPIN_POWER = 500f; /** El poder maximo que puede alcanzar el Spin Dash cargado. */

    /**
     * Constructor para el personaje Sonic.
     *
     * @param playerId El ID del jugador asociado a este Sonic.
     * @param atlas    El TextureAtlas que contiene las texturas de Sonic.
     */
    public Sonic(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        this.atlasSonic = atlas;
        this.moveSpeed = 400f; // Sonic es mas rapido
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
    }

    /**
     * Activa la habilidad especial Spin Dash de Sonic.
     * Solo se puede activar si está en el suelo y no hay otra habilidad activa.
     */
    @Override
    public void useAbility() {
        if (isGrounded && !isAbilityActive) {
            isSpinning = true;
            isAbilityActive = true;
            spinPower = 0;
            setCurrentAnimation(spinDashAnimation);
        }
    }

    /**
     * Actualiza el estado de Sonic en cada fotograma.
     * Incluye la logica para cargar y liberar el Spin Dash.
     *
     * @param delta            El tiempo transcurrido desde el ultimo fotograma en segundos.
     * @param collisionManager El gestor de colisiones para interactuar con el entorno.
     */
    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);

        if (isSpinning) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                // Cargando poder: incrementa spinPower hasta el máximo.
                spinPower = Math.min(spinPower + 100 * delta, MAX_SPIN_POWER);
            } else {
                // Liberar habilidad: aplica un impulso horizontal basado en el poder cargado.
                float impulso = spinPower * delta;
                x += facingRight ? impulso : -impulso;

                isSpinning = false;
                isAbilityActive = false;

                // Transicion suave a la animacion de inactividad o rodar despues de la habilidad.
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }

    /**
     * Carga todas las animaciones especificas de Sonic desde su TextureAtlas.
     */
    private void cargarAnimaciones() {
        // Animacion idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasSonic.findRegion("SonicIdle" + i));
        }
        idleAnimation = new Animation<>(0.08f, idleFrames); // Frame time reducido

        // Animacion correr
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            runFrames.add(atlasSonic.findRegion("SonicRun" + i));
        }
        runAnimation = new Animation<>(0.08f, runFrames); // Frame time reducido

        // Animacion de bolita (roll)
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            TextureRegion region = atlasSonic.findRegion("SonicRoll" + i);
            if (region != null) ballFrames.add(region);
        }
        rollAnimation = new Animation<>(0.03f, ballFrames); // Frame time reducido

        // Animacion saltar
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            TextureRegion region = atlasSonic.findRegion("SonicJump" + i);
            if (region != null) jumpFrames.add(region);
        }
        jumpAnimation = new Animation<>(0.2f, jumpFrames);

        // Animacion Spin Dash
        Array<TextureRegion> spinDashFrames = new Array<>();
        for (int i = 0; i < 10; i++) {
            TextureRegion region = atlasSonic.findRegion("SonicSkill" + i);
            if (region != null) spinDashFrames.add(region);
        }
        spinDashAnimation = new Animation<>(0.04f, spinDashFrames); // Frame time reducido
    }

    /**
     * Libera los recursos especificos de Sonic.
     * En este caso, el TextureAtlas se gestiona centralmente en la clase Assets, por lo que no se libera aqui.
     */
    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
