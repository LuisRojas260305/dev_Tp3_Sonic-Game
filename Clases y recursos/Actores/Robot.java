/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Robot {
    private float x, y;
    private float stateTime;
    private Animation<TextureRegion> animation;
    private TextureAtlas robotAtlas;
    private boolean active;
    private float speed = 100f; // Aumentar velocidad
    private boolean movingRight;

    public Robot(float startX, float startY, boolean movingRight) {
        this.x = startX;
        this.y = startY;
        this.movingRight = movingRight;
        this.active = true;
        stateTime = 0f;
        loadAnimation();
        Gdx.app.log("Robot", "Creado en (" + x + ", " + y + ")");
    }

    private void loadAnimation() {
        try {
            robotAtlas = new TextureAtlas(Gdx.files.internal("Robot.txt"));
            
            Array<TextureRegion> frames = new Array<>();
            for (int i = 0; i < 4; i++) {
                TextureRegion region = robotAtlas.findRegion("RBI" + i);
                if (region != null) {
                    frames.add(region);
                } else {
                    Gdx.app.error("Robot", "No se encontró región RBI" + i);
                }
            }
            
            if (frames.size > 0) {
                animation = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
            } else {
                Gdx.app.error("Robot", "No se cargaron frames para la animación");
            }
        } catch (Exception e) {
            Gdx.app.error("Robot", "Error cargando atlas: " + e.getMessage());
        }
    }

    public void update(float delta) {
        if (!active) return;
        
        stateTime += delta;
        x += (movingRight ? speed : -speed) * delta;
        
        // Desactivar si sale de pantalla
        if (x < -100 || x > Gdx.graphics.getWidth() + 100) {
            active = false;
            Gdx.app.log("Robot", "Desactivado");
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        
        TextureRegion frame = animation.getKeyFrame(stateTime, true);
        if (frame != null) {
            float drawX = movingRight ? x : x + frame.getRegionWidth();
            float width = movingRight ? frame.getRegionWidth() : -frame.getRegionWidth();
            
            batch.draw(frame, drawX, 100, 20, 20);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void dispose() {
        if (robotAtlas != null) {
            robotAtlas.dispose();
        }
    }
}
