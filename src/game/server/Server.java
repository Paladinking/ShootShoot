package game.server;

import game.events.GameEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public class Server {

    private final Object waiter;

    private boolean ready = false;

    private static final int PLAYERS = 1;

    private final int[] startingPositions = new int[]{100, 100, 1820, 100, 100, 980, 1820, 980};

    List<PlayerHandler> playerHandlers = new ArrayList<>();

    public Server() {
        this.waiter = new Object();
    }

    public void open(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        ready = true;
        synchronized (waiter){
            waiter.notify();
        }
        while (playerHandlers.size() < PLAYERS) {
            Socket socket = serverSocket.accept();
            playerHandlers.add(new PlayerHandler(socket, playerHandlers.size()));
            System.out.println(socket.getInetAddress() + " connected!");
        }
        for (int i = 0; i < playerHandlers.size(); i++) {
            PlayerHandler handler = playerHandlers.get(i);
            handler.sendInitialData(PLAYERS, startingPositions);
            new Thread(handler::start).start();
        }
        start();

    }

    public void waitForReady(){
        while (!ready) {
            synchronized (waiter) {
                try {
                    waiter.wait();
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
                        for (PlayerHandler handler : playerHandlers){
                            handler.sendInt(totalEvents);
                        }
                        for (int j = 0; j < totalEvents; j++){
                            GameEvent e = events.remove();
                            for (PlayerHandler handler : playerHandlers) { ;
                                handler.sendEvent(e);
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
                for (PlayerHandler handler : playerHandlers){
                    handler.flush();
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


}
