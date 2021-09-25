package game.server;

import game.events.GameEvent;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerHandler {

    private final ReentrantLock queLock = new ReentrantLock();

    private static int bulletIndex = 0;

    private final DataOutputStream out;
    private final DataInputStream in;

    private final Queue<GameEvent> events;

    private final int number;

    public PlayerHandler(Socket socket, int number) throws IOException {
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.events = new ArrayDeque<>();
        this.number = number;
    }

    public Lock getLock(){
        return queLock;
    }

    public void start() {
        try {
            while (true) {
                readEvents();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void readEvents() throws IOException {
        int totalEvents = in.readInt();
            for (int i = 0; i < totalEvents; i++) {
                GameEvent e = GameEvent.read(in);
                if (e == null) {
                    byte[] b = in.readAllBytes();
                    System.out.println(Arrays.toString(b));
                    return;
                }
                e.handle(this);
                queLock.lock();
                try {
                    events.add(e);
                } finally {
                    queLock.unlock();
                }
            }

    }

    public Queue<GameEvent> getEvents() {
        return events;
    }

    public void sendEvent(GameEvent event) throws IOException {
        event.write(out);
    }

    public void sendInitialData(int players, int[] startingPositions) throws IOException {
        out.writeInt(players);
        for (int i = 0; i < players; i++) {
            out.writeInt(startingPositions[2 * i]);
            out.writeInt(startingPositions[2 * i + 1]);
        }
        out.writeInt(number);
        out.flush();
    }

    public void sendInt(int i)throws IOException {
        out.writeInt(i);
    }

    public synchronized static int getBulletIndex() {
        bulletIndex++;
        return bulletIndex;
    }

    public void flush() throws IOException{
        out.flush();
    }

    public int getNumber() {
        return number;
    }
}
