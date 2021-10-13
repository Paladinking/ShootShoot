package game.server;

import game.events.GameEvent;
import game.events.ServerEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class Server {

    private volatile boolean ready = false;

    private static final int PLAYERS = 1, TIMEOUT_MILLIS = 5000;

    private final int[] startingPositions = new int[]{100, 100, 1820, 100, 100, 900, 1820, 900};

    private final List<PlayerHandler> playerHandlers = new ArrayList<>();

    private final Queue<ServerEvent> serverEvents = new ArrayDeque<>();

    private volatile int livingPlayers, level;

    private volatile boolean restart = false;

    public Server() {
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void open(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        ready = true;
        synchronized (this) {
            notify();
        }
        while (playerHandlers.size() < PLAYERS) {
            Socket socket = serverSocket.accept();
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(TIMEOUT_MILLIS);
            playerHandlers.add(new PlayerHandler(socket, playerHandlers.size(), this));
            System.out.println(socket.getInetAddress() + " connected!");
        }
        livingPlayers = playerHandlers.size();
        for (PlayerHandler handler : playerHandlers) {
            handler.sendInitialData(PLAYERS, startingPositions, level);
            handler.start();
        }
        start();

    }

    public void waitForReady() {
        while (!ready) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    private void start() {
        boolean running = true;
        while (running) {
            try {
                for (PlayerHandler player : playerHandlers) {
                    int number = player.getNumber();
                    for (PlayerHandler handler : playerHandlers) {
                        handler.sendInt(number);
                    }
                    Lock lock = player.getLock();
                    lock.lock();
                    try {
                        Queue<GameEvent> events = player.getEvents();
                        int totalEvents = events.size();
                        // For every playerHandler that is not player:
                        for (int i = 1; i <= playerHandlers.size() - 1; i++){
                            PlayerHandler handler = playerHandlers.get((number + i) % playerHandlers.size());
                            handler.sendInt(totalEvents);
                        }
                        for (int j = 0; j < totalEvents; j++) {
                            GameEvent e = events.remove();
                            for (int i = 1; i <= playerHandlers.size() - 1; i++){
                                PlayerHandler handler = playerHandlers.get((number + i) % playerHandlers.size());
                                handler.sendEvent(e);
                            }
                        }
                        player.sendSelfEvents();
                    } finally {
                        lock.unlock();
                    }
                }
                synchronized (this) {
                    for (PlayerHandler handler : playerHandlers) {
                        handler.sendInt(serverEvents.size());
                    }
                    while (!serverEvents.isEmpty()) {
                        ServerEvent event = serverEvents.remove();
                        for (PlayerHandler handler : playerHandlers) {
                            handler.sendEvent(event);
                        }

                    }
                    if (restart) {
                        for (PlayerHandler handler : playerHandlers) {
                            handler.joinListenerThread();
                        }
                        restart = false;
                        livingPlayers = playerHandlers.size();
                        serverEvents.clear();
                        for (PlayerHandler handler : playerHandlers) {
                            handler.sendInitialData(playerHandlers.size(), startingPositions, level);
                            handler.start();
                        }

                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                running = false;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void playerDied() {
        synchronized (this) {
            livingPlayers--;
            if (livingPlayers == 1 || PLAYERS == 1) {
                serverEvents.add(new ServerEvent.NewGame());
                restart = true;
            }
        }

    }

    public void playerDisconnected(int number) {

    }
}
