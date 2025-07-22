/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 *
 * @author usuario
 */
public class ETank extends Personajes{
    private TextureAtlas atlasEnemigo;
    public Animation<TextureRegion> IdleEnemigo;

    public ETank() {
        cargarAtlas();
        setCurrentAnimation(IdleEnemigo);
        setPosition(300, 100);
    }
    
    public void cargarAtlas() {
        atlasEnemigo = new TextureAtlas(Gdx.files.internal("Enemigos.txt"));
        
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 1; i < 5; i++) {
            TextureRegion region = atlasEnemigo.findRegion("ET" + i);
            if (region != null) {
                idleFrames.add(region);
            } else {
                Gdx.app.error("Egman", "No se encontró región EC" + i);
            }
        }
        
        if (idleFrames.size > 0) {
            IdleEnemigo = new Animation<>(0.08f, idleFrames, Animation.PlayMode.LOOP);
        } else {
            Gdx.app.error("Egman", "No se cargaron frames para la animación");
            // Crear animación vacía para evitar NullPointerException
            IdleEnemigo = new Animation<>(0.1f, new TextureRegion());
        }
    }
    
    @Override
    public void update(float delta) {
        // Actualizar el tiempo de estado para la animación
        stateTime += delta;
    }
    
    
    @Override
    public void usarHabilidad() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
