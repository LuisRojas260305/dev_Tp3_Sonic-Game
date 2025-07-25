package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MaquinaReciclaje extends Objetos {
    private Animation<TextureRegion> workingAnimation;
    private Animation<TextureRegion> idleAnimation;
    private float stateTime;
    private int totalCollectedTrash = 0;

    public MaquinaReciclaje(float x, float y, TextureAtlas atlas) {
        super(x, y, atlas.findRegion("MReciclaje0")); // Frame inicial
        cargarAnimaciones(atlas);
        this.stateTime = 0;
        this.hitbox = new Rectangle(x, y, getWidth(), getHeight());
        this.activo = true;
    }

    public void addTrash(int amount) {
        this.totalCollectedTrash += amount;
    }

    public int getTotalCollectedTrash() {
        return totalCollectedTrash;
    }

    public void setTotalCollectedTrash(int totalCollectedTrash) {
        this.totalCollectedTrash = totalCollectedTrash;
    }

    private void cargarAnimaciones(TextureAtlas atlas) {
        // Animación de trabajo (todos los frames)
        Array<TextureRegion> workingFrames = new Array<>();
        for (int i = 0; i <= 11; i++) {
            workingFrames.add(atlas.findRegion("MReciclaje" + i));
        }
        workingAnimation = new Animation<>(0.1f, workingFrames, Animation.PlayMode.LOOP);

        // Animación inactiva (un solo frame)
        idleAnimation = new Animation<>(0.1f, atlas.findRegion("MReciclaje0"));
    }

    @Override
    public void actualizar(float delta) {
        stateTime += delta;
        // Por ahora, siempre en animación de trabajo. Se puede añadir lógica para cambiar.
        textura = workingAnimation.getKeyFrame(stateTime, true);
    }

    @Override
    public float getWidth() {
        return workingAnimation.getKeyFrame(0).getRegionWidth();
    }

    @Override
    public float getHeight() {
        return workingAnimation.getKeyFrame(0).getRegionHeight();
    }
}
