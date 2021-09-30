package game.textures;

import game.entities.Player;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PlayerTexture extends Texture {

    public enum State {
        NORMAL, HURT
    }

    private final static BufferedImage[] PLAYER_IMAGES;

    static {
        PLAYER_IMAGES = new BufferedImage[4];
        try {
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResource("images/Players.png"));
            for (int i = 0; i < 4; i++){
                PLAYER_IMAGES[i] = image.getSubimage(0, 40 * i, 40, 40);
            }
        } catch (IOException ignored){}
    }

    private final int number;

    private final Player player;

    public PlayerTexture(Player player, int number) {
        this.player = player;
        this.number = number;
    }

    @Override
    public void draw(Graphics2D g) {
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        Vector2d position = player.getPosition();
        double angle = Math.atan2(mousePos.y - position.y, mousePos.x - position.x);
        g.translate(position.x, position.y);
        g.rotate(angle);
        int radius = player.getRadius();
        g.drawImage(PLAYER_IMAGES[number], -radius, -radius, radius * 2, radius * 2, null);
        //g.fillOval(- radius,- radius, radius * 2, radius * 2);
        g.rotate(-angle);
        g.translate(-position.x, -position.y);
    }

    public void setState(State state){

    }

    @Override
    public boolean tick() {
        return player.isDead();
    }
}
