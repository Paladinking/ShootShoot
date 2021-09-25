package game.client;

import game.events.GameEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Queue;

public class GameClient {

    private final String ip;

    private final int port;

    private DataInputStream in;
    private DataOutputStream out;

    private Socket socket;


    public GameClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    public void connect() throws IOException {
        socket = new Socket(ip, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }


    public int readInt() throws IOException {
        return in.readInt();
    }

    public void receiveData(int totalPlayers, GameEvent[][] events) throws IOException {
        for (int i = 0; i < totalPlayers; i++) {
            int source = in.readInt();
            int totalEvents = in.readInt();
            events[source] = new GameEvent[totalEvents];
            for (int j = 0; j < totalEvents; j++){
                events[source][j] = GameEvent.read(in);
            }
        }
    }

    public void sendData(Queue<GameEvent> events) throws IOException {
        int totalEvents = events.size();
        out.writeInt(totalEvents);
        while (!events.isEmpty()) {
            GameEvent e = events.remove();
            e.write(out);
        }
        out.flush();

    }
}
