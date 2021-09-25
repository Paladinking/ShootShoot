package game.ui;

import game.Game;
import game.client.GameClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class GamePanel extends JPanel {

    private final Game game;

    public GamePanel(Game game){
        this.game = game;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        game.draw((Graphics2D) g, getSize());
    }

    public void addListeners() {
        addKeyListener(game);
        addMouseMotionListener(game);
    }

    public void startGame(String ip, int port) {
        try {
            game.start(this, ip, port);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
