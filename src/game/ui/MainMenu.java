package game.ui;

import game.Game;
import game.data.DataLoader;
import game.server.Server;
import game.sound.Sound;
import game.tiles.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class MainMenu {

    private JFrame frame;

    private JPanel mainMenuPanel;

    private JComponent waitingComponent;

    private LevelSelector levelSelector;

    private GamePanel gamePanel;

    private OptionsMenu optionsMenu;

    private JButton host, join, options;

    public MainMenu() {

    }

    public void init(Dimension screenSize) {
        frame = new JFrame("Skjuta Skjuta");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        host = new JButton("Host");
        join = new JButton("Join");
        options = new JButton("Options");
        mainMenuPanel = getMainMenuPanel(screenSize);
        DataLoader loader = new DataLoader();
        try {
            loader.readData();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Level[] levels = loader.getLevels();
        Sound[] sounds = loader.getSounds();
        int levelWidth = loader.getLevelWidth(), levelHeight = loader.getLevelHeight();
        Game game = new Game(levelWidth, levelHeight, screenSize.width, screenSize.height, levels, sounds);
        levelSelector = new LevelSelector(levelWidth, levelHeight, levels);
        levelSelector.setPreferredSize(screenSize);
        optionsMenu = new OptionsMenu();
        optionsMenu.loadOptions();
        optionsMenu.setPreferredSize(screenSize);
        optionsMenu.initOptionsComponents(this);
        waitingComponent = new JComponent() {
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                String s = "Waiting for players...";
                g.setFont(new Font("Dialog", Font.PLAIN, 80));
                int stringWidth = g.getFontMetrics().stringWidth(s);
                g.drawString(s, getWidth() / 2 - stringWidth/2, getHeight()/2);
            }
        };
        waitingComponent.setPreferredSize(screenSize);
        gamePanel = new GamePanel(game);
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
        join.setEnabled(false);
        host.setEnabled(false);
        String ip = "127.0.0.1";
        int port = optionsMenu.getPort();
        int players = optionsMenu.getPlayers();
        frame.getContentPane().remove(0);
        frame.add(levelSelector);
        frame.validate();
        new Thread(() -> {
            int level = levelSelector.selectLevel();
            boolean friendlyFire = optionsMenu.getFriendlyFire();
            Server server = new Server(players);
            server.setOptions(level, friendlyFire);
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
        join.setEnabled(false);
        host.setEnabled(false);
        String ip = optionsMenu.getIp();
        int port = optionsMenu.getPort();
        new Thread(()-> startGame(ip, port)).start();
    }

    private JPanel getMainMenuPanel(Dimension screenSize) {
        JPanel mainMenuPanel = new JPanel(new MyGridLayout(3, 40));
        mainMenuPanel.setPreferredSize(screenSize);
        mainMenuPanel.add(host, new GridConstraints(1, 14 , 1, 2));
        mainMenuPanel.add(join, new GridConstraints(1, 17, 1, 2));
        mainMenuPanel.add(options, new GridConstraints(1, 20, 1, 2));
        mainMenuPanel.setFocusable(true);
        mainMenuPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
            }
        });
        host.addActionListener(this::host);
        join.addActionListener(this::join);
        options.addActionListener(this::options);
        return mainMenuPanel;
    }

    private void options(ActionEvent actionEvent) {
        frame.getContentPane().remove(0);
        frame.add(optionsMenu);
        frame.validate();
        frame.repaint();
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
        EventQueue.invokeLater(() -> {
            frame.getContentPane().remove(0);
            frame.add(waitingComponent);
            frame.validate();
        });
        gamePanel.startGame(ip, port);
        EventQueue.invokeLater(() -> {
            frame.getContentPane().remove(0);
            frame.add(gamePanel);
            frame.validate();
            gamePanel.requestFocus();
            gamePanel.addListeners();
        });
    }

    public void closeOptionsMenu() {
        frame.getContentPane().remove(0);
        frame.add(mainMenuPanel);
        frame.validate();
        frame.repaint();
    }
}
