/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Tails extends Personajes{
    private TextureAtlas atlasTails;
    private TextureAtlas atlasTailsF;
    public Animation<TextureRegion> skillAnimation;
    public boolean isFlying = false;
    private final float flySpeed = 100f;
    public Animation<TextureRegion> flyAnimation;
    
    public Robot robot;
    private float robotDelay = 0;
    public boolean shouldCreateRobot = false;
    private boolean skillAnimationPlaying = false;
    
    public Tails(){
        cargarAtlas();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
    }
    
    @Override
    public void update(float delta) {
        stateTime += delta;
        
        // Actualizar robot si existe
        if (robot != null) {
            robot.update(delta);
            if (!robot.isActive()) {
                robot.dispose();
                robot = null;
            }
        }
        
        // Temporizador para crear robot
        if (shouldCreateRobot) {
            robotDelay -= delta;
            if (robotDelay <= 0) {
                robot = new Robot(x + (facingRight ? 50 : -50), y + 30, facingRight);
                shouldCreateRobot = false;
                skillAnimationPlaying = false; // Finalizar animación de habilidad
            }
        }
        
        // Forzar animación de habilidad si está activa
        if (enHabilidad && !skillAnimationPlaying) {
            setCurrentAnimation(skillAnimation);
            skillAnimationPlaying = true;
        }
        
        // Finalizar animación de habilidad si ha terminado
        if (skillAnimationPlaying && skillAnimation.isAnimationFinished(stateTime)) {
            enHabilidad = false;
            skillAnimationPlaying = false;
        }
        
        if (isFlying) {
            // Movimiento horizontal
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                x += 300 * Gdx.graphics.getDeltaTime();
                facingRight = true;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                x -= 300 * Gdx.graphics.getDeltaTime();
                facingRight = false;
            }
            
            // Elevación
            y += flySpeed * delta;
            
            // Forzar la animación de vuelo
            if (currentAnimation != flyAnimation) {
                setCurrentAnimation(flyAnimation);
            }
            
            // Si se suelta W, dejar de volar
            if (!Gdx.input.isKeyPressed(Input.Keys.W)) {
                isFlying = false;
                enHabilidad = false;
                velocidadY = 0;
                setCurrentAnimation(jumpAnimation);
            }
        } else {
            // Comportamiento normal
            super.update(delta);
        }
    }
    
    public void handleInput() {
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
        
        // Agregar esto después del manejo de movimiento/salto
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !enHabilidad && !isFlying) {
            usarHabilidad();
        }
        
        // Salto y vuelo: solo si no está en habilidad
        if (!enHabilidad) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) && isGrounded) {
                velocidadY = fuerzaSalto;
                isGrounded = false;
                setCurrentAnimation(jumpAnimation);
            } else if (!isGrounded && Gdx.input.isKeyPressed(Input.Keys.W)) {
                if (!isFlying) {
                    isFlying = true;
                    enHabilidad = true;
                    setCurrentAnimation(flyAnimation);
                }
            }
        }
      
        // Transiciones de animación
        if (enHabilidad && skillAnimationPlaying) {
            // Mantener animación de habilidad
            setCurrentAnimation(skillAnimation);
        } else if (isFlying) {
            setCurrentAnimation(flyAnimation);
        } else if (isRolling && isGrounded) {
            setCurrentAnimation(rollAnimation);
        } else if (isMoving && isGrounded) {
            setCurrentAnimation(runAnimation);
        } else if (isGrounded) {
            setCurrentAnimation(idleAnimation);
        }
    }
    
    
    private void cargarAtlas() {
        atlasTails = new TextureAtlas(Gdx.files.internal("TailsAtlas.txt"));
        atlasTailsF = new TextureAtlas(Gdx.files.internal("TailsF.txt"));
        
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 1; i < 9; i++) {
            idleFrames.add(atlasTails.findRegion("TE (" + i + ")"));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i < 8; i++) {
            runFrames.add(atlasTails.findRegion("TR (" + i + ")"));
        }
        runAnimation = new Animation<>(0.15f, runFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 1; i < 9; i++){
            ballFrames.add(atlasTails.findRegion("TB (" + i + ")"));
        }
        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> jumpFrames = new Array<>();
        
        jumpFrames.add(atlasTails.findRegion("TJ (5)"));
        jumpAnimation = new Animation<>(0.25f, jumpFrames, Animation.PlayMode.NORMAL);
        
        // Animación de vuelo
        Array<TextureRegion> flyFrames = new Array<>();
        flyFrames.add(atlasTailsF.findRegion("TF0"));
        flyFrames.add(atlasTailsF.findRegion("TF1"));
        flyFrames.add(atlasTailsF.findRegion("TF2"));
        flyFrames.add(atlasTailsF.findRegion("TF3"));
        flyFrames.add(atlasTailsF.findRegion("TF4"));
        flyFrames.add(atlasTailsF.findRegion("TF5"));
        flyFrames.add(atlasTailsF.findRegion("TF6"));
        flyFrames.add(atlasTailsF.findRegion("TF7"));
        flyFrames.add(atlasTailsF.findRegion("TF8"));
        flyFrames.add(atlasTailsF.findRegion("TF9"));
        flyAnimation = new Animation<>(0.08f, flyFrames, Animation.PlayMode.LOOP);
        
        // Animación de habilidad - asegurar que las regiones existen
        Array<TextureRegion> skillFrames = new Array<>();
        for (int i = 1; i <= 6; i++) {
            TextureRegion region = atlasTails.findRegion("TA (" + i + ")");
            if (region != null) {
                skillFrames.add(region);
            } else {
                Gdx.app.error("Tails", "No se encontró región TA (" + i + ")");
            }
        }
        
        if (skillFrames.size > 0) {
            skillAnimation = new Animation<>(0.15f, skillFrames, Animation.PlayMode.NORMAL);
        } else {
            Gdx.app.error("Tails", "No se cargaron frames para skillAnimation");
            // Crear animación vacía para evitar NullPointerException
            skillAnimation = new Animation<>(0.1f, new TextureRegion());
        }
    }

    public void dispose() {
        if (atlasTails != null) {
            atlasTails.dispose();
            atlasTailsF.dispose();
        }
        if (robot != null) {
            robot.dispose();
            robot = null;
        }
    }
    
    public void renderRobot(SpriteBatch batch) {
        if (robot != null) {
            robot.render(batch);
        }
    }

    @Override
    public void usarHabilidad() {
        // Solo activar si no hay un robot activo y está en el suelo
        if (isGrounded && !enHabilidad && robot == null) {
            enHabilidad = true;
            shouldCreateRobot = true;
            robotDelay = 0.5f; // 0.5 segundos de retraso
            
            // Iniciar animación inmediatamente
            setCurrentAnimation(skillAnimation);
            stateTime = 0f; // Reiniciar tiempo de animación
            skillAnimationPlaying = true;
            
            Gdx.app.log("Tails", "Habilidad activada");
        }
    }

}

