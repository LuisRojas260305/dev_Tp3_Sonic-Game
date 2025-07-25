package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.miestudio.jsonic.Util.CollisionManager;

public class Robot {
    public float x;
    public float y;
    public boolean facingRight;
    private float speed;
    private float distanceTraveled = 0;
    private static final float MAX_DISTANCE = 500f;
    private TextureRegion texture;
    private boolean active = true;

    public Robot(float startX, float startY, boolean facingRight, float speed, TextureRegion texture) {
        this.x = startX;
        this.y = startY;
        this.facingRight = facingRight;
        this.speed = speed;
        this.texture = texture;
    }

    public void update(float delta, CollisionManager collisionManager) {
        if (!active) return;
        
        float moveAmount = speed * delta;
        x += facingRight ? moveAmount : -moveAmount;
        distanceTraveled += moveAmount;
        
        Rectangle bounds = new Rectangle(x, y, texture.getRegionWidth(), texture.getRegionHeight());
        if (collisionManager.collides(bounds) || distanceTraveled >= MAX_DISTANCE) {
            active = false;
        }
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