/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 *
 * @author usuario
 */
public class CargarObjetos {
    private Array<Objetos> objetos;
    private TextureAtlas atlasObjetos;
    
    // Animaciones
    private Animation<TextureRegion> animacionAnillo;


    public CargarObjetos() {
        objetos = new Array<>();
        cargarRecursos();
        crearObjetosIniciales();
        
    }
    
    private void cargarRecursos() {
        atlasObjetos = new TextureAtlas(Gdx.files.internal("objetos.txt"));
        
        // Cargar animación de anillos
        Array<TextureRegion> framesAnillo = new Array<>();
        for (int i = 0; i < 5; i++) {
            framesAnillo.add(atlasObjetos.findRegion("ObjA" + i));
        }
        animacionAnillo = new Animation<>(0.15f, framesAnillo, Animation.PlayMode.LOOP);
        
        
    }

    private void crearObjetosIniciales() {
        // Ejemplo: Crear 10 anillos en línea
        for (int i = 0; i < 10; i++) {
            agregarAnillo(100 + i * 17, 110);
        }
        
       
    }

    
    public void agregarAnillo(float x, float y) {
        objetos.add(new Anillo(x, y, animacionAnillo));
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

    public void agregarEfecto(float x, float y, String tipo) {
        // Cargar animación según el tipo de efecto
        Array<TextureRegion> framesEfecto = new Array<>();
        for (int i = 0; i < 4; i++) {
            framesEfecto.add(atlasObjetos.findRegion(tipo + i));
        }
        Animation<TextureRegion> animacion = new Animation<>(0.09f, framesEfecto);
        objetos.add(new Efecto(x, y, animacion));
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
