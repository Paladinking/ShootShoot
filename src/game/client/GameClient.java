package game.client;

import game.events.GameEvent;
import game.events.ServerEvent;
import game.listeners.GameEventHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

public class GameClient implements GameEventHandler {

    private final String ip;

    private final int port;

    private final Queue<GameEvent> events;

    private DataInputStream in;
    private DataOutputStream out;

    private Socket socket;


    public GameClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.events = new ArrayDeque<>();
    }


    public void connect() throws IOException {
        socket = new Socket(ip, port);
        socket.setTcpNoDelay(true);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }


    public int readInt() throws IOException {
        return in.readInt();
    }

    public void receiveData(int totalPlayers, GameEventHandler handler) throws IOException {
        for (int i = 0; i < totalPlayers; i++) {
            int source = in.readInt();
            int totalEvents = in.readInt();
            for (int j = 0; j < totalEvents; j++){
                GameEvent event = GameEvent.read(in);
                event.source = source;
                handler.addEvent(event);
            }
        }
        int totalEvents = in.readInt();
        for (int i = 0; i < totalEvents; i++){
            ServerEvent event = ServerEvent.read(in);
            handler.addEvent(event);
            handler.execImmediate(event);
        }
    }

    public void sendData() throws IOException {
        int totalEvents = events.size();
        out.writeInt(totalEvents);
        while (!events.isEmpty()) {
            GameEvent e = events.remove();
            e.write(out);
        }
        out.flush();
    }

    public void addEvent(GameEvent event) {
        events.add(event);
    }
}
