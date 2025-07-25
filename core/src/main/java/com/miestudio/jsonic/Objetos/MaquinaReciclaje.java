package com.miestudio.jsonic.Objetos;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.miestudio.jsonic.Util.Assets;

public class MaquinaReciclaje extends Objetos {
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> activeAnimation;
    private Animation<TextureRegion> reverseAnimation;
    private float stateTime;
    private boolean isActive;
    private int totalCollectedTrash = 0;
    private Texture screenTexture;
    private BitmapFont font;

    public MaquinaReciclaje(float x, float y, TextureAtlas atlas, Assets assets) {
        super(x, y, atlas.findRegion("MReciclaje0")); // Textura inicial
        cargarAnimaciones(atlas);
        this.isActive = false;
        this.stateTime = 0;
        this.screenTexture = assets.screenTexture;
        this.font = assets.hubFont;
    }

    private void cargarAnimaciones(TextureAtlas atlas) {
        // Animación inactiva (0 al 4)
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i <= 4; i++) {
            idleFrames.add(atlas.findRegion("MReciclaje" + i));
        }
        idleAnimation = new Animation<>(0.1f, idleFrames, Animation.PlayMode.LOOP);

        // Animación activa (5 al 11)
        Array<TextureRegion> activeFrames = new Array<>();
        for (int i = 5; i <= 11; i++) {
            activeFrames.add(atlas.findRegion("MReciclaje" + i));
        }
        activeAnimation = new Animation<>(0.1f, activeFrames, Animation.PlayMode.NORMAL);

        // Animación inversa (11 al 5)
        Array<TextureRegion> reverseFrames = new Array<>();
        for (int i = 11; i >= 5; i--) {
            reverseFrames.add(atlas.findRegion("MReciclaje" + i));
        }
        reverseAnimation = new Animation<>(0.1f, reverseFrames, Animation.PlayMode.NORMAL);
    }

    @Override
    public void actualizar(float delta) {
        stateTime += delta;
        if (isActive) {
            if (activeAnimation.isAnimationFinished(stateTime)) {
                // Cuando la animación activa termina, iniciar la reversa
                stateTime = 0; // Reiniciar stateTime para la animación inversa
                isActive = false; // Desactivar para que no se repita la animación activa
                // La animación inversa se manejará en renderizar
            }
        }
        // Actualizar la textura actual
        if (isActive) {
            textura = activeAnimation.getKeyFrame(stateTime);
        } else if (stateTime < reverseAnimation.getAnimationDuration()) { // Si la animación inversa no ha terminado
            textura = reverseAnimation.getKeyFrame(stateTime);
        } else {
            textura = idleAnimation.getKeyFrame(stateTime); // Volver a la inactiva
        }
    }

    @Override
    public void renderizar(SpriteBatch batch) {
        super.renderizar(batch); // Renderiza la animación de la máquina

        // Dibujar la pantalla de la máquina
        float screenX = x + 8; // Coordenada X de la pantalla dentro de la máquina
        float screenY = y + 21; // Coordenada Y de la pantalla dentro de la máquina
        float screenWidth = 39; // Ancho de la pantalla
        float screenHeight = 38; // Alto de la pantalla

        batch.draw(screenTexture, screenX, screenY, screenWidth, screenHeight);

        // Dibujar el texto de la basura recolectada
        String trashText = String.valueOf(totalCollectedTrash);
        font.setColor(Color.GREEN);

        GlyphLayout layout = new GlyphLayout(font, trashText);

        // Calcular la posición para centrar el texto en la pantalla
        // Las coordenadas internas de la imagen son 8:21 y 26:3
        // Esto significa que el área de texto es de 18x18 píxeles (26-8 = 18, 21-3 = 18)
        float textX = x + 8 + (screenWidth - layout.width) / 2; // Centrar horizontalmente
        float textY = y + 21 + (screenHeight + layout.height) / 2; // Centrar verticalmente

        font.draw(batch, trashText, textX, textY);
    }

    public void activate() {
        if (!isActive) {
            isActive = true;
            stateTime = 0; // Reiniciar el tiempo para la animación activa
        }
    }

    public void addTrash(int amount) {
        this.totalCollectedTrash += amount;
        activate(); // Activar la animación cuando se añade basura
    }

    public boolean isAnimationFinished() {
        return activeAnimation.isAnimationFinished(stateTime);
    }

    public int getTotalCollectedTrash() {
        return totalCollectedTrash;
    }

    public void setTotalCollectedTrash(int totalCollectedTrash) {
        this.totalCollectedTrash = totalCollectedTrash;
    }
}
