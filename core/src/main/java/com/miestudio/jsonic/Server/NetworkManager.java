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

    private final JuegoSonic game;
    private ServerSocket serverTcpSocket;
    private DatagramSocket udpSocket;
    private final List<ClientConnection> clientConnections = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);
    private volatile GameState currentGameState;
    private Socket clientTcpSocket;
    private ObjectOutputStream clientTcpOut;
    private ObjectInputStream clientTcpIn;
    private InetAddress serverAddress;
    private int serverUdpPort;

    private final ConcurrentHashMap<Integer, InputState> playerInputs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> selectedCharacters = new ConcurrentHashMap<>();
    private volatile boolean isHost = false;
    private int localPlayerId = -1;
    private String selectedCharacterType;

    private Thread hostDiscoveryThread;
    private Thread clientTcpReceiveThread;
    private Thread udpReceiveThread;

    private GameServer gameServer;

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
            Gdx.app.postRunnable(() -> game.setScreen(new CharacterSelectionScreen(game, game.selectedCharacters)));
        });
        networkThread.start();
    }

    public void sendCharacterSelection(String characterType) {
        this.selectedCharacterType = characterType;
        if (!isHost) {
            sendTcpMessageToServer(new CharacterSelectionPacket(localPlayerId, characterType));
        } else {
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

    public void initializeGameServer(TiledMap map, CollisionManager collisionManager, float mapWidth, float mapHeight) {
        if (gameServer == null) {
            gameServer = new GameServer(game, playerInputs, map, collisionManager, mapWidth, mapHeight);
            gameServer.start();
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
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
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

    private void startUdpListener() {
        try {
            udpSocket.setSoTimeout(1000); // Timeout de 1 segundo
        } catch (SocketException e) {
            Gdx.app.error("NetworkManager", "Error al configurar timeout UDP: " + e.getMessage());
        }

        udpReceiveThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            Gdx.app.log("NetworkManager", "Iniciando escucha UDP...");

            while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Preparar paquete para recepción
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet); // Espera hasta recibir datos (o timeout)

                    // Procesar paquete recibido
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
                            Gdx.app.log("NetworkManager",
                                "Input recibido - Jugador: " + input.getPlayerId() +
                                    ", Izq: " + input.isLeft() +
                                    ", Der: " + input.isRight() +
                                    ", Salto: " + input.isUp()
                            );

                            // Guardar input para procesamiento en el game loop
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

                            // Actualizar estado actual del juego
                            currentGameState = gameState;
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout esperado, continuar
                } catch (SocketException e) {
                    if (!udpSocket.isClosed()) {
                    }
                    break; // Salir si el socket se cerró
                } catch (IOException e) {
                } catch (ClassNotFoundException e) {
                }
            }
        });

        udpReceiveThread.setDaemon(true);
        udpReceiveThread.setName("UDP-Listener");
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

    /**
     * Transmite el estado actual del juego a todos los clientes (solo host)
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

    public void setCurrentGameState(GameState gameState) {
        this.currentGameState = gameState;
    }

    public ConcurrentHashMap<Integer, InputState> getPlayerInputs() {
        return playerInputs;
    }

    public boolean isHost() {
        return isHost;
    }

    public ConcurrentHashMap<Integer, String> getSelectedCharacters() {
        return selectedCharacters;
    }

    public void dispose() {
        if (isHost) {
            if (gameServer != null) gameServer.stop();
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

                for (java.util.Map.Entry<String, Boolean> entry : game.selectedCharacters.entrySet()) {
                    if (entry.getValue()) {
                        sendTcpMessage(new CharacterTakenPacket(entry.getKey(), true));
                    }
                }

                while (!tcpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    Object msg = in.readObject();
                    if (msg instanceof CharacterSelectionPacket) {
                        CharacterSelectionPacket packet = (CharacterSelectionPacket) msg;
                        processCharacterSelection(packet.playerId, packet.characterType);
                        sendTcpMessage(packet);
                    }
                    Thread.sleep(100);
                }

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                // Desconexión
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
