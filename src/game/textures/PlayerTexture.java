package game.textures;

import game.entities.Player;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class PlayerTexture extends Texture {

    private double angle;

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public enum State {
        NORMAL, HURT
    }

    private final static List<Map<State, BufferedImage>> PLAYER_IMAGES = new ArrayList<>(4);
    private final Map<State, BufferedImage> images;

    private BufferedImage image;

    static {
        try {
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResource("images/players.png"));
            for (int i = 0; i < 4; i++){
                Map<State, BufferedImage> images = new EnumMap<>(State.class);
                images.put(State.NORMAL, image.getSubimage(0, 40 * i, 40, 40));
                images.put(State.HURT, image.getSubimage(40, 40 * i, 40, 40));
                PLAYER_IMAGES.add(images);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private final Player player;

    public PlayerTexture(Player player, int number) {
        this.player = player;
        this.images = PLAYER_IMAGES.get(number);
        this.setState(State.NORMAL);
    }

    @Override
    public void draw(Graphics2D g) {
        Vector2d position = player.getPosition();
        g.translate(position.x, position.y);
        g.rotate(angle);
        int radius = player.getRadius();
        g.drawImage(image, -radius, -radius, radius * 2, radius * 2, null);
        g.rotate(-angle);
        g.translate(-position.x, -position.y);
    }

    public void setState(State state){
        this.image = images.get(state);
    }

    @Override
    public boolean tick() {
        return player.isDead();
    }
}
