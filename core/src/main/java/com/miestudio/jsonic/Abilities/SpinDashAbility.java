package com.miestudio.jsonic.Abilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Util.CollisionManager;

public class SpinDashAbility implements Ability {
    private Animation<TextureRegion> spinDashAnimation;
    private boolean active = false;
    private float spinPower = 0;
    private final float MAX_SPIN_POWER = 500f;
    private TextureAtlas atlasSonic;

    public SpinDashAbility(TextureAtlas atlas) {
        this.atlasSonic = atlas;
        cargarAnimaciones();
    }

    private void cargarAnimaciones() {
        Array<TextureRegion> spinDashFrames = new Array<>();
        for (int i = 9; i < 13; i++) {
            TextureRegion region = atlasSonic.findRegion("SB" + i);
            if (region != null) spinDashFrames.add(region);
        }
        spinDashAnimation = new Animation<>(0.04f, spinDashFrames);
    }

    @Override
    public void activate(Personajes player) {
        if (player.isGrounded() && !active) {
            active = true;
            spinPower = 0;
            player.setCurrentAnimation(spinDashAnimation);
        }
    }

    @Override
    public void update(Personajes player, float delta, CollisionManager collisionManager) {
        if (active) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                spinPower = Math.min(spinPower + 100 * delta, MAX_SPIN_POWER);
            } else {
                float impulso = spinPower * delta;
                player.setPlayerX(player.getPlayerX() + (player.isFacingRight() ? impulso : -impulso));
                deactivate(player);
            }
        }
    }

    @Override
    public void deactivate(Personajes player) {
        active = false;
        if (player.isGrounded()) {
            player.setCurrentAnimation(player.isRolling ? player.getRollAnimation() : player.getIdleAnimation());
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
