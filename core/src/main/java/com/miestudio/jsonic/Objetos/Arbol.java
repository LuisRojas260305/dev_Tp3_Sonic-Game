package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.miestudio.jsonic.Util.Assets;

public class Arbol extends Objetos {

    public Arbol(float x, float y, TextureRegion texture) {
        super(x, y, texture);
        // El hitbox ya se inicializa con el tamaño de la textura en Objetos.java
    }

    @Override
    public void actualizar(float delta) {
        // Los árboles no tienen lógica de actualización compleja
    }
}
