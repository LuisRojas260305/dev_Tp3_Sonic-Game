package com.miestudio.jsonic.Pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Sistema de hub de juego que sigue la cámara y usa fuentes del sistema.
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
        public Vector2 relativePosition; // Posición relativa a la cámara
        
        
        
        
        public HubComponent(ComponentType type, 
                           float relX, float relY, // Posiciones relativas (0-1)
                           float bgWidth, float bgHeight,
                           float iconSize, 
                           Texture bgTexture, Texture iconTexture, 
                           BitmapFont font) {
            this.type = type;
            this.backgroundTexture = bgTexture;
            this.iconTexture = iconTexture;
            this.font = font;
            this.relativePosition = new Vector2(relX, relY);
            
            // Configurar rectángulos iniciales (se actualizarán con la cámara)
            backgroundRect = new Rectangle(0, 0, bgWidth, bgHeight);
            iconRect = new Rectangle(0, 0, iconSize, iconSize);
            
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
        
        public void updatePosition(Vector2 cameraPosition, Vector2 viewportSize) {
            // Calcular posición absoluta basada en posición relativa y cámara
            float absX = cameraPosition.x - viewportSize.x/2 + relativePosition.x * viewportSize.x;
            float absY = cameraPosition.y - viewportSize.y/2 + relativePosition.y * viewportSize.y;
            
            backgroundRect.setPosition(absX, absY);
            iconRect.setPosition(
                absX + 10, 
                absY + backgroundRect.height - iconRect.height - 10
            );
        }
        
        public void render(SpriteBatch batch) {
            // Dibujar fondo
            batch.draw(backgroundTexture, 
                      backgroundRect.x, backgroundRect.y,
                      backgroundRect.width, backgroundRect.height);
            
            // Dibujar icono
            batch.draw(iconTexture,
                      iconRect.x + 130, iconRect.y + 10,
                      iconRect.width, iconRect.height);
            
            // Dibujar etiqueta (arriba del rectángulo)
            font.setColor(labelColor);
            font.draw(batch, label, 
                     backgroundRect.x, 
                     backgroundRect.y + backgroundRect.height - 5);
            
            // Dibujar valor (centrado en el rectángulo)
            font.setColor(valueColor);
            font.draw(batch, value, 
                     backgroundRect.x + backgroundRect.width/2 - 5, 
                     backgroundRect.y + backgroundRect.height/2 + 5);
        }
    }
    
    private final Array<HubComponent> components = new Array<>();
    private float elapsedTime = 0;
    public final BitmapFont systemFont;

    public GameHub() {
        // Crear fuente del sistema
        systemFont = new BitmapFont();
        systemFont.getData().setScale(1.2f); // Tamaño de fuente
        systemFont.setColor(Color.WHITE);
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
    
    public void update(float delta, Vector2 cameraPosition, Vector2 viewportSize) {
        elapsedTime += delta;

        // Actualizar posiciones de los componentes
        for(HubComponent comp : components) {
            comp.updatePosition(cameraPosition, viewportSize);
        }

        updateComponentValues();
    }
    
    private void updateComponentValues() {
        for(HubComponent comp : components) {
            if(comp.type == ComponentType.TIME) {
                comp.value = formatTime(elapsedTime);
            }
        }
    }
    
    private String formatTime(float seconds) {
        int minutes = (int)(seconds / 60);
        int secs = (int)(seconds % 60);
        return String.format("%02d:%02d", minutes, secs);
    }
    
    public void render(SpriteBatch batch) {
        for(HubComponent comp : components) {
            comp.render(batch);
        }
    }

    public void updateCollectibleCount(ComponentType type, int count) {
        HubComponent comp = getComponent(type);
        if (comp != null) {
            comp.value = String.valueOf(count);
        }
    }

    public void updateLives(int lives) {
        HubComponent comp = getComponent(ComponentType.LIVES);
        if (comp != null) {
            comp.value = String.valueOf(lives);
            if (lives <= 1) {
                comp.valueColor = Color.RED;
            } else {
                comp.valueColor = Color.WHITE;
            }
        }
    }

    public void updateRecord(int record) {
        HubComponent comp = getComponent(ComponentType.RECORD);
        if (comp != null) {
            comp.value = String.valueOf(record);
        }
    }
    
    @Override
    public void dispose() {
        systemFont.dispose();
        for(HubComponent comp : components) {
            comp.backgroundTexture.dispose();
            comp.iconTexture.dispose();
        }
    }
}