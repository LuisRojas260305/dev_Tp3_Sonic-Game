package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Util.CollisionManager;

/**
 * Representa al personaje Knockles en el juego, extendiendo las funcionalidades base de Personajes.
 * Incluye animaciones específicas y la lógica para su habilidad especial de puñetazo cargado.
 */
public class Knockles extends Personajes{
    private TextureAtlas atlasKnockles;

    public Knockles(int playerId, TextureAtlas atlas){
        this.playerId = playerId;
        this.atlasKnockles = atlas;
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(10, 20);

    }

    @Override
    public void update(float delta, CollisionManager collisionManager) {
        super.update(delta, collisionManager);
    }

    private void cargarAnimaciones() {
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i < 6; i++) {
            idleFrames.add(atlasKnockles.findRegion("KE" + i));
        }
        idleAnimation = new Animation<>(0.18f, idleFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i < 8; i++) {
            runFrames.add(atlasKnockles.findRegion("KR" + i));
        }

        runAnimation = new Animation<>(0.08f, runFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> ballFrames = new Array<>();
        for (int i = 0; i < 4; i++){
            ballFrames.add(atlasKnockles.findRegion("KB" + i));
        }

        rollAnimation = new Animation<>(0.1f, ballFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> jumpFrames = new Array<>();
        jumpFrames.add(atlasKnockles.findRegion("KJ1"));
        jumpFrames.add(atlasKnockles.findRegion("KJ2"));
        jumpFrames.add(atlasKnockles.findRegion("KJ0"));
        jumpFrames.add(atlasKnockles.findRegion("KJ3"));

        jumpAnimation = new Animation<>(0.2f, jumpFrames, Animation.PlayMode.NORMAL);
    }

    @Override
    public void dispose() {
        // El atlas se gestiona en la clase Assets
    }
}
