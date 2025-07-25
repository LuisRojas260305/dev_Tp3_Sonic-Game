package com.miestudio.jsonic.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Clase para gestionar la carga y descarga de todos los assets del juego.
 * Centraliza la gestion de recursos para una mejor organizacion y mantenimiento.
 */
public class Assets {

    /**
     * Atlas de texturas para el personaje Sonic.
     */
    public TextureAtlas sonicAtlas;
    /**
     * Atlas de texturas para el personaje Tails.
     */
    public TextureAtlas tailsAtlas;
    /**
     * Atlas de texturas para el personaje Knuckles.
     */
    public TextureAtlas knucklesAtlas;

    public TextureAtlas objetosAtlas;

    public Animation<TextureRegion> anilloAnimation;
    
    public TextureAtlas egmanAtlas;
    public Texture trashTexture;
    public TextureAtlas maquinaAtlas;
    public TextureAtlas enemyAtlas;
    public Texture treeTexture;
    public Texture rockTexture;
    
    public BitmapFont  hubFont;
    /**
     * Carga todos los assets del juego.
     * Este metodo debe ser llamado al inicio de la aplicacion para precargar los recursos.
     */
    public void load() {
        try {
            sonicAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "AtlasSonic.txt"));
            tailsAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "AtlasTails.txt"));
            knucklesAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "AtlasKnuckles.txt"));
            objetosAtlas = new TextureAtlas(Gdx.files.internal(Constantes.OBJECTS_PATH + "Objetos.txt"));
            egmanAtlas = new TextureAtlas(Gdx.files.internal("Personajes/AtlasEgman.txt"));
            hubFont = new BitmapFont(Gdx.files.internal("ui/default.fnt"));
            trashTexture = new Texture(Gdx.files.internal("ui/HubB.png"));
            maquinaAtlas = new TextureAtlas(Gdx.files.internal(Constantes.OBJECTS_PATH + "AtlasMaquina.txt"));
            // Verificar si las regiones de MaquinaReciclaje se cargan correctamente
            if (maquinaAtlas.findRegion("MReciclaje0") == null) {
                Gdx.app.error("Assets", "MReciclaje0 no encontrado en AtlasMaquina.txt");
            }
            if (maquinaAtlas.findRegion("MReciclaje5") == null) {
                Gdx.app.error("Assets", "MReciclaje5 no encontrado en AtlasMaquina.txt");
            }
            enemyAtlas = new TextureAtlas(Gdx.files.internal(Constantes.PERSONAJES_PATH + "AtlasEnemy.atlas"));
            treeTexture = new Texture(Gdx.files.internal(Constantes.OBJECTS_PATH + "Arbol.png"));
            rockTexture = new Texture(Gdx.files.internal(Constantes.OBJECTS_PATH + "Roca.png"));
            
            Array<TextureRegion> framesAnillo = new Array<>();
            for (int i = 0; i < 5; i++) {
                framesAnillo.add(objetosAtlas.findRegion("ObjA" + i));
            }
            anilloAnimation = new Animation<>(0.15f, framesAnillo, Animation.PlayMode.LOOP);

        } catch (Exception e) {

        }
    }

    /**
     * Libera todos los assets cargados.
     * Este metodo debe ser llamado al finalizar la aplicacion para evitar fugas de memoria.
     */
    public void dispose() {
        if (sonicAtlas != null) sonicAtlas.dispose();
        if (tailsAtlas != null) tailsAtlas.dispose();
        if (knucklesAtlas != null) knucklesAtlas.dispose();
        if (objetosAtlas != null) objetosAtlas.dispose();
        if (egmanAtlas != null) egmanAtlas.dispose();
        if (trashTexture != null) trashTexture.dispose();
        if (maquinaAtlas != null) maquinaAtlas.dispose();
        if (enemyAtlas != null) enemyAtlas.dispose();
        if (treeTexture != null) treeTexture.dispose();
        if (rockTexture != null) rockTexture.dispose();
    }
}
