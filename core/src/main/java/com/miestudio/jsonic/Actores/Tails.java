package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Representa al personaje Tails en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas para Tails.
 */
public class Tails extends Personajes{
    private TextureAtlas AtlasTails;
    
    public Tails(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.AtlasTails = atlas;
        cargarAnimaciones();
        currentAnimation = idleAnimation;
    }
    
    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            idleFrames.add(AtlasTails.findRegion("TailsIdle" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 0; i < 9; i++) {
            runFrames.add(AtlasTails.findRegion("TailsRun" + i));
        }
        
        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 2; i++){
            ballFrames.add(AtlasTails.findRegion("TailsHit" + i));
        }
        
        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);
        
        Array<TextureRegion> jumpFrames = new Array<>();
        for (int i = 0; i < 4; i++){
            jumpFrames.add(AtlasTails.findRegion("TailsJump" + i));
        }
        
        jumpAnimation = new Animation<>(0.25f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }

    @Override
    public void useAbility() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}