package com.miestudio.jsonic;

import com.badlogic.gdx.Game;
import com.miestudio.jsonic.Pantallas.MainScreen;
import com.miestudio.jsonic.Server.NetworkManager;
import com.miestudio.jsonic.Util.Assets;
import com.miestudio.jsonic.Server.domain.ShutdownPacket;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase principal del juego que extiende la clase Game de LibGDX.
 * Controla la gestion global del juego, inicializa el NetworkManager
 * y establece la pantalla inicial.
 */
public class JuegoSonic extends Game {

    /** Gestor de red para manejar la creacion del host y la conexion de clientes. */
    public NetworkManager networkManager;
    /** Gestor de assets para cargar y liberar recursos. */
    private Assets assets;

    /** Almacena el estado de seleccion de personajes (true si esta tomado, false si esta disponible). */
    public ConcurrentHashMap<String, Boolean> selectedCharacters;

    /**
     * Metodo principal de inicializacion del juego.
     * Se llama una vez al crear la aplicacion. Aqui se inicializan los recursos y gestores principales.
     */
    @Override
    public void create() {
        assets = new Assets();
        assets.load(); // Cargar todos los assets al inicio

        networkManager = new NetworkManager(this);

        selectedCharacters = new ConcurrentHashMap<>();
        selectedCharacters.put("Sonic", false);
        selectedCharacters.put("Tails", false);
        selectedCharacters.put("Knuckles", false);

        // Al iniciar, siempre mostramos la pantalla principal para elegir rol o iniciar el juego.
        setScreen(new MainScreen(this));
    }

    /**
     * Libera todos los recursos del juego cuando la aplicacion es destruida.
     * Es crucial para evitar fugas de memoria y asegurar un cierre limpio.
     */
    @Override
    public void dispose() {
        if (networkManager != null) {
            // Si esta instancia es el host, notifica a los clientes antes de cerrar.
            if (networkManager.isHost()) {
                networkManager.broadcastTcpMessage(new ShutdownPacket());
            }
            networkManager.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
        super.dispose();
    }

    /**
     * Obtiene la instancia del gestor de assets.
     * @return La instancia de la clase Assets.
     */
    public Assets getAssets() {
        return assets;
    }

    /**
     * Verifica si un personaje ya ha sido seleccionado.
     * @param characterType El tipo de personaje (ej. "Sonic", "Tails").
     * @return true si el personaje ya esta tomado, false en caso contrario.
     */
    public boolean isCharacterTaken(String characterType) {
        return selectedCharacters.getOrDefault(characterType, false);
    }

    /**
     * Establece el estado de seleccion de un personaje.
     * @param characterType El tipo de personaje.
     * @param taken true si el personaje esta tomado, false si esta disponible.
     */
    public void setCharacterTaken(String characterType, boolean taken) {
        selectedCharacters.put(characterType, taken);
    }
}
