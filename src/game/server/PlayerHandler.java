package game.server;

import game.events.GameEvent;
import game.events.ServerEvent;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerHandler {

    private final ReentrantLock queLock = new ReentrantLock();

    private static int bulletIndex = 0;

    private final DataOutputStream out;
    private final DataInputStream in;

    private final Queue<GameEvent> events;
    private final Queue<GameEvent> selfEvents;

    private final int number;

    private final Server server;

    private volatile boolean running;

    private Thread listenerThread;

    public PlayerHandler(Socket socket, int number, Server server) throws IOException {
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.events = new ArrayDeque<>();
        this.selfEvents = new ArrayDeque<>();
        this.number = number;
        this.server = server;
    }

    public Lock getLock() {
        return queLock;
    }

    public void start() {
        running = true;
        listenerThread = new Thread(this::readEvents);
        listenerThread.start();
    }

    private void readEvents() {
        try {
            while (running) {
                int totalEvents = in.readInt();
                for (int i = 0; i < totalEvents; i++) {
                    GameEvent e = GameEvent.read(in);
                    e.handle(this);
                    queLock.lock();
                    try {
                        events.add(e);
                        if (e.propagateBack) selfEvents.add(e);
                    } finally {
                        queLock.unlock();
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            queLock.lock();
            try {
                server.playerDisconnected(number);
            } finally {
                queLock.unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopReaderThread() {
        running = false;
    }

    public Queue<GameEvent> getEvents() {
        return events;
    }

    public void sendEvent(GameEvent event) throws IOException {
        GameEvent.write(event, out);
    }

    public void sendEvent(ServerEvent event) throws IOException {
        ServerEvent.write(event, out);
    }

    /**
     * Sends all events contained in <code>selfEvents</code> to the client of this <code>PlayerHandler</code>.
     * The <code>selfEvents</code> queue will be empty when this function returns. Should only be called
     * after acquiring the eventLock of <code>this</code>.
     * @throws IOException If an IOException occurs.
     */
    public void sendSelfEvents() throws IOException{
        int totalEvents = selfEvents.size();
        out.writeInt(totalEvents);
        for (int i = 0; i < totalEvents; i++){
            GameEvent e = selfEvents.remove();
            GameEvent.write(e, out);
        }
    }

    public void sendInitialData(int players, int[] startingPositions, int level) throws IOException {
        out.writeInt(level);
        out.writeInt(players);
        out.writeInt(number);
        for (int i = 0; i < players; i++) {
            out.writeInt(startingPositions[2 * i]);
            out.writeInt(startingPositions[2 * i + 1]);
        }
        out.flush();
    }

    public void sendInt(int i) throws IOException {
        out.writeInt(i);
    }

    public synchronized static int getBulletIndex() {
        bulletIndex++;
        return bulletIndex;
    }

    public void died() {
        server.playerDied();
    }

    public void joinListenerThread() {
        while (listenerThread.isAlive()) {
            try {
                listenerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        events.clear();
    }


    public int getNumber() {
        return number;
    }
}
