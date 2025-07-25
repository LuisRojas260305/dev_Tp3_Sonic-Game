package com.miestudio.jsonic.Util;

/**
 * Clase que contiene todas las constantes utilizadas a lo largo del proyecto.
 * Centraliza valores como puertos de red, limites de jugadores, rutas de assets y constantes fisicas
 * para facilitar su gestion y modificacion.
 */
public class Constantes {

    /** Puerto UDP utilizado para el descubrimiento de servidores en la red local. */
    public static final int DISCOVERY_PORT = 8888;
    /** Puerto TCP y UDP principal para la comunicacion del juego. */
    public static final int GAME_PORT = 7777;

    /** Numero maximo de jugadores permitidos en una partida. */
    public static final int MAX_PLAYERS = 3;

    /** Ruta base para los archivos de assets de personajes. */
    public static final String PERSONAJES_PATH = "Personajes/";
    /** Ruta base para los archivos de assets de mapas. */
    public static final String MAPA_PATH = "Mapa/";
    /** Ruta base para los archivos de assets de background GameScreen*/
    public static final String BACKGROUND_GAMESCREEN_PATH = "Background/GameScreen/";
    /** Ruta base para los archivos de assets de backgroud MainScreen*/
    public static final String BACKGROUND_MAINSCREEN_PATH = "Background/MainScreen/";
    /** Ruta base para los archivos de assets de background HelpScreen*/
    public static final String BACKGROUND_HELP_PATH = "Background/HelpScreen/";
    /** Reta base para los archivos de assets de objetos */
    public static final String OBJECTS_PATH = "Objetos/";

    /** Constante de gravedad aplicada a los personajes y objetos en el juego. */
    public static final float GRAVITY = -500f;
    /** Fuerza aplicada a los personajes al realizar un salto. */
    public static final float JUMP_FORCE = 200f;

    /** Tamaño estandar de un tile en el mapa del juego en pixeles. */
    public static final int TILE_SIZE = 32;

    public static final float OBJECT_COLLISION_OFFSET = 5f; /** Desplazamiento adicional para ajustar la detección de colisiones de objetos. */
}
