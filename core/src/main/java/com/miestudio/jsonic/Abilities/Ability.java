package com.miestudio.jsonic.Abilities;

import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Util.CollisionManager;

public interface Ability {
    void activate(Personajes player);
    void update(Personajes player, float delta, CollisionManager collisionManager);
    void deactivate(Personajes player);
    boolean isActive();
}
