package com.miestudio.jsonic.Abilities;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.miestudio.jsonic.Actores.Personajes;
import com.miestudio.jsonic.Actores.Robot;
import com.miestudio.jsonic.Util.CollisionManager;

public class RobotSummonAbility implements Ability {
    private boolean active = false;
    private TextureAtlas robotAtlas; // Atlas para el robot

    public RobotSummonAbility(TextureAtlas atlas) {
        this.robotAtlas = atlas;
    }

    @Override
    public void activate(Personajes player) {
        if (!active) {
            active = true;
            // Lógica para invocar al robot
            // Por ejemplo, crear una instancia de Robot y añadirla al juego
            // Esto requerirá que GameScreen o un gestor de entidades tenga un método para añadir actores
            System.out.println("Robot summoned by " + player.getClass().getSimpleName());
            // Ejemplo: new Robot(player.getPlayerId(), robotAtlas);
            // Necesitarás pasar el robot a la pantalla de juego para que se renderice y actualice.
            // Por ahora, solo imprimirá un mensaje.
        }
    }

    @Override
    public void update(Personajes player, float delta, CollisionManager collisionManager) {
        // La habilidad de invocar un robot es instantánea, no necesita actualización continua
        if (active) {
            deactivate(player);
        }
    }

    @Override
    public void deactivate(Personajes player) {
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
