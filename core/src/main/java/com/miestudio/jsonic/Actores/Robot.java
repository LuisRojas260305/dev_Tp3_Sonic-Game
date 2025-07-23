package com.miestudio.jsonic.Actores;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Robot extends Personajes {

    public Robot(int playerId, TextureAtlas atlas) {
        this.playerId = playerId;
        // Asumiendo que el atlas tiene regiones para el robot
        // Por ahora, usaré animaciones genéricas o de placeholder
        cargarAnimaciones();
        setCurrentAnimation(idleAnimation);
        setPosition(0, 0);
    }

    private void cargarAnimaciones() {
        // Placeholder animations for Robot
        Array<TextureRegion> idleFrames = new Array<>();
        // Add robot specific frames here
        // For now, using a dummy frame
        idleFrames.add(new TextureRegion()); // Replace with actual robot texture
        idleAnimation = new Animation<>(0.1f, idleFrames, Animation.PlayMode.LOOP);

        runAnimation = idleAnimation; // Placeholder
        jumpAnimation = idleAnimation; // Placeholder
        rollAnimation = idleAnimation; // Placeholder
    }

    @Override
    public void dispose() {
        // Dispose resources if any
    }

    
}