package game.ui;

import game.Game;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GamePanel extends JPanel {

    private final Game game;

    public GamePanel(Game game){
        this.game = game;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        game.draw((Graphics2D) g);
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
