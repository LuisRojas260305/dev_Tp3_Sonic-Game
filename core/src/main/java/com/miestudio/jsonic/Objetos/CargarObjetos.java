package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;


public class CargarObjetos {
    public Array<Objetos> objetos;
    private TextureAtlas atlasObjetos;
    public Animation<TextureRegion> animacionAnillo;

    public CargarObjetos(TextureAtlas atlasObjetos) {
        objetos = new Array<>();
        this.atlasObjetos = atlasObjetos;
        cargarRecursos();
    }

    private void cargarRecursos() {

        // Cargar animación de anillos
        Array<TextureRegion> framesAnillo = new Array<>();
        for (int i = 0; i < 5; i++) {
            framesAnillo.add(atlasObjetos.findRegion("ObjA" + i));
        }
        animacionAnillo = new Animation<>(0.15f, framesAnillo, Animation.PlayMode.LOOP);
    }

    public void agregarAnillo(float x, float y) {
        objetos.add(new Anillo(x, y, animacionAnillo));
    }

    public void agregarAnillo(Anillo anillo) {
        objetos.add(anillo);
    }

    public void actualizar(float delta) {
        for (Objetos objeto : objetos) {
            objeto.actualizar(delta);
        }

        // Eliminar objetos inactivos
        for (int i = objetos.size - 1; i >= 0; i--) {
            if (!objetos.get(i).estaActivo()) {
                objetos.removeIndex(i);
            }
        }
    }

    public void renderizar(SpriteBatch batch) {
        for (Objetos objeto : objetos) {
            objeto.renderizar(batch);
        }
    }

    public Array<Objetos> getObjetos() {
        return objetos;
    }

    public void dispose() {
        atlasObjetos.dispose();
    }
}
