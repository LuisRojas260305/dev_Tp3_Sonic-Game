/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 *
 * @author usuario
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic;
    private TextureAtlas atlasSonicH;
    public Animation<TextureRegion> spinDashAnimation;
    public boolean isSpinning = false;
    public float spinPower = 0;
    private final float MAX_SPIN_POWER = 400f;

    public Sonic() {
        cargarAtlas();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
    }

    @Override
    public void usarHabilidad() {
        if (isGrounded && !enHabilidad) {
            isSpinning = true;
            enHabilidad = true;
            spinPower = MAX_SPIN_POWER;
            setCurrentAnimation(spinDashAnimation);
        }
    }
    
    @Override
    public void update(float delta) {
        super.update(delta);
        
        if (isSpinning) {
            // Aplicar impulso inmediatamente
            float impulso = spinPower * delta;
            x += facingRight ? impulso : -impulso;
            
            // Verificar si la animación ha terminado
            if (spinDashAnimation.isAnimationFinished(stateTime)) {
                isSpinning = false;
                enHabilidad = false;
                
                // Transición suave después de la habilidad
                if (isGrounded) {
                    setCurrentAnimation(isRolling ? rollAnimation : idleAnimation);
                }
            }
        }
    }
    
    private void cargarAtlas() {
        atlasSonic = new TextureAtlas(Gdx.files.internal("SonicAtlas.txt"));
        atlasSonicH = new TextureAtlas(Gdx.files.internal("SonicHabilidad.txt"));
        
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasSonic.findRegion("SE" + i));
        }
        idleAnimation = new Animation<>(0.08f, idleFrames); // Frame time reducido
        
        // Animación correr
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            runFrames.add(atlasSonic.findRegion("SR" + i));
        }
        runAnimation = new Animation<>(0.07f, runFrames); // Frame time reducido
        
        // Animación de bolita (roll)
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 5; i < 9; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) ballFrames.add(region);
        }
        rollAnimation = new Animation<>(0.03f, ballFrames); // Frame time reducido
        
        // Animación saltar
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            TextureRegion region = atlasSonic.findRegion("SJ" + i);
            if (region != null) jumpFrames.add(region);
        }
        jumpAnimation = new Animation<>(0.2f, jumpFrames);
        
        // Animación Spin Dash
        Array<TextureRegion> spinDashFrames = new Array<>();
        for (int i = 0; i < 7; i++) {
            TextureRegion region = atlasSonicH.findRegion("SH" + i);
            if (region != null) spinDashFrames.add(region);
        }
        spinDashAnimation = new Animation<>(0.07f, spinDashFrames); // Frame time reducido
    }
    
    public void handleInput() {
        boolean isMoving = false;
        
        // Movimiento horizontal
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += 500 * Gdx.graphics.getDeltaTime();
            facingRight = true;
            isMoving = true;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= 500 * Gdx.graphics.getDeltaTime();
            facingRight = false;
            isMoving = true;
        }
        
        // Roll (bolita)
        isRolling = Gdx.input.isKeyPressed(Input.Keys.S);
        
        // Salto
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isGrounded) {
            velocidadY = fuerzaSalto;
            isGrounded = false;
            setCurrentAnimation(jumpAnimation);
        }
        
        // Habilidad
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            usarHabilidad();
        }
      
        // Transiciones de animación SOLO si no está en habilidad
        if (!enHabilidad) {
            if (isRolling && isGrounded) {
                setCurrentAnimation(rollAnimation);
            }
            else if (isMoving && isGrounded) {
                setCurrentAnimation(runAnimation);
            }
            else if (isGrounded) {
                setCurrentAnimation(idleAnimation);
            }
        }
    }
    
    public void dispose() {
        if (atlasSonic != null) {
            atlasSonic.dispose();
        }
    }
}