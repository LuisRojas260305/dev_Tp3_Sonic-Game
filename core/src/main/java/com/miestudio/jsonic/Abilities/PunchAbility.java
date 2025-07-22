package com.miestudio.jsonic.Abilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Util.CollisionManager;

public class PunchAbility implements Ability {
    private Animation<TextureRegion> punchAnimation;
    private boolean active = false;
    private float punchPower = 0;
    private final float MAX_PUNCH_POWER = 500f;
    private TextureAtlas atlasKnockles;

    public PunchAbility(TextureAtlas atlas) {
        this.atlasKnockles = atlas;
        cargarAnimaciones();
    }

    private void cargarAnimaciones() {
        Array<TextureRegion> PunchFrames = new Array<>();
        for (int i = 1; i < 6; i++){
            PunchFrames.add(atlasKnockles.findRegion("KG" + i));
        }
        punchAnimation = new Animation<>(0.17f, PunchFrames, Animation.PlayMode.LOOP);
    }

    @Override
    public void activate(Personajes player) {
        if (player.isGrounded() && !active) {
            active = true;
            punchPower = 0;
            player.setCurrentAnimation(punchAnimation);
        }
    }

    @Override
    public void update(Personajes player, float delta, CollisionManager collisionManager) {
        if (active) {
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                punchPower = Math.min(punchPower + 100 * delta, MAX_PUNCH_POWER);
            } else {
                float impulso = punchPower * delta;
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
