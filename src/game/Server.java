package game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int PLAYERS = 2;

    private final int[] startingPositions = new int[]{100, 100, 1820, 100, 100, 980, 1820, 980};

    List<PlayerHandler> playerHandlers = new ArrayList<>();

    public void open(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (playerHandlers.size() < PLAYERS){
            Socket socket = serverSocket.accept();
            playerHandlers.add(new PlayerHandler(socket));
            System.out.println(socket.getInetAddress() + " connected!");
        }
        for (int i = 0; i < playerHandlers.size(); i++){
            PlayerHandler handler = playerHandlers.get(i);
            handler.sendInitialData(i, PLAYERS, startingPositions[2 * i], startingPositions[2 * i + 1]);
            new Thread(handler::start).start();
        }
        start();

    }

    private void start() {
        while (true){
            for (int i = 0; i < playerHandlers.size(); i++){
                if (playerHandlers.get(i).hasShot)System.out.println("Player " + i + " Shot");
                byte[] data = playerHandlers.get(i).getData();
                for (PlayerHandler handler : playerHandlers){
                    try {
                        handler.sendData(data);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
