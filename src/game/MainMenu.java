package game;

import ui.GridConstraints;
import ui.MyGridLayout;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;

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
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());

            new Thread(()->{
                try {
                    new Server().open(ipField.getText(),Integer.parseInt(portField.getText()));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
            try {
                startGame(frame, gamePanel, connect(ip, port));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        });
        join.addActionListener((e)-> {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            Socket socket = null;
            try {
                startGame(frame, gamePanel, connect(ip, port));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        });
        frame.add(mainMenuPanel);
    }

    private static Socket connect(String ip, int port) throws IOException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Socket(ip, port);

    }

    public static void startGame(JFrame frame, GamePanel gamePanel, Socket connection){
        frame.getContentPane().remove(0);
        frame.add(gamePanel);
        frame.pack();
        gamePanel.requestFocus();
        gamePanel.addListeners();
        gamePanel.startGame(connection);
    }

}
