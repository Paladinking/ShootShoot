package game.textures;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MineTexture extends Texture {

    private static final int TEXTURE_DIMENSION = 20;

    private final int radius, x, y;

    public enum State {
        ACTIVE, INACTIVE, TRIGGERED, EXPLODED
    }

    private final static BufferedImage[] IMAGES = new BufferedImage[State.values().length];

    static {
        try {
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResource("images/Mine.png"));
            for (int i = 0; i < State.values().length; i++){
                IMAGES[i] = image.getSubimage(i * TEXTURE_DIMENSION, 0, TEXTURE_DIMENSION, TEXTURE_DIMENSION);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private State state;

    public MineTexture(int x, int y){
        this.radius = TEXTURE_DIMENSION;
        this.x = x;
        this.y = y;
        this.state = State.INACTIVE;
    }


    @Override
    public void draw(Graphics2D g) {
        int diameter = radius * 2;
        g.drawImage(IMAGES[state.ordinal()], x - radius, y - radius, diameter, diameter, null);
    }

    public void setState(State state){
        this.state = state;
    }

    @Override
    public boolean tick() {
        return state == State.EXPLODED;
    }
}
