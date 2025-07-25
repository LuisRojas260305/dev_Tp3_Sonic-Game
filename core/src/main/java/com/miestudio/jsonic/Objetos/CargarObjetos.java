package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.miestudio.jsonic.Util.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;


/**
 * Clase para cargar, gestionar y actualizar los objetos del juego, como los anillos.
 */
public class CargarObjetos {
    public Array<Objetos> objetos; /** Lista de todos los objetos activos en el juego. */
    private TextureAtlas atlasObjetos; /** Atlas de texturas para los objetos. */
    public Animation<TextureRegion> animacionAnillo; /** Animacion especifica para los anillos. */
    private Texture basuraTexture; /** Textura para los objetos de basura. */

    /**
     * Constructor de CargarObjetos.
     * @param atlasObjetos El TextureAtlas que contiene las texturas de los objetos.
     */
    public CargarObjetos(TextureAtlas atlasObjetos, Assets assets) {
        objetos = new Array<>();
        this.atlasObjetos = atlasObjetos;
        this.basuraTexture = assets.trashTexture;
        cargarRecursos();
    }

    /**
     * Carga los recursos necesarios para los objetos, como las animaciones.
     */
    private void cargarRecursos() {

        // Cargar animacion de anillos
        Array<TextureRegion> framesAnillo = new Array<>();
        for (int i = 0; i < 5; i++) {
            framesAnillo.add(atlasObjetos.findRegion("ObjA" + i));
        }
        animacionAnillo = new Animation<>(0.15f, framesAnillo, Animation.PlayMode.LOOP);
    }

    /**
     * Agrega un nuevo anillo al juego en las coordenadas especificadas.
     * @param x Posicion X del anillo.
     * @param y Posicion Y del anillo.
     */
    public void agregarAnillo(float x, float y) {
        objetos.add(new Anillo(x, y, animacionAnillo));
    }

    /**
     * Agrega un objeto Anillo existente a la lista de objetos del juego.
     * @param anillo El objeto Anillo a agregar.
     */
    public void agregarAnillo(Anillo anillo) {
        objetos.add(anillo);
    }

    /**
     * Agrega un nuevo objeto de basura al juego en las coordenadas especificadas.
     * @param x Posicion X de la basura.
     * @param y Posicion Y de la basura.
     */
    public void agregarBasura(float x, float y) {
        objetos.add(new Objetos(x, y, new TextureRegion(basuraTexture)) {
            @Override
            public void actualizar(float delta) {
                // La basura no tiene animacion ni logica de actualizacion compleja
            }
        });
    }

    /**
     * Actualiza el estado de todos los objetos en el juego.
     * Elimina los objetos que ya no estan activos.
     * @param delta El tiempo transcurrido desde el ultimo fotograma en segundos.
     */
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

    /**
     * Renderiza todos los objetos activos en el juego.
     * @param batch El SpriteBatch utilizado para el renderizado.
     */
    public void renderizar(SpriteBatch batch) {
        for (Objetos objeto : objetos) {
            objeto.renderizar(batch);
        }
    }

    /**
     * Obtiene la lista de todos los objetos activos en el juego.
     * @return Un Array de objetos del juego.
     */
    public Array<Objetos> getObjetos() {
        return objetos;
    }

    /**
     * Libera los recursos utilizados por el gestor de objetos.
     */
    public void dispose() {
        atlasObjetos.dispose();
    }
}
