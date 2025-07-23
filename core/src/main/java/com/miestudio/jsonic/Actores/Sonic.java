package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Sonic en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial Spin Dash.
 */
public class Sonic extends Personajes {
    private TextureAtlas atlasSonic;

    public Sonic(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        this.atlasSonic = atlas;
        this.moveSpeed = 400f;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);
    }

    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);
    }

    private void cargarAnimaciones() {
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
        runAnimation = new Animation<>(0.08f, runFrames); // Frame time reducido

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
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
