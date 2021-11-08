package game.client;

import game.Game;
import game.events.GameEvent;
import game.events.ServerEvent;
import game.listeners.GameObjectHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

public class GameClient {

    private final String ip;

    private final int port;

    private final Object eventLock = new Object();

    private boolean receivingData;

    private final Queue<GameEvent> events;

    private DataInputStream in;
    private DataOutputStream out;

    private Socket socket;

    private final Game game;

    private int totalPlayers;

    private Thread readerThread;

    public GameClient(String ip, int port, Game game) {
        this.ip = ip;
        this.port = port;
        this.events = new ArrayDeque<>();
        this.game = game;
    }


    public void connect() throws IOException {
        socket = new Socket(ip, port);
        socket.setTcpNoDelay(true);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void init() {
        try {
            int level = in.readInt();
            boolean friendlyFire = in.readBoolean();
            totalPlayers = in.readInt();
            int playerNumber = in.readInt();
            for (int i = 0; i < totalPlayers; i++) {
                int x = in.readInt(), y = in.readInt();
                game.createPlayer(x, y, i, i == playerNumber);
            }
            game.setOptions(level, friendlyFire);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void receiveData(GameObjectHandler handler) throws IOException {
        for (int i = 0; i < totalPlayers; i++) {
            int source = in.readInt();
            int totalEvents = in.readInt();
            for (int j = 0; j < totalEvents; j++) {
                GameEvent event = GameEvent.read(in);
                event.source = source;
                handler.addEvent(event);
            }
        }
        int totalEvents = in.readInt();
        for (int i = 0; i < totalEvents; i++) {
            ServerEvent event = ServerEvent.read(in);
            handler.addEvent(event);
            event.executeImmediate(this);
        }
    }

    public void sendData() throws IOException {
        synchronized (eventLock) {
            int totalEvents = events.size();
            out.writeInt(totalEvents);
            while (!events.isEmpty()) {
                GameEvent e = events.remove();
                GameEvent.write(e, out);
            }
        }
        out.flush();
    }

    public void addEvent(GameEvent event) {
        synchronized (eventLock) {
            events.add(event);
        }
    }

    public void startReaderThread() {
        receivingData = true;
        readerThread = new Thread(this::readData);
        readerThread.start();
    }

    private void readData() {
        try {
            while (receivingData) {
                receiveData(game);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopReaderThread() {
        receivingData = false;
    }

    public void joinReaderThread() {
        while (readerThread.isAlive()) {
            try {
                readerThread.join();
            } catch (InterruptedException ignored) {

            }
        }
    }
}
