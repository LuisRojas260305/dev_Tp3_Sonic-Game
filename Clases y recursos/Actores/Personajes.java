/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 *
 * @author usuario
 */
public abstract class Personajes extends Actor {

    public float stateTime;
    protected float x, y, velocidadY = 0;
    protected boolean facingRight = true, isGrounded = true;
    public Animation<TextureRegion> currentAnimation;
    public Animation<TextureRegion> idleAnimation;
    public Animation<TextureRegion> runAnimation;
    public Animation<TextureRegion> jumpAnimation;
    public Animation<TextureRegion> rollAnimation; 
    
    private final float gravedad = -800f;
    final float fuerzaSalto = 500f;
    private final float suelo = 100;
    public boolean isRolling = false;
    public boolean enHabilidad = false; // Control clave para habilidades

    public abstract void usarHabilidad();
    
    public void update(float delta) {
        stateTime += delta; // Siempre actualizar stateTime
        //updatePhysics(delta);
        
        
        
        if (!(this instanceof Tails && ((Tails)this).isFlying)) {
            updatePhysics(delta);
        }
        
        if (!enHabilidad) {
            handleInput(); // Solo manejar input si no está en habilidad
        }
    }
    
    private void updatePhysics(float delta) {
        velocidadY += gravedad * delta;
        y += velocidadY * delta;
        
        
        
        if (y <= suelo) {
            y = suelo;
            velocidadY = 0;
            isGrounded = true;
        }
    }

    
    protected void handleInput() {
        boolean isMoving = false;
        
        // Movimiento horizontal
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += 300 * Gdx.graphics.getDeltaTime();
            facingRight = true;
            isMoving = true;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= 300 * Gdx.graphics.getDeltaTime();
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
    
    // Método protegido para cambiar animaciones
    protected void setCurrentAnimation(Animation<TextureRegion> newAnimation) {
        if (currentAnimation != newAnimation) {
            currentAnimation = newAnimation;
            stateTime = 0f; // Solo reiniciar tiempo al cambiar
        }
    }
    
    // Getters y setters
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isFacingRight() { return facingRight; }
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public TextureRegion getCurrentFrame() { 
        return currentAnimation.getKeyFrame(stateTime, true); 
    }
}