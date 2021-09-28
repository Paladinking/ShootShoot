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

    private static final int PLAYERS = 2, TIMEOUT_MILLIS = 5000;

    private final int[] startingPositions = new int[]{100, 100, 1820, 100, 100, 980, 1820, 980};

    private final List<PlayerHandler> playerHandlers = new ArrayList<>();

    private final Queue<GameEvent> serverEvents = new ArrayDeque<>();

    private int livingPlayers;

    private boolean restart = false;

    public Server() {
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
        for (int i = 0; i < playerHandlers.size(); i++) {
            PlayerHandler handler = playerHandlers.get(i);
            handler.sendInitialData(PLAYERS, startingPositions);
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
                for (int i = 0; i < playerHandlers.size(); i++) {
                    for (PlayerHandler handler : playerHandlers) {
                        handler.sendInt(i);
                    }
                    Lock lock = playerHandlers.get(i).getLock();
                    lock.lock();
                    try {
                        Queue<GameEvent> events = playerHandlers.get(i).getEvents();
                        int totalEvents = events.size();
                        for (PlayerHandler handler : playerHandlers) {
                            handler.sendInt(totalEvents);
                        }
                        for (int j = 0; j < totalEvents; j++) {
                            GameEvent e = events.remove();
                            for (PlayerHandler handler : playerHandlers) {
                                ;
                                handler.sendEvent(e);
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
                synchronized (this) {
                    for (PlayerHandler handler : playerHandlers) {
                        handler.sendInt(serverEvents.size());
                    }
                    while (!serverEvents.isEmpty()) {
                        for (GameEvent event : serverEvents) {
                            for (PlayerHandler handler : playerHandlers) {
                                handler.sendEvent(event);
                            }
                        }
                    }
                    if (restart) {
                        for (PlayerHandler handler : playerHandlers) {
                            handler.restart();
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
        livingPlayers--;
        if (livingPlayers == 0) {
            synchronized (this) {
                serverEvents.add(new ServerEvent.NewGame());
                restart = true;
            }
        }
    }

    public void playerDisconnected(int number) {

    }
}
