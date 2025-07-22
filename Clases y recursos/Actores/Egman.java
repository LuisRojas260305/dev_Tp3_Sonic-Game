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

public class Egman extends Personajes {
    private TextureAtlas atlasEgman;
    public Animation<TextureRegion> IdleEgman;

    public Egman() {
        cargarAtlas();
        setCurrentAnimation(IdleEgman);
        setPosition(400, 100);
    }

    public void cargarAtlas() {
        atlasEgman = new TextureAtlas(Gdx.files.internal("Egman.txt"));
        
        // Animación idle
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 4; i++) {
            TextureRegion region = atlasEgman.findRegion("EgE" + i);
            if (region != null) {
                idleFrames.add(region);
            } else {
                Gdx.app.error("Egman", "No se encontró región EgE" + i);
            }
        }
        
        if (idleFrames.size > 0) {
            IdleEgman = new Animation<>(0.08f, idleFrames, Animation.PlayMode.LOOP);
        } else {
            Gdx.app.error("Egman", "No se cargaron frames para la animación");
            // Crear animación vacía para evitar NullPointerException
            IdleEgman = new Animation<>(0.1f, new TextureRegion());
        }
    }
    
    @Override
    public void update(float delta) {
        // Actualizar el tiempo de estado para la animación
        stateTime += delta;
    }
    
    @Override
    public void usarHabilidad() {
        // No se necesita para un enemigo estático
    }
}