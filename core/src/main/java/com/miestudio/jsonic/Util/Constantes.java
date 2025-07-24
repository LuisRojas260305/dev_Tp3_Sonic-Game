package com.miestudio.jsonic.Util;

/**
 * Clase que contiene todas las constantes utilizadas a lo largo del proyecto.
 * Centraliza valores como puertos de red, límites de jugadores, rutas de assets y constantes físicas
 * para facilitar su gestión y modificación.
 */
public class Constantes {

    /** Puerto UDP utilizado para el descubrimiento de servidores en la red local. */
    public static final int DISCOVERY_PORT = 8888;
    /** Puerto TCP y UDP principal para la comunicación del juego. */
    public static final int GAME_PORT = 7777;

    /** Número máximo de jugadores permitidos en una partida. */
    public static final int MAX_PLAYERS = 3;

    /** Ruta base para los archivos de assets de personajes. */
    public static final String PERSONAJES_PATH = "Personajes/";
    /** Ruta base para los archivos de assets de mapas. */
    public static final String MAPA_PATH = "Mapa/";
    /** Ruta base para los archivos de assets de background */
    public static final String BACKGROUND_PATH = "Background/";
    /** Reta base para los archivos de assets de objetos */
    public static final String OBJECT_PATCH = "Objetos/";

    /** Constante de gravedad aplicada a los personajes y objetos en el juego. */
    public static final float GRAVITY = -500f;
    /** Fuerza aplicada a los personajes al realizar un salto. */
    public static final float JUMP_FORCE = 200f;

    /** Tamaño estándar de un tile en el mapa del juego en píxeles. */
    public static final int TILE_SIZE = 32;

    public static final float OBJECT_COLLISION_OFFSET = 5f;
}
