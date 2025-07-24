package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Server.domain.InputState;
import com.miestudio.jsonic.Util.CollisionManager;

public class Tails extends Personajes {
    private TextureAtlas AtlasTails;
    private Animation<TextureRegion> flyAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> robotAnimation;
    
    // Sistema de vuelo
    private boolean isFlying = false;
    private float flySpeed = 150f; // Velocidad de ascenso
    private float flyTime = 0f;
    private static final float MAX_FLY_TIME = 5f; // Tiempo máximo de vuelo
    
    // Sistema de robot
    private Array<Robot> activeRobots = new Array<>(); // Lista de robots activos
    private float robotCooldown = 0f;
    private static final float ROBOT_COOLDOWN_TIME = 3f;
    
    private float abilityActiveTimer = 0f;
    private static final float ABILITY_DURATION = 0.5f; // 0.5 segundos dura la animación de crear robot
    
    public Tails(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        this.AtlasTails = atlas;
        cargarAnimaciones();
        currentAnimation = idleAnimation;
        setAbilityAnimation(robotAnimation);
    }
    
    private void cargarAnimaciones() {
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            idleFrames.add(AtlasTails.findRegion("TailsIdle" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);
        
        // Animación correr
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            runFrames.add(AtlasTails.findRegion("TailsRun" + i));
        }
        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);
        
        // Animación rodar (bolita)
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 2; i++){
            ballFrames.add(AtlasTails.findRegion("TailsHit" + i));
        }
        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);
        
        // Animación salto
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 4; i++){
            jumpFrames.add(AtlasTails.findRegion("TailsJump" + i));
        }
        jumpAnimation = new Animation<>(0.25f, jumpFrames, Animation.PlayMode.NORMAL);
        
        // Nueva animación de vuelo
        Array<TextureRegion> flyFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            flyFrames.add(AtlasTails.findRegion("TailsFly" + i));
        }
        flyAnimation = new Animation<>(0.1f, flyFrames, Animation.PlayMode.LOOP);
        
        // Nueva animación de caída
        Array<TextureRegion> fallFrames = new Array<>();
        for (int i = 2; i < 4; i++) {
            fallFrames.add(AtlasTails.findRegion("TailsJump" + i));
        }
        fallAnimation = new Animation<>(0.15f, fallFrames, Animation.PlayMode.LOOP);
        
        // Animación para el robot (usada en la habilidad)
        Array<TextureRegion> robotFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            robotFrames.add(AtlasTails.findRegion("RobotTailsIdleRun" + i));
        }
        robotAnimation = new Animation<>(0.1f, robotFrames, Animation.PlayMode.LOOP);
    }

    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);
        
        // Actualizar cooldown del robot
        if (robotCooldown > 0) {
            robotCooldown -= delta;
        }
        
        // Gestionar el temporizador de la habilidad activa
        if (abilityActiveTimer > 0) {
            abilityActiveTimer -= delta;
            if (abilityActiveTimer <= 0) {
                isAbilityActive = false;
            }
        }
        
        // Actualizar tiempo de vuelo y aplicar movimiento
        if (isFlying) {
            flyTime += delta;
            velocityY = flySpeed; // Aplicar velocidad de ascenso constante.
            if (flyTime >= MAX_FLY_TIME) {
                stopFlying();
            }
            
            // Asegurar que la animación de vuelo se mantenga
            if (currentAnimation != flyAnimation) {
                setCurrentAnimation(flyAnimation);
            }
        }
        
        // Actualizar robots activos
        for (Robot robot : activeRobots) {
            robot.update(delta, collisionManager);
        }
        
        // Eliminar robots inactivos
        for (int i = activeRobots.size - 1; i >= 0; i--) {
            if (!activeRobots.get(i).isActive()) {
                activeRobots.removeIndex(i);
            }
        }
        
        // Si está en el aire y no volando, mostrar animación de caída
        if (!isGrounded && !isFlying && !isAbilityActive) {
            setCurrentAnimation(fallAnimation);
        }
    }

    @Override
    public void handleInput(InputState input, CollisionManager collisionManager, float delta) {
        boolean isMoving = false;

        // Movimiento horizontal (común en tierra y aire)
        if (input.isRight()) {
            float nextX = x + moveSpeed * delta;
            Rectangle horizontalCheck = new Rectangle(nextX, y, getWidth(), getHeight());
            if (!collisionManager.collides(horizontalCheck)) {
                x = nextX;
                facingRight = true;
            }
            isMoving = true;
        }

        if (input.isLeft()) {
            float nextX = x - moveSpeed * delta;
            Rectangle horizontalCheck = new Rectangle(nextX, y, getWidth(), getHeight());
            if (!collisionManager.collides(horizontalCheck)) {
                x = nextX;
                facingRight = false;
            }
            isMoving = true;
        }
        
        // Lógica de salto y vuelo
        if (input.isUp()) {
            if (isGrounded) { // Salto normal si está en el suelo
                velocityY = jumpForce;
                isGrounded = false;
            } else if (!isGrounded && !isAbilityActive) { // Empezar a volar si está en el aire
                startFlying();
            }
        } else if (isFlying) {
            // Detener vuelo si se suelta la tecla
            stopFlying();
        }
        
        // Habilidad: crear robot (solo si no está activa y el cooldown ha terminado)
        if (input.isAbility() && !isAbilityActive && robotCooldown <= 0) {
            useAbility();
        }
        
        // Limitar posición dentro del mapa
        x = Math.max(0, Math.min(x, collisionManager.getMapWidth() - getWidth()));
        y = Math.max(0, Math.min(y, collisionManager.getMapHeight() - getHeight()));

        isRolling = input.isDown();
        Rectangle characterBounds = new Rectangle(x, y, getWidth(), getHeight());
        isGrounded = collisionManager.isOnGround(characterBounds);

        // Prioridad de animaciones
        if (isAbilityActive) {
            setCurrentAnimation(abilityAnimation);
        } else if (isFlying) {
            setCurrentAnimation(flyAnimation);
        } else if (!isGrounded) {
            if (velocityY > 0) {
                setCurrentAnimation(jumpAnimation);
            } else {
                setCurrentAnimation(fallAnimation);
            }
        } else if (isRolling) {
            setCurrentAnimation(rollAnimation);
        } else if (isMoving) {
            setCurrentAnimation(runAnimation);
        } else {
            setCurrentAnimation(idleAnimation);
        }
    }

    @Override
    public void useAbility() {
        if (!isAbilityActive && robotCooldown <= 0) {
            isAbilityActive = true;
            stateTime = 0f;
            abilityActiveTimer = ABILITY_DURATION;
            
            // Crear nuevo robot independiente
            TextureRegion robotTexture = robotAnimation.getKeyFrame(0);
            Robot newRobot = new Robot(
                this.x + (facingRight ? 30 : -30), // Aparece al lado de Tails
                this.y,
                this.facingRight,
                this.moveSpeed * 1.5f,
                robotTexture
            );
            
            activeRobots.add(newRobot);
            robotCooldown = ROBOT_COOLDOWN_TIME;
        }
    }
    
    public void startFlying() {
        if (!isFlying && flyTime < MAX_FLY_TIME) {
            isFlying = true;
            setCurrentAnimation(flyAnimation);
        }
    }
    
    public void stopFlying() {
        if (isFlying) {
            isFlying = false;
        }
    }
    
    public void resetFlyTime() {
        flyTime = 0f;
    }
    
    public boolean isFlying() {
        return isFlying;
    }
    
    public float getRobotCooldown() {
        return robotCooldown;
    }
    
    public Array<Robot> getActiveRobots() {
        return activeRobots;
    }

    @Override
    public String getCurrentAnimationName() {
        if (isFlying) {
            return "FLY";
        }
        return super.getCurrentAnimationName();
    }
    
    @Override
    public void setAnimation(AnimationType animationType) {
        if (animationType == AnimationType.FLY) {
            setCurrentAnimation(flyAnimation);
        } else {
            super.setAnimation(animationType);
        }
    }

    @Override
    public void dispose() {
        activeRobots.clear();
    }
    
    
}