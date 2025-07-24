package com.miestudio.jsonic.Server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Pantallas.CharacterSelectionScreen;
import com.miestudio.jsonic.Pantallas.GameScreen;
import com.miestudio.jsonic.Pantallas.LobbyScreen;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Server.domain.GameState;
import com.miestudio.jsonic.Server.domain.InputState;
import com.miestudio.jsonic.Server.domain.ShutdownPacket;
import com.miestudio.jsonic.Server.network.CharacterSelectionPacket;
import com.miestudio.jsonic.Server.network.CharacterTakenPacket;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.miestudio.jsonic.Util.CollisionManager;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkManager {

    private final JuegoSonic game; /** Referencia a la instancia principal del juego. */
    private ServerSocket serverTcpSocket; /** Socket TCP para el servidor (solo host). */
    private DatagramSocket udpSocket; /** Socket UDP para el envio y recepcion de datagramas. */
    private final List<ClientConnection> clientConnections = Collections.synchronizedList(new ArrayList<>()); /** Lista de conexiones de clientes (solo host). */
    private final AtomicInteger nextPlayerId = new AtomicInteger(1); /** Generador de IDs para nuevos jugadores. */
    private volatile GameState currentGameState; /** El estado actual del juego, sincronizado entre host y clientes. */
    private Socket clientTcpSocket; /** Socket TCP para la conexion al servidor (solo cliente). */
    private ObjectOutputStream clientTcpOut; /** Stream de salida TCP para el cliente. */
    private ObjectInputStream clientTcpIn; /** Stream de entrada TCP para el cliente. */
    private InetAddress serverAddress; /** Direccion IP del servidor al que el cliente esta conectado. */
    private int serverUdpPort; /** Puerto UDP del servidor. */

    private final ConcurrentHashMap<Integer, InputState> playerInputs = new ConcurrentHashMap<>(); /** Inputs de los jugadores, indexados por ID de jugador. */
    private final ConcurrentHashMap<Integer, String> selectedCharacters = new ConcurrentHashMap<>(); /** Personajes seleccionados por los jugadores, indexados por ID de jugador. */
    private volatile boolean isHost = false; /** Indica si esta instancia es el host del juego. */
    private int localPlayerId = -1; /** ID del jugador local. */
    private String selectedCharacterType; /** Tipo de personaje seleccionado por el jugador local. */

    private Thread hostDiscoveryThread; /** Hilo para la deteccion de host. */
    private Thread clientTcpReceiveThread; /** Hilo para la recepcion de mensajes TCP del servidor (solo cliente). */
    private Thread udpReceiveThread; /** Hilo para la recepcion de mensajes UDP. */

    private GameServer gameServer; /** Instancia del GameServer (solo host). */

    /**
     * Constructor de NetworkManager.
     * @param game La instancia principal del juego.
     */
    public NetworkManager(JuegoSonic game) {
        this.game = game;
    }

    /**
     * Inicia el proceso de verificacion de estado de la red.
     * Intenta descubrir un host existente; si no lo encuentra, inicia uno nuevo.
     * Luego, transiciona a la pantalla de seleccion de personaje.
     */
    public void checkNetworkStatus() {
        Thread networkThread = new Thread(() -> {
            String serverIp = discoverServer();
            if (serverIp != null) {
                connectAsClient(serverIp, Constantes.GAME_PORT);
            } else {
                startHost();
            }
            Gdx.app.postRunnable(() -> game.setScreen(new CharacterSelectionScreen(game, game.selectedCharacters)));
        });
        networkThread.start();
    }

    /**
     * Envia la seleccion de personaje del jugador local al servidor.
     * Si es cliente, envia un paquete TCP. Si es host, procesa la seleccion directamente.
     * @param characterType El tipo de personaje seleccionado (ej. "Sonic", "Tails").
     */
    public void sendCharacterSelection(String characterType) {
        this.selectedCharacterType = characterType;
        if (!isHost) {
            sendTcpMessageToServer(new CharacterSelectionPacket(localPlayerId, characterType));
        } else {
            processCharacterSelection(localPlayerId, characterType);
        }
    }

    /**
     * Envia un mensaje TCP al servidor. Solo para clientes.
     * @param message El objeto Serializable a enviar.
     */
    public void sendTcpMessageToServer(Object message) {
        try {
            if (clientTcpOut != null) {
                clientTcpOut.writeObject(message);
                clientTcpOut.flush();
            }
        } catch (IOException e) {
            Gdx.app.error("NetworkManager", "Error al enviar mensaje TCP al servidor: " + e.getMessage());
        }
    }

    /**
     * Procesa la seleccion de un personaje, marcandolo como tomado y notificando a los demas clientes.
     * @param playerId El ID del jugador que selecciono el personaje.
     * @param characterType El tipo de personaje seleccionado.
     */
    private void processCharacterSelection(int playerId, String characterType) {
        if (game.isCharacterTaken(characterType)) {
            return;
        }
        game.setCharacterTaken(characterType, true);
        selectedCharacters.put(playerId, characterType);

        broadcastTcpMessage(new CharacterTakenPacket(characterType, true));

        // Solo transicionar si es el jugador local
        if (playerId == localPlayerId) {
            Gdx.app.postRunnable(() -> game.setScreen(new LobbyScreen(game, getColorForCharacter(characterType), isHost)));
        }
    }

    /**
     * Obtiene un color asociado a un tipo de personaje.
     * @param characterType El tipo de personaje.
     * @return Un objeto Color que representa el color del personaje.
     */
    private Color getColorForCharacter(String characterType) {
        if ("Sonic".equals(characterType)) {
            return Color.BLUE;
        } else if ("Tails".equals(characterType)) {
            return Color.YELLOW;
        } else if ("Knuckles".equals(characterType)) {
            return Color.RED;
        } else {
            return Color.GRAY;
        }
    }

    /**
     * Intenta descubrir un servidor existente en la red local enviando un paquete de difusion UDP.
     * Espera una respuesta durante un tiempo limitado.
     * @return La direccion IP del servidor si se encuentra, o null si no se encuentra ninguno.
     */
    private String discoverServer() {
        try (DatagramSocket discoverySocket = new DatagramSocket(Constantes.DISCOVERY_PORT)) {
            discoverySocket.setSoTimeout(2000);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            discoverySocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            return packet.getAddress().getHostAddress();
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Inicia el servidor del juego, configurando los sockets TCP y UDP,
     * y comenzando los hilos para anunciar el servidor y aceptar clientes.
     */
    public void startHost() {
        isHost = true;
        try {
            serverTcpSocket = new ServerSocket(Constantes.GAME_PORT);
            serverTcpSocket.setSoTimeout(1000);
            udpSocket = new DatagramSocket(Constantes.GAME_PORT);
            localPlayerId = 0;

            hostDiscoveryThread = new Thread(this::announceServer);
            hostDiscoveryThread.setDaemon(true);
            hostDiscoveryThread.start();

            Thread acceptClientsThread = new Thread(this::acceptClients);
            acceptClientsThread.setDaemon(true);
            acceptClientsThread.start();
            startUdpListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicializa la instancia de GameServer en el host.
     * Este metodo se llama una vez que el mapa y el gestor de colisiones estan listos.
     * @param map El mapa de tiles del juego.
     * @param collisionManager El gestor de colisiones del juego.
     * @param mapWidth El ancho del mapa en pixeles.
     * @param mapHeight La altura del mapa en pixeles.
     */
    public void initializeGameServer(TiledMap map, CollisionManager collisionManager, float mapWidth, float mapHeight) {
        if (gameServer == null) {
            gameServer = new GameServer(game, playerInputs, map, collisionManager, mapWidth, mapHeight);
            gameServer.start();
        }
    }

    /**
     * Anuncia la presencia del servidor en la red local a traves de paquetes UDP de difusion.
     * Este metodo se ejecuta en un hilo separado.
     */
    private void announceServer() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            while (isHost && !Thread.currentThread().isInterrupted()) {
                String message = "SONIC_GAME_HOST";
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), Constantes.DISCOVERY_PORT);
                socket.send(packet);
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Acepta nuevas conexiones de clientes TCP. Se ejecuta en un hilo separado.
     * Limita el numero de clientes conectados a {@link Constantes#MAX_PLAYERS}.
     */
    private void acceptClients() {
        while (isHost && !serverTcpSocket.isClosed()) {
            try {
                Socket clientSocket = serverTcpSocket.accept();
                if (clientConnections.size() < Constantes.MAX_PLAYERS - 1) {
                    int playerId = nextPlayerId.getAndIncrement();
                    ClientConnection connection = new ClientConnection(clientSocket, playerId);
                    clientConnections.add(connection);
                    Thread clientThread = new Thread(connection);
                    clientThread.setDaemon(true);
                    clientThread.start();
                } else {
                    clientSocket.close();
                }
            } catch (SocketTimeoutException e) {
                // Timeout, continuar
            } catch (IOException e) {
                if (!isHost || serverTcpSocket.isClosed()) {
                    break;
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * Conecta esta instancia como cliente a un servidor existente.
     * @param ip La direccion IP del servidor.
     * @param port El puerto TCP del servidor.
     */
    public void connectAsClient(String ip, int port) {
        isHost = false;
        try {
            serverAddress = InetAddress.getByName(ip);
            clientTcpSocket = new Socket(serverAddress, port);
            udpSocket = new DatagramSocket();
            try {
                udpSocket.setSoTimeout(1000);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            clientTcpOut = new ObjectOutputStream(clientTcpSocket.getOutputStream());
            clientTcpIn = new ObjectInputStream(clientTcpSocket.getInputStream());

            clientTcpOut.writeInt(udpSocket.getLocalPort());
            clientTcpOut.flush();

            int playerId = clientTcpIn.readInt();
            serverUdpPort = clientTcpIn.readInt();
            localPlayerId = playerId;

            if (playerId != -1) {
                startClientTcpListener(clientTcpIn, playerId);
                startUdpListener();
            } else {
                clientTcpSocket.close();
                udpSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicia un hilo para escuchar mensajes TCP entrantes del servidor (solo para clientes).
     * @param clientTcpIn El ObjectInputStream para leer mensajes del servidor.
     * @param playerId El ID del jugador local.
     */
    private void startClientTcpListener(ObjectInputStream clientTcpIn, int playerId) {
        clientTcpReceiveThread = new Thread(() -> {
            try {
                while (!clientTcpSocket.isClosed()) {
                    Object msg = clientTcpIn.readObject();
                    if ("START_GAME".equals(msg)) {
                        Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, playerId)));
                    } else if (msg instanceof ShutdownPacket) {
                        Gdx.app.postRunnable(game::dispose);
                    } else if (msg instanceof CharacterSelectionPacket) {
                        CharacterSelectionPacket packet = (CharacterSelectionPacket) msg;
                        Gdx.app.postRunnable(() -> game.setScreen(new LobbyScreen(game, getColorForCharacter(packet.characterType), isHost)));
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                // Desconexión
            }
        });
        clientTcpReceiveThread.setDaemon(true);
        clientTcpReceiveThread.start();
    }

    /**
     * Inicia un hilo para escuchar mensajes UDP entrantes.
     * Si es host, escucha inputs de clientes. Si es cliente, escucha GameStates del servidor.
     */
    private void startUdpListener() {
        try {
            udpSocket.setSoTimeout(1000); // Timeout de 1 segundo para no bloquear indefinidamente
        } catch (SocketException e) {
            Gdx.app.error("NetworkManager", "Error al configurar timeout UDP: " + e.getMessage());
        }

        udpReceiveThread = new Thread(() -> {
            byte[] buffer = new byte[4096]; // Buffer para los datos del paquete UDP
            Gdx.app.log("NetworkManager", "Iniciando escucha UDP...");

            while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Preparar paquete para recepción
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet); // Espera hasta recibir datos (o timeout)

                    // Procesar paquete recibido: deserializar el objeto
                    ByteArrayInputStream bais = new ByteArrayInputStream(
                        packet.getData(),
                        0,
                        packet.getLength()
                    );

                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Object obj = ois.readObject();

                    if (isHost) {
                        // Host recibe inputs de los clientes
                        if (obj instanceof InputState) {
                            InputState input = (InputState) obj;
                            

                            // Guardar input para procesamiento en el game loop del servidor
                            playerInputs.put(input.getPlayerId(), input);
                        }
                    } else {
                        // Cliente recibe estados del juego del host
                        if (obj instanceof GameState) {
                            GameState gameState = (GameState) obj;
                            Gdx.app.log("NetworkManager",
                                "Estado recibido - Secuencia: " + gameState.getSequenceNumber() +
                                    ", Jugadores: " + gameState.getPlayers().size()
                            );

                            // Actualizar estado actual del juego del cliente
                            currentGameState = gameState;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout esperado, continuar el bucle para seguir escuchando
                } catch (SocketException e) {
                    if (!udpSocket.isClosed()) {
                        Gdx.app.error("NetworkManager", "Error de socket UDP: " + e.getMessage());
                    }
                    break; // Salir si el socket se cerro inesperadamente
                } catch (IOException e) {
                    Gdx.app.error("NetworkManager", "Error IO UDP: " + e.getMessage());
                } catch (ClassNotFoundException e) {
                    Gdx.app.error("NetworkManager", "Clase desconocida en paquete UDP: " + e.getMessage());
                }
            }
            Gdx.app.log("NetworkManager", "Escucha UDP terminada.");
        });

        udpReceiveThread.setDaemon(true);
        udpReceiveThread.setName("UDP-Listener");
        udpReceiveThread.start();
    }

    /**
     * Inicia la partida. Solo el host puede llamar a este metodo.
     * Envia un mensaje TCP a todos los clientes para que transicionen a la GameScreen.
     */
    public void startGame() {
        if (isHost) {
            broadcastTcpMessage("START_GAME");
            Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, 0))); // El host también va a la GameScreen
        }
    }

    /**
     * Envia un mensaje TCP a todos los clientes conectados (solo host).
     * @param message El objeto Serializable a enviar.
     */
    public void broadcastTcpMessage(Object message) {
        synchronized (clientConnections) {
            for (ClientConnection conn : clientConnections) {
                conn.sendTcpMessage(message);
            }
        }
    }

    /**
     * Transmite el estado actual del juego a todos los clientes a traves de UDP (solo host).
     * Se llama en cada tick del GameServer.
     */
    public void broadcastUdpGameState() {
        if (!isHost || currentGameState == null) return;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(currentGameState);
            oos.flush();
            byte[] data = baos.toByteArray();

            synchronized (clientConnections) {
                for (ClientConnection conn : clientConnections) {
                    DatagramPacket packet = new DatagramPacket(
                        data, data.length,
                        conn.getClientAddress(),
                        conn.getClientUdpPort()
                    );
                    udpSocket.send(packet);
                }
            }
        } catch (IOException e) {
            Gdx.app.error("NetworkManager", "Error al transmitir estado UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envia el estado de input del jugador local al servidor a traves de UDP (solo cliente).
     * @param inputState El objeto InputState con el estado actual de los inputs del jugador.
     */
    public void sendInputState(InputState inputState) {
        if (isHost) return; // El host no envía inputs a sí mismo
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(inputState);
            oos.flush();
            byte[] data = baos.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverUdpPort);
            udpSocket.send(packet);
        } catch (IOException e) {
            Gdx.app.error("NetworkManager", "Error al enviar input UDP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el estado actual del juego recibido del servidor.
     * @return El objeto GameState actual.
     */
    public GameState getCurrentGameState() {
        return currentGameState;
    }

    /**
     * Establece el estado actual del juego. Utilizado por el hilo UDP para actualizar el estado.
     * @param gameState El nuevo objeto GameState.
     */
    public void setCurrentGameState(GameState gameState) {
        this.currentGameState = gameState;
    }

    /**
     * Obtiene el mapa de inputs de los jugadores. Utilizado por el GameServer.
     * @return Un ConcurrentHashMap de IDs de jugador a InputState.
     */
    public ConcurrentHashMap<Integer, InputState> getPlayerInputs() {
        return playerInputs;
    }

    /**
     * Verifica si esta instancia es el host del juego.
     * @return true si es el host, false en caso contrario.
     */
    public boolean isHost() {
        return isHost;
    }

    /**
     * Obtiene el mapa de personajes seleccionados por los jugadores.
     * @return Un ConcurrentHashMap de IDs de jugador a tipos de personaje.
     */
    public ConcurrentHashMap<Integer, String> getSelectedCharacters() {
        return selectedCharacters;
    }

    /**
     * Libera todos los recursos de red y detiene los hilos.
     * Se llama al cerrar la aplicacion.
     */
    public void dispose() {
        if (isHost) {
            if (gameServer != null) gameServer.stop();
            if (hostDiscoveryThread != null) hostDiscoveryThread.interrupt();
            broadcastTcpMessage(new ShutdownPacket()); // Notificar a los clientes antes de cerrar
            try {
                if (serverTcpSocket != null) serverTcpSocket.close();
            } catch (IOException e) {
                Gdx.app.error("NetworkManager", "Error al cerrar serverTcpSocket: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (clientTcpReceiveThread != null) clientTcpReceiveThread.interrupt();
            try {
                if (clientTcpSocket != null) clientTcpSocket.close();
            } catch (IOException e) {
                Gdx.app.error("NetworkManager", "Error al cerrar clientTcpSocket: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (udpSocket != null) udpSocket.close();
        if (udpReceiveThread != null) udpReceiveThread.interrupt();
        Gdx.app.log("NetworkManager", "Recursos de red liberados.");
    }

    /**
     * Obtiene el tipo de personaje seleccionado por el jugador local.
     * @return El tipo de personaje como String.
     */
    public String getSelectedCharacterType() {
        return selectedCharacterType;
    }

    /**
     * Clase interna que representa la conexion de un cliente individual en el lado del host.
     * Maneja la comunicacion TCP con ese cliente.
     */
    private class ClientConnection implements Runnable {
        private final Socket tcpSocket; /** Socket TCP para la comunicacion con este cliente. */
        private final int playerId; /** ID del jugador asociado a esta conexión. */
        private ObjectOutputStream tcpOut; /** Stream de salida TCP para enviar mensajes al cliente. */
        private InetAddress clientAddress; /** Direccion IP del cliente. */
        private int clientUdpPort; /** Puerto UDP del cliente para enviar GameStates. */

        /**
         * Constructor de ClientConnection.
         * @param socket El socket TCP conectado al cliente.
         * @param playerId El ID asignado a este cliente.
         */
        ClientConnection(Socket socket, int playerId) {
            this.tcpSocket = socket;
            this.playerId = playerId;
            this.clientAddress = socket.getInetAddress();
        }

        /**
         * Metodo principal del hilo de ClientConnection.
         * Lee mensajes TCP del cliente y los procesa.
         */
        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(tcpSocket.getInputStream())) {
                this.tcpOut = new ObjectOutputStream(tcpSocket.getOutputStream());

                // Recibir el puerto UDP del cliente
                this.clientUdpPort = in.readInt();

                // Enviar el ID de jugador y el puerto UDP del servidor al cliente
                tcpOut.writeInt(playerId);
                tcpOut.writeInt(udpSocket.getLocalPort());
                tcpOut.flush();

                // Enviar al nuevo cliente el estado actual de los personajes ya seleccionados
                for (java.util.Map.Entry<String, Boolean> entry : game.selectedCharacters.entrySet()) {
                    if (entry.getValue()) {
                        sendTcpMessage(new CharacterTakenPacket(entry.getKey(), true));
                    }
                }

                // Bucle principal de escucha de mensajes TCP del cliente
                while (!tcpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    Object msg = in.readObject();
                    if (msg instanceof CharacterSelectionPacket) {
                        CharacterSelectionPacket packet = (CharacterSelectionPacket) msg;
                        // Procesar la selección de personaje del cliente y reenviarla a todos
                        processCharacterSelection(packet.playerId, packet.characterType);
                        sendTcpMessage(packet); // Reenviar al propio cliente para confirmación
                    }
                    Thread.sleep(100); // Pequeña pausa para evitar busy-waiting
                }

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                Gdx.app.error("NetworkManager", "Error en ClientConnection para jugador " + playerId + ": " + e.getMessage());
                // Desconexion del cliente (se manejara en el bloque finally)
            } finally {
                // Eliminar la conexión de la lista y cerrar el socket
                clientConnections.remove(this);
                try {
                    if (!tcpSocket.isClosed()) tcpSocket.close();
                } catch (IOException e) {
                    Gdx.app.error("NetworkManager", "Error al cerrar socket de cliente " + playerId + ": " + e.getMessage());
                    e.printStackTrace();
                }
                Gdx.app.log("NetworkManager", "Cliente ID " + playerId + " desconectado.");
            }
        }

        /**
         * Envia un mensaje TCP a este cliente especifico.
         * @param message El objeto Serializable a enviar.
         */
        void sendTcpMessage(Object message) {
            try {
                if (tcpOut != null) {
                    tcpOut.writeObject(message);
                    tcpOut.flush();
                }
            } catch (IOException e) {
                Gdx.app.error("NetworkManager", "Error al enviar mensaje TCP a cliente " + playerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        /**
         * Obtiene la direccion IP del cliente.
         * @return La direccion IP del cliente.
         */
        public InetAddress getClientAddress() {
            return clientAddress;
        }

        /**
         * Obtiene el puerto UDP del cliente.
         * @return El puerto UDP del cliente.
         */
        public int getClientUdpPort() {
            return clientUdpPort;
        }
    }
}
