/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.miestudio.jsonic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;

public class Hub {
    // Clase interna para cada elemento del HUD
    public static class ElementoHub {
        // Identificador del elemento
        public String id;
        
        // Componentes visuales
        public TextureRegion icono;
        public TextureRegion panelPeque;
        public TextureRegion panelGrande;
        
        // Textos
        public String etiqueta;
        public String valor;
        
        // Posiciones y tamaños
        public float x, y;
        public float iconX, iconY;
        public float panelPequeX, panelPequeY;
        public float panelGrandeX, panelGrandeY;
        public float etiquetaX, etiquetaY;
        public float valorX, valorY;
        
        // Tamaños
        public float iconWidth = 20, iconHeight = 20;
        public float panelPequeWidth = 120, panelPequeHeight = 20;
        public float panelGrandeWidth = 120, panelGrandeHeight = 40;
        
        // Fuentes
        public BitmapFont fontEtiqueta;
        public BitmapFont fontValor;
        
        // Visibilidad
        public boolean visible = true;
        
        public ElementoHub(String id, String etiqueta, String valor, 
                          TextureRegion icono, TextureRegion panelPeque, TextureRegion panelGrande,
                          BitmapFont fontEtiqueta, BitmapFont fontValor) {
            this.id = id;
            this.etiqueta = etiqueta;
            this.valor = valor;
            this.icono = icono;
            this.panelPeque = panelPeque;
            this.panelGrande = panelGrande;
            this.fontEtiqueta = fontEtiqueta;
            this.fontValor = fontValor;
        }
        
        public void render(SpriteBatch batch) {
            if (!visible) return;
            
            // Dibujar paneles
            if (panelPeque != null) {
                batch.draw(panelPeque, panelPequeX, panelPequeY, panelPequeWidth, panelPequeHeight);
            }
            
            if (panelGrande != null) {
                batch.draw(panelGrande, panelGrandeX, panelGrandeY, panelGrandeWidth, panelGrandeHeight);
            }
            
            // Dibujar icono
            if (icono != null) {
                batch.draw(icono, iconX, iconY, iconWidth, iconHeight);
            }
            
            // Dibujar textos
            if (fontEtiqueta != null && etiqueta != null) {
                fontEtiqueta.draw(batch, etiqueta, etiquetaX, etiquetaY);
            }
            
            if (fontValor != null && valor != null) {
                fontValor.draw(batch, valor, valorX, valorY);
            }
        }
        
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
            updatePositions();
        }
        
        public void setIconPosition(float x, float y) {
            this.iconX = x;
            this.iconY = y;
        }
        
        public void setIconSize(float width, float height) {
            this.iconWidth = width;
            this.iconHeight = height;
        }
        
        public void setPanelPequePosition(float x, float y) {
            this.panelPequeX = x;
            this.panelPequeY = y;
        }
        
        public void setPanelPequeSize(float width, float height) {
            this.panelPequeWidth = width;
            this.panelPequeHeight = height;
        }
        
        public void setPanelGrandePosition(float x, float y) {
            this.panelGrandeX = x;
            this.panelGrandeY = y;
        }
        
        public void setPanelGrandeSize(float width, float height) {
            this.panelGrandeWidth = width;
            this.panelGrandeHeight = height;
        }
        
        public void setEtiquetaPosition(float x, float y) {
            this.etiquetaX = x;
            this.etiquetaY = y;
        }
        
        public void setValorPosition(float x, float y) {
            this.valorX = x;
            this.valorY = y;
        }
        
        private void updatePositions() {
            // Posiciones relativas por defecto
            panelPequeX = x;
            panelPequeY = y;
            
            iconX = x + 5;
            iconY = y + (panelPequeHeight - iconHeight) / 2;
            
            etiquetaX = x + iconWidth + 10;
            etiquetaY = y + panelPequeHeight * 0.75f;
            
            panelGrandeX = x;
            panelGrandeY = y - panelGrandeHeight - 5;
            
            valorX = x + panelGrandeWidth / 2;
            valorY = panelGrandeY + panelGrandeHeight * 0.7f;
        }
    }
    
    // Elementos del HUD
    private ElementoHub tiempoElement;
    private ElementoHub monedasElement;
    private ElementoHub basuraElement;
    private ElementoHub vidasElement;
    private ElementoHub recordElement;
    
    // Datos del juego
    private int monedas;
    private int basura;
    private int vidas;
    private float tiempo;
    private int record;

    public Hub(TextureRegion panelPeque, TextureRegion panelGrande, 
               TextureRegion iconoTiempo, TextureRegion iconoMonedas, 
               TextureRegion iconoBasura, TextureRegion iconoVidas, 
               TextureRegion iconoRecord,
               BitmapFont fontEtiqueta, BitmapFont fontValor) {
        
        // Crear elementos con posiciones iniciales
        tiempoElement = new ElementoHub("tiempo", "Tiempo", "", 
                                       iconoTiempo, panelPeque, panelGrande,
                                       fontEtiqueta, fontValor);
        tiempoElement.setPosition(20, Gdx.graphics.getHeight() - 30);
        
        monedasElement = new ElementoHub("monedas", "Anillos", "", 
                                        iconoMonedas, panelPeque, panelGrande,
                                        fontEtiqueta, fontValor);
        monedasElement.setPosition(20, Gdx.graphics.getHeight() - 100);
        
        basuraElement = new ElementoHub("basura", "Basura", "", 
                                       iconoBasura, panelPeque, panelGrande,
                                       fontEtiqueta, fontValor);
        basuraElement.setPosition(20, Gdx.graphics.getHeight() - 170);
        
        vidasElement = new ElementoHub("vidas", "Vidas", "", 
                                      iconoVidas, panelPeque, panelGrande,
                                      fontEtiqueta, fontValor);
        vidasElement.setPosition(20, Gdx.graphics.getHeight() - 240);
        
        recordElement = new ElementoHub("record", "Récord", "", 
                                       iconoRecord, panelPeque, panelGrande,
                                       fontEtiqueta, fontValor);
        recordElement.setPosition(20, Gdx.graphics.getHeight() - 310);
        
        // Inicializar valores
        monedas = 0;
        basura = 0;
        vidas = 3;
        tiempo = 0;
        record = 0;
    }
    
    public void actualizar(float delta) {
        // Actualizar el tiempo de juego
        tiempo += delta;
        
        // Actualizar valores en los elementos
        tiempoElement.valor = formatearTiempo(tiempo);
        monedasElement.valor = String.valueOf(monedas);
        basuraElement.valor = String.valueOf(basura);
        vidasElement.valor = String.valueOf(vidas);
        recordElement.valor = formatearTiempo(record);
    }
    
    public void setMonedas(int monedas) {
        this.monedas = monedas;
    }
    
    public void setBasura(int basura) {
        this.basura = basura;
    }
    
    public void setVidas(int vidas) {
        this.vidas = vidas;
    }
    
    public void setTiempo(float tiempo) {
        this.tiempo = tiempo;
    }
    
    public void setRecord(int record) {
        this.record = record;
    }
    
    public void render(SpriteBatch batch) {
        tiempoElement.render(batch);
        monedasElement.render(batch);
        basuraElement.render(batch);
        vidasElement.render(batch);
        recordElement.render(batch);
    }
    
    private String formatearTiempo(float segundos) {
        int minutos = (int)(segundos / 60);
        int segundosResto = (int)(segundos % 60);
        return String.format("%02d:%02d", minutos, segundosResto);
    }
    
    // Métodos para acceder a los elementos
    public ElementoHub getTiempoElement() {
        return tiempoElement;
    }
    
    public ElementoHub getMonedasElement() {
        return monedasElement;
    }
    
    public ElementoHub getBasuraElement() {
        return basuraElement;
    }
    
    public ElementoHub getVidasElement() {
        return vidasElement;
    }
    
    public ElementoHub getRecordElement() {
        return recordElement;
    }
    
    public void dispose() {
        // Las fuentes se deben destruir externamente si son compartidas
    }
}