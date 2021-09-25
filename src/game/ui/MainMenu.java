package game.ui;

import game.Game;
import game.server.Server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainMenu {

    public static void main(String[] args) {
        Game game = new Game(Game.WIDTH, Game.HEIGHT);
        game.init();
        EventQueue.invokeLater(()->{
            JFrame frame = new JFrame("Skjuta Skjuta");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            GamePanel gamePanel = new GamePanel(game);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            gamePanel.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());

            addMainMenuPanel(frame, gamePanel);
            frame.pack();
            frame.setVisible(true);
        });
    }

    public static void addMainMenuPanel(JFrame frame, GamePanel gamePanel){
        JPanel mainMenuPanel = new JPanel(new MyGridLayout(3, 40));
        mainMenuPanel.setPreferredSize(gamePanel.getPreferredSize());
        JTextField ipField = new JTextField("127.0.0.1");
        JTextField portField = new JTextField("6066");
        mainMenuPanel.add(ipField, new GridConstraints(1, 14 + 2));
        mainMenuPanel.add(portField, new GridConstraints(1, 14+ 4));
        JButton host = new JButton("Host");
        JButton join = new JButton("Join");
        mainMenuPanel.add(host, new GridConstraints(1, 14+ 6, 1, 2));
        mainMenuPanel.add(join, new GridConstraints(1, 17+ 6, 1, 2));
        host.addActionListener((e)->{
            String ip = "127.0.0.1";
            int port = Integer.parseInt(portField.getText());
            Server server = new Server();
            new Thread(()-> {
                try {
                    server.open(port);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
            server.waitForReady();
            startGame(frame, gamePanel, ip, port);

        });
        join.addActionListener((e)-> {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            startGame(frame, gamePanel, ip, port);
        });
        frame.add(mainMenuPanel);
    }

    public static void startGame(JFrame frame, GamePanel gamePanel, String ip, int port){
        frame.getContentPane().remove(0);
        frame.add(gamePanel);
        frame.pack();
        gamePanel.requestFocus();
        gamePanel.addListeners();
        gamePanel.startGame(ip, port);
    }

}
