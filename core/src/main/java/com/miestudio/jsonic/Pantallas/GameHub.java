package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Sistema de hub de juego que muestra información importante al jugador.
 * Permite diseñar fácilmente con texturas y posicionar elementos libremente.
 */
public class GameHub implements Disposable {

    // Tipos de componentes del hub
    public enum ComponentType {
        TIME, RINGS, TRASH, RECORD, LIVES
    }

    // Componente individual del hub
    public static class HubComponent {
        public ComponentType type;
        public Rectangle backgroundRect;
        public Rectangle iconRect;
        public Texture backgroundTexture;
        public Texture iconTexture;
        public String label;
        public String value;
        public BitmapFont font;
        public Color labelColor = Color.WHITE;
        public Color valueColor = Color.WHITE;
        
        public HubComponent(ComponentType type, float x, float y, 
                           float bgWidth, float bgHeight,
                           float iconSize, 
                           Texture bgTexture, Texture iconTexture, 
                           BitmapFont font) {
            this.type = type;
            this.backgroundTexture = bgTexture;
            this.iconTexture = iconTexture;
            this.font = font;
            
            // Configurar rectángulos
            backgroundRect = new Rectangle(x, y, bgWidth, bgHeight);
            iconRect = new Rectangle(
                x + 10, 
                y + bgHeight - iconSize - 10, 
                iconSize, 
                iconSize
            );
            
            // Configurar etiquetas por defecto
            switch(type) {
                case TIME: label = "Tiempo"; break;
                case RINGS: label = "Anillos"; break;
                case TRASH: label = "Basura"; break;
                case RECORD: label = "Récord"; break;
                case LIVES: label = "Vidas"; break;
            }
            
            value = "0";
        }
        
        public void render(SpriteBatch batch) {
           
            // Dibujar fondo
            batch.draw(backgroundTexture, 
                      backgroundRect.x, backgroundRect.y,
                      backgroundRect.width, backgroundRect.height);
            
            // Dibujar icono
            batch.draw(iconTexture,
                      iconRect.x, iconRect.y,
                      iconRect.width, iconRect.height);
            
            // Dibujar etiqueta (arriba del rectángulo)
            font.setColor(labelColor);
            font.draw(batch, label, 
                     backgroundRect.x + backgroundRect.width/2, 
                     backgroundRect.y + backgroundRect.height + 20, 
                     0, label.length(), 
                     backgroundRect.width, 1, true);
            
            // Dibujar valor (centrado en el rectángulo)
            font.setColor(valueColor);
            font.draw(batch, value, 
                     backgroundRect.x, 
                     backgroundRect.y + backgroundRect.height/2 + 10, 
                     backgroundRect.width, 1, true);
           
        }
        
        public void setPosition(float x, float y) {
            float deltaX = x - backgroundRect.x;
            float deltaY = y - backgroundRect.y;
            backgroundRect.setPosition(x, y);
            iconRect.setPosition(iconRect.x + deltaX, iconRect.y + deltaY);
        }
        
        public void setSize(float width, float height) {
            // Mantener proporciones del icono
            float iconRatio = iconRect.width / backgroundRect.width;
            backgroundRect.setSize(width, height);
            iconRect.setSize(width * iconRatio, height * iconRatio);
            iconRect.setPosition(
                backgroundRect.x + 10, 
                backgroundRect.y + backgroundRect.height - iconRect.height - 10
            );
        }
    }
    
    private final Array<HubComponent> components = new Array<>();
    private final SpriteBatch batch;
    private float elapsedTime = 0;
    private int rings = 0;
    private int trash = 0;
    private int record = 0;
    private int lives = 3;

    public GameHub(SpriteBatch batch) {
        this.batch = batch;
    }
    
    public void addComponent(HubComponent component) {
        components.add(component);
    }
    
    public HubComponent getComponent(ComponentType type) {
        for(HubComponent comp : components) {
            if(comp.type == type) return comp;
        }
        return null;
    }
    
    public void update(float delta) {
        elapsedTime += delta;
        updateComponentValues();
    }
    
    private void updateComponentValues() {
        for(HubComponent comp : components) {
            switch(comp.type) {
                case TIME:
                    comp.value = formatTime(elapsedTime);
                    break;
                case RINGS:
                    comp.value = String.valueOf(rings);
                    break;
                case TRASH:
                    comp.value = String.valueOf(trash);
                    break;
                case RECORD:
                    comp.value = String.valueOf(record);
                    break;
                case LIVES:
                    comp.value = String.valueOf(lives);
                    // Cambiar color si quedan pocas vidas
                    if(lives <= 1) comp.valueColor = Color.RED;
                    else comp.valueColor = Color.WHITE;
                    break;
            }
        }
    }
    
    private String formatTime(float seconds) {
        int minutes = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%02d:%02d", minutes, secs);
    }
    
    public void render() {
        for(HubComponent comp : components) {
            comp.render(batch);
        }
    }
    
    
    
    // Métodos para actualizar valores del juego
    public void addRings(int count) { rings += count; }
    public void addTrash(int count) { trash += count; }
    public void setRecord(int value) { record = value; }
    public void setLives(int value) { lives = value; }
    public void loseLife() { lives = Math.max(0, lives - 1); }
    public void gainLife() { lives++; }
    
    @Override
    public void dispose() {
        for(HubComponent comp : components) {
            comp.backgroundTexture.dispose();
            comp.iconTexture.dispose();
        }
    }
}
