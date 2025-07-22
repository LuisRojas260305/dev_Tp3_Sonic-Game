package com.miestudio.jsonic.Abilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Util.InputState;

public class FlyAbility {
    private Animation<TextureRegion> flyAnimation;
    private boolean active = false;
    private TextureAtlas atlasTails;

    public FlyAbility(TextureAtlas atlas) {
        this.atlasTails = atlas;
        cargarAnimaciones();
    }

    private void cargarAnimaciones() {
        Array<TextureRegion> flyFrames = new Array<>();
        // Asumiendo que tienes frames para volar en el atlas de Tails
        // Por ejemplo: atlasTails.findRegion("TF" + i)
        // Por ahora, usaré una animación de salto o una genérica si no hay una específica.
        // Deberías reemplazar esto con los frames correctos de vuelo de Tails.
        for (int i = 1; i < 5; i++){
            flyFrames.add(atlasTails.findRegion("TJ (" + i + ")")); // Usando frames de salto como placeholder
        }
        flyAnimation = new Animation<>(0.1f, flyFrames, Animation.PlayMode.LOOP);
    }

    public void handleFlight(Personajes player, InputState input, float delta, CollisionManager collisionManager) {
        if (input.isUp() && !player.isGrounded()) { // Activate flight if 'up' is pressed and not grounded
            if (!active) {
                active = true;
                player.setCanJump(false); // Tails cannot jump while flying
                player.setVelocityY(0); // Reset vertical velocity to start flying smoothly
                player.setCurrentAnimation(flyAnimation);
            }
            // Lógica de vuelo: mantener a Tails en el aire y permitir movimiento vertical
            player.setPlayerY(player.getPlayerY() + player.getMoveSpeed() * delta); // Ascender

            if (input.isDown()) { // Assuming 'down' key for descending
                player.setPlayerY(player.getPlayerY() - player.getMoveSpeed() * delta); // Descender
            }
        } else { // Deactivate flight if 'up' is released or player is grounded
            if (active) {
                active = false;
                player.setCanJump(true); // Tails can jump again
                if (player.isGrounded()) {
                    player.setCurrentAnimation(player.getIdleAnimation());
                } else {
                    // If flight stops in mid-air, transition to jump animation
                    player.setCurrentAnimation(player.getJumpAnimation());
                }
            }
        }
    }

    public boolean isActive() {
        return active;
    }
}
