
package com.miestudio.jsonic.Server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.miestudio.jsonic.JuegoSonic;
import com.miestudio.jsonic.Pantallas.CharacterSelectionScreen;
import com.miestudio.jsonic.Pantallas.GameScreen;
import com.miestudio.jsonic.Pantallas.LobbyScreen;
import com.miestudio.jsonic.Util.Constantes;
import com.miestudio.jsonic.Util.GameState;
import com.miestudio.jsonic.Util.InputState;
import com.miestudio.jsonic.Util.ShutdownPacket;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.miestudio.jsonic.Util.CollisionManager;
import com.miestudio.jsonic.Sistemas.PollutionSystem;

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

    private final JuegoSonic game;
    private ServerSocket serverTcpSocket;
    private DatagramSocket udpSocket;
    private final List<ClientConnection> clientConnections = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private volatile GameState currentGameState;
    private Socket clientTcpSocket;
    private ObjectOutputStream clientTcpOut; // Añadido
    private ObjectInputStream clientTcpIn; // Añadido
    private InetAddress serverAddress;
    private int serverUdpPort;

    private final ConcurrentHashMap<Integer, InputState> playerInputs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> selectedCharacters = new ConcurrentHashMap<>(); // Nuevo: Almacena el personaje seleccionado por cada jugador
    private volatile boolean isHost = false;
    private int localPlayerId = -1; // Almacena el ID del jugador local
    private String selectedCharacterType; // Nuevo: Almacena el tipo de personaje seleccionado

    private Thread hostDiscoveryThread;
    private Thread clientTcpReceiveThread;
    private Thread udpReceiveThread;

    private GameServer gameServer; // Referencia al GameServer

    public NetworkManager(JuegoSonic game) {
        this.game = game;
    }

    public void checkNetworkStatus() {
        Thread networkThread = new Thread(() -> {
            String serverIp = discoverServer();
            if (serverIp != null) {
                connectAsClient(serverIp, Constantes.GAME_PORT);
            } else {
                startHost();
            }
            Gdx.app.postRunnable(() -> game.setScreen(new CharacterSelectionScreen(game)));
        });
        networkThread.start();
    }

    public void sendCharacterSelection(String characterType) {
        this.selectedCharacterType = characterType; // Almacenar la selección localmente
        if (!isHost) { // Si es cliente, enviar al servidor
            sendTcpMessageToServer(new com.miestudio.jsonic.Server.network.CharacterSelectionPacket(localPlayerId, characterType));
        } else { // Si es host, procesar localmente
            processCharacterSelection(localPlayerId, characterType);
        }
    }

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

    private void processCharacterSelection(int playerId, String characterType) {
        // Lógica para verificar si el personaje ya está tomado
        if (selectedCharacters.containsValue(characterType)) {
            Gdx.app.log("NetworkManager", "Personaje " + characterType + " ya seleccionado.");
            // Enviar mensaje de rechazo al cliente (futuro)
            return;
        }
        selectedCharacters.put(playerId, characterType);
        Gdx.app.log("NetworkManager", "Jugador " + playerId + " seleccionó " + characterType);

        // Notificar a todos los clientes sobre la selección (futuro)
        // Por ahora, simplemente pasar a la LobbyScreen
        Gdx.app.postRunnable(() -> game.setScreen(new LobbyScreen(game, getColorForCharacter(characterType), isHost)));
    }

    private Color getColorForCharacter(String characterType) {
        if ("Sonic".equals(characterType)) {
            return Color.BLUE;
        } else if ("Tails".equals(characterType)) {
            return Color.YELLOW;
        } else if ("Knuckles".equals(characterType)) {
            return Color.RED;
        } else {
            return Color.GRAY; // Color por defecto
        }
    }

    private String discoverServer() {
        try (DatagramSocket discoverySocket = new DatagramSocket(Constantes.DISCOVERY_PORT)) {
            discoverySocket.setSoTimeout(2000);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            Gdx.app.log("NetworkManager", "Buscando servidor...");
            discoverySocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            Gdx.app.log("NetworkManager", "Servidor encontrado: " + message);
            return packet.getAddress().getHostAddress();
        } catch (SocketTimeoutException e) {
            Gdx.app.log("NetworkManager", "No se encontró servidor.");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startHost() {
        isHost = true;
        try {
            serverTcpSocket = new ServerSocket(Constantes.GAME_PORT);
            serverTcpSocket.setSoTimeout(1000); // Establecer timeout para accept()
            udpSocket = new DatagramSocket(Constantes.GAME_PORT);
            Gdx.app.log("NetworkManager", "Servidor iniciado en TCP y UDP en el puerto " + Constantes.GAME_PORT);
            localPlayerId = 0; // Host es el jugador 0

            hostDiscoveryThread = new Thread(this::announceServer);
            hostDiscoveryThread.setDaemon(true);
            hostDiscoveryThread.start();

            Thread acceptClientsThread = new Thread(this::acceptClients);
            acceptClientsThread.setDaemon(true); // Hacer que este hilo sea demonio
            acceptClientsThread.start();
            startUdpListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Nuevo método para inicializar GameServer desde GameScreen
    public void initializeGameServer(TiledMap map, CollisionManager collisionManager, PollutionSystem pollutionSystem, float mapWidth, float mapHeight) {
        if (gameServer == null) {
            gameServer = new GameServer(game, playerInputs, map, collisionManager, pollutionSystem, mapWidth, mapHeight);
            gameServer.start(); // Iniciar el bucle del juego del servidor
        }
    }

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
        } catch (IOException e) {
            // Manejar la excepción de IO, posiblemente loguear
            Gdx.app.error("NetworkManager", "Error en announceServer: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            Gdx.app.log("NetworkManager", "announceServer interrumpido.");
        } finally {
            Gdx.app.log("NetworkManager", "announceServer finalizado.");
        }
    }

    private void acceptClients() {
        while (isHost && !serverTcpSocket.isClosed()) {
            try {
                Socket clientSocket = serverTcpSocket.accept();
                if (clientConnections.size() < Constantes.MAX_PLAYERS - 1) {
                    int playerId = nextPlayerId.getAndIncrement();
                    ClientConnection connection = new ClientConnection(clientSocket, playerId);
                    clientConnections.add(connection);
                    Thread clientThread = new Thread(connection);
                    clientThread.setDaemon(true); // Hacer que este hilo sea demonio
                    clientThread.start();
                } else {
                    clientSocket.close();
                }
            } catch (SocketTimeoutException e) {
                // Timeout, continuar el bucle para comprobar el estado del host
            } catch (IOException e) {
                if (!isHost || serverTcpSocket.isClosed()) {
                    break; // Salir del bucle si el host se detiene o el socket del servidor se cierra
                } 
                e.printStackTrace();
            }
        }
    }

    public void connectAsClient(String ip, int port) {
        isHost = false;
        try {
            serverAddress = InetAddress.getByName(ip);
            clientTcpSocket = new Socket(serverAddress, port);
            udpSocket = new DatagramSocket(); // Puerto aleatorio para UDP
            try {
                udpSocket.setSoTimeout(1000); // Establecer timeout para receive()
            } catch (SocketException e) {
                e.printStackTrace();
            }

            clientTcpOut = new ObjectOutputStream(clientTcpSocket.getOutputStream());
            clientTcpIn = new ObjectInputStream(clientTcpSocket.getInputStream());

            // Enviar el puerto UDP al servidor
            clientTcpOut.writeInt(udpSocket.getLocalPort());
            clientTcpOut.flush();

            int playerId = clientTcpIn.readInt();
            serverUdpPort = clientTcpIn.readInt();
            localPlayerId = playerId;

            if (playerId != -1) {
                Gdx.app.log("NetworkManager", "Conectado como jugador " + playerId);

                startClientTcpListener(clientTcpIn, playerId);
                startUdpListener();
            } else {
                Gdx.app.log("NetworkManager", "Servidor lleno.");
                clientTcpSocket.close();
                udpSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startClientTcpListener(ObjectInputStream clientTcpIn, int playerId) {
        clientTcpReceiveThread = new Thread(() -> {
            try {
                while (!clientTcpSocket.isClosed()) {
                    Object msg = clientTcpIn.readObject();
                    if ("START_GAME".equals(msg)) {
                        Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, playerId)));
                    } else if (msg instanceof ShutdownPacket) {
                        Gdx.app.postRunnable(game::dispose);
                    } else if (msg instanceof com.miestudio.jsonic.Server.network.CharacterSelectionPacket) {
                        com.miestudio.jsonic.Server.network.CharacterSelectionPacket packet = (com.miestudio.jsonic.Server.network.CharacterSelectionPacket) msg;
                        // Aquí el cliente recibe la confirmación o rechazo de la selección
                        // Por ahora, simplemente logueamos y pasamos a LobbyScreen si es exitoso
                        Gdx.app.log("NetworkManager", "Recibido CharacterSelectionPacket para jugador " + packet.playerId + ": " + packet.characterType);
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

    private void startUdpListener() {
        try {
            udpSocket.setSoTimeout(1000); // Establecer timeout para receive()
        } catch (SocketException e) {
            e.printStackTrace();
        }
        udpReceiveThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Object obj = ois.readObject();

                    if (isHost) {
                        if (obj instanceof InputState) {
                            InputState input = (InputState) obj;
                            playerInputs.put(input.getPlayerId(), input);
                        }
                    } else {
                        if (obj instanceof GameState) {
                            currentGameState = (GameState) obj;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout, continuar el bucle para comprobar el estado del hilo
                } catch (SocketException e) {
                    break; // El socket se cerró
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        udpReceiveThread.setDaemon(true);
        udpReceiveThread.start();
    }

    public void startGame() {
        if (isHost) {
            broadcastTcpMessage("START_GAME");
            Gdx.app.postRunnable(() -> game.setScreen(new GameScreen(game, 0)));
        }
    }

    public void broadcastTcpMessage(Object message) {
        synchronized (clientConnections) {
            for (ClientConnection conn : clientConnections) {
                conn.sendTcpMessage(message);
            }
        }
    }

    public void broadcastUdpGameState(GameState gameState) {
        if (!isHost) return;
        this.currentGameState = gameServer.getCurrentGameState(); // Obtener el estado del GameServer
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(currentGameState); // Serializar el estado obtenido del GameServer
            oos.flush();
            byte[] data = baos.toByteArray();

            synchronized (clientConnections) {
                for (ClientConnection conn : clientConnections) {
                    DatagramPacket packet = new DatagramPacket(data, data.length, conn.getClientAddress(), conn.getClientUdpPort());
                    udpSocket.send(packet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInputState(InputState inputState) {
        if (isHost) return;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(inputState);
            oos.flush();
            byte[] data = baos.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverUdpPort);
            udpSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    // Nuevo método para que GameServer pueda actualizar el currentGameState
    public void setCurrentGameState(GameState gameState) {
        this.currentGameState = gameState;
    }

    public ConcurrentHashMap<Integer, InputState> getPlayerInputs() {
        return playerInputs;
    }

    public boolean isHost() {
        return isHost;
    }

    public void dispose() {
        if (isHost) {
            if (gameServer != null) gameServer.stop(); // Detener GameServer
            if (hostDiscoveryThread != null) hostDiscoveryThread.interrupt();
            broadcastTcpMessage(new ShutdownPacket());
            try {
                if (serverTcpSocket != null) serverTcpSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (clientTcpReceiveThread != null) clientTcpReceiveThread.interrupt();
            try {
                if (clientTcpSocket != null) clientTcpSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (udpSocket != null) udpSocket.close();
        if (udpReceiveThread != null) udpReceiveThread.interrupt();
    }

    private class ClientConnection implements Runnable {
        private final Socket tcpSocket;
        private final int playerId;
        private ObjectOutputStream tcpOut;
        private InetAddress clientAddress;
        private int clientUdpPort;

        ClientConnection(Socket socket, int playerId) {
            this.tcpSocket = socket;
            this.playerId = playerId;
            this.clientAddress = socket.getInetAddress();
        }

        @Override
        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(tcpSocket.getInputStream())) {
                this.tcpOut = new ObjectOutputStream(tcpSocket.getOutputStream());
                
                this.clientUdpPort = in.readInt();
                
                tcpOut.writeInt(playerId);
                tcpOut.writeInt(udpSocket.getLocalPort());
                tcpOut.flush();

                Gdx.app.log("NetworkManager", "Cliente " + playerId + " conectado desde " + clientAddress.getHostAddress() + ":" + clientUdpPort);

                // Mantener el hilo vivo para escuchar mensajes TCP si es necesario en el futuro
                while (!tcpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    Object msg = in.readObject();
                    if (msg instanceof com.miestudio.jsonic.Server.network.CharacterSelectionPacket) {
                        com.miestudio.jsonic.Server.network.CharacterSelectionPacket packet = (com.miestudio.jsonic.Server.network.CharacterSelectionPacket) msg;
                        processCharacterSelection(packet.playerId, packet.characterType);
                        // Enviar confirmación al cliente (futuro)
                        sendTcpMessage(packet); // Reenviar el paquete al cliente para confirmación
                    }
                    Thread.sleep(100); // Pequeña pausa para evitar busy-waiting
                }

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                Gdx.app.log("NetworkManager", "Cliente " + playerId + " desconectado.");
            } finally {
                clientConnections.remove(this);
                try {
                    if (!tcpSocket.isClosed()) tcpSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void sendTcpMessage(Object message) {
            try {
                if (tcpOut != null) {
                    tcpOut.writeObject(message);
                    tcpOut.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public InetAddress getClientAddress() {
            return clientAddress;
        }

        public int getClientUdpPort() {
            return clientUdpPort;
        }
    }
}
