package game;

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

    public void startGame(Socket connection) {


        try {
            game.start(this, connection.getInputStream(), connection.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
