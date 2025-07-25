package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.miestudio.jsonic.Objetos.Objetos;
import com.miestudio.jsonic.Objetos.MaquinaReciclaje;
import com.miestudio.jsonic.Server.GameServer;
import com.miestudio.jsonic.Util.CollisionManager;

public class Robot {

    public enum RobotState {
        IDLE,
        MOVING_TO_TRASH,
        COLLECTING_TRASH,
        MOVING_TO_MACHINE,
        DELIVERING_TRASH,
        SELF_DESTRUCTING
    }

    public float x;
    public float y;
    public boolean facingRight;
    private float speed;
    private TextureRegion texture;
    private boolean active = true;

    private RobotState currentState = RobotState.IDLE;
    private Objetos targetTrash; // El objeto de basura a recolectar
    private MaquinaReciclaje targetRecyclingMachine; // La máquina a la que entregar
    private GameServer gameServer; // Referencia al GameServer para interactuar con el juego
    private float collectionTimer = 0; // Temporizador para la recolección de basura
    private static final float MAX_COLLECTION_TIME = 10f; // Tiempo máximo de recolección
    private int collectedTrashAmount = 0; // Cantidad de basura recolectada por el robot
    private static final int MAX_TRASH_CAPACITY = 20; // Capacidad máxima de basura del robot

    public Robot(float startX, float startY, boolean facingRight, float speed, TextureRegion texture, GameServer gameServer, Objetos targetTrash, MaquinaReciclaje targetRecyclingMachine) {
        this.x = startX;
        this.y = startY;
        this.facingRight = facingRight;
        this.speed = speed;
        this.texture = texture;
        this.gameServer = gameServer;
        this.targetTrash = targetTrash;
        this.targetRecyclingMachine = targetRecyclingMachine;
        this.currentState = RobotState.MOVING_TO_TRASH; // Estado inicial
    }

    public void update(float delta, CollisionManager collisionManager) {
        if (!active) return;

        switch (currentState) {
            case MOVING_TO_TRASH:
                if (targetTrash != null && targetTrash.estaActivo()) {
                    moveToTarget(targetTrash.x, targetTrash.y, delta);
                    if (new Rectangle(x, y, texture.getRegionWidth(), texture.getRegionHeight()).overlaps(targetTrash.getHitbox())) {
                        currentState = RobotState.COLLECTING_TRASH;
                        collectionTimer = 0;
                    }
                } else {
                    // Basura no encontrada o inactiva, buscar nueva o autodestruirse
                    targetTrash = gameServer.getNearestTrash(new Vector2(x, y));
                    if (targetTrash == null) {
                        selfDestruct();
                    }
                }
                break;
            case COLLECTING_TRASH:
                collectionTimer += delta;
                if (collectionTimer >= MAX_COLLECTION_TIME || collectedTrashAmount >= MAX_TRASH_CAPACITY) {
                    // Terminar recolección, pasar a entregar
                    if (collectedTrashAmount > 0) {
                        currentState = RobotState.MOVING_TO_MACHINE;
                    } else {
                        selfDestruct(); // No recolectó nada, autodestruirse
                    }
                } else {
                    // Simular recolección de basura
                    if (targetTrash != null && targetTrash.estaActivo()) {
                        collectedTrashAmount++; // Incrementar basura recolectada
                        targetTrash.setActivo(false); // Desactivar basura del mapa
                        gameServer.removeGameObject(targetTrash.getId()); // Eliminar del servidor
                        targetTrash = null; // Ya no hay basura objetivo
                        // Buscar más basura si aún no se llena la capacidad
                        if (collectedTrashAmount < MAX_TRASH_CAPACITY) {
                            targetTrash = gameServer.getNearestTrash(new Vector2(x, y));
                            if (targetTrash == null) {
                                // No hay más basura, ir a la máquina
                                currentState = RobotState.MOVING_TO_MACHINE;
                            }
                        } else {
                            currentState = RobotState.MOVING_TO_MACHINE;
                        }
                    } else {
                        // Basura objetivo desapareció, buscar nueva o ir a la máquina
                        targetTrash = gameServer.getNearestTrash(new Vector2(x, y));
                        if (targetTrash == null) {
                            currentState = RobotState.MOVING_TO_MACHINE;
                        }
                    }
                }
                break;
            case MOVING_TO_MACHINE:
                if (targetRecyclingMachine != null) {
                    moveToTarget(targetRecyclingMachine.x, targetRecyclingMachine.y, delta);
                    if (new Rectangle(x, y, texture.getRegionWidth(), texture.getRegionHeight()).overlaps(targetRecyclingMachine.getHitbox())) {
                        currentState = RobotState.DELIVERING_TRASH;
                    }
                } else {
                    selfDestruct(); // No hay máquina, autodestruirse
                }
                break;
            case DELIVERING_TRASH:
                if (collectedTrashAmount > 0 && targetRecyclingMachine != null) {
                    gameServer.addTrashToMachine(targetRecyclingMachine, collectedTrashAmount);
                    collectedTrashAmount = 0;
                }
                selfDestruct();
                break;
            case SELF_DESTRUCTING:
                selfDestruct();
                break;
            case IDLE:
            default:
                // No hacer nada o esperar instrucciones
                break;
        }
    }

    private void moveToTarget(float targetX, float targetY, float delta) {
        Vector2 direction = new Vector2(targetX - x, targetY - y).nor();
        x += direction.x * speed * delta;
        y += direction.y * speed * delta;

        if (direction.x > 0) facingRight = true;
        else if (direction.x < 0) facingRight = false;
    }

    private void selfDestruct() {
        active = false;
        // Lógica de efectos de autodestrucción si es necesario
    }
    
    public boolean isActive() {
        return active;
    }
    
    public TextureRegion getTexture() {
        return texture;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isFacingRight() { return facingRight; }
}