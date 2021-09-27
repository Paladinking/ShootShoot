package game.textures;

import game.entities.Player;

import javax.vecmath.Vector2d;
import java.awt.*;

public class PlayerTexture extends Texture {

    public enum State {
        NORMAL, HURT
    }

    private final Color[] colors;

    private Color color;

    private final Player player;

    public PlayerTexture(Player player, Color color) {
        this.player = player;
        this.colors = new Color[]{color, Color.ORANGE};
        this.color = color;
    }

    @Override
    public void draw(Graphics2D g) {
        Vector2d position = player.getPosition();
        int radius = player.getRadius();
        g.setColor(color);
        g.fillOval((int) position.x - radius, (int) position.y - radius, radius * 2, radius * 2);
    }

    public void setState(State state){
        this.color = colors[state.ordinal()];
    }

    @Override
    public boolean tick() {
        return player.isDead();
    }
}
