package game.ui;

import game.Game;
import game.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class MainMenu {

    private JFrame frame;

    private JPanel mainMenuPanel;

    private LevelSelector levelSelector;

    private GamePanel gamePanel;

    private JTextField ipField, portField;

    private JButton host, join;

    public MainMenu() {

    }

    public void init(Dimension screenSize) {
        frame = new JFrame("Skjuta Skjuta");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ipField = new JTextField("127.0.0.1");
        portField = new JTextField("6066");
        host = new JButton("Host");
        join = new JButton("Join");
        mainMenuPanel = getMainMenuPanel(screenSize);
        Game game = new Game(Game.WIDTH, Game.HEIGHT, screenSize.width, screenSize.height);
        levelSelector = new LevelSelector(Game.WIDTH, Game.HEIGHT);
        levelSelector.setPreferredSize(screenSize);
        try {
            levelSelector.readLevelImages();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        gamePanel = new GamePanel(game, levelSelector);
        gamePanel.setPreferredSize(screenSize);
    }


    public void show() {
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.add(mainMenuPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public void host(ActionEvent e) {
        String ip = "127.0.0.1";
        int port = Integer.parseInt(portField.getText());
        frame.getContentPane().remove(0);
        frame.add(levelSelector);
        frame.pack();
        new Thread(() -> {
            int level = levelSelector.selectLevel();
            Server server = new Server();
            server.setLevel(level);
            new Thread(() -> {
                try {
                    server.open(port);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
            server.waitForReady();
            startGame(ip, port);
        }).start();
    }

    public void join(ActionEvent e) {
        String ip = ipField.getText();
        int port = Integer.parseInt(portField.getText());
        new Thread(()-> startGame(ip, port)).start();
    }

    private JPanel getMainMenuPanel(Dimension screenSize) {
        JPanel mainMenuPanel = new JPanel(new MyGridLayout(3, 40));
        mainMenuPanel.setPreferredSize(screenSize);
        mainMenuPanel.add(ipField, new GridConstraints(1, 14 + 2));
        mainMenuPanel.add(portField, new GridConstraints(1, 14 + 4));
        mainMenuPanel.add(host, new GridConstraints(1, 14 + 6, 1, 2));
        mainMenuPanel.add(join, new GridConstraints(1, 17 + 6, 1, 2));
        host.addActionListener(this::host);
        join.addActionListener(this::join);
        return mainMenuPanel;
    }

    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        EventQueue.invokeLater(() -> {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            mainMenu.init(screenSize);
            mainMenu.show();
        });
    }


    public void startGame(String ip, int port) {
        gamePanel.startGame(ip, port);
        EventQueue.invokeLater(() -> {
            frame.getContentPane().remove(0);
            frame.add(gamePanel);
            frame.pack();
            gamePanel.requestFocus();
            gamePanel.addListeners();
        });
    }

}
