package game.textures;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class Texture {

    public abstract void draw(Graphics2D g);

    public abstract boolean tick();

    private static final int TOTAL_EXPLOSIONS = 40, EXPLOSION_WIDTH = 256, EXPLOSION_HEIGHT = 248;

    private static final BufferedImage[] explosion = new BufferedImage[TOTAL_EXPLOSIONS];

    static {
        try {
            BufferedImage explosions = ImageIO.read(ClassLoader.getSystemResource("images/explosion.png"));
            for (int i = 0; i < TOTAL_EXPLOSIONS; i++){
                explosion[i] = explosions.getSubimage((i % 8) * EXPLOSION_WIDTH, (i / 8) * EXPLOSION_HEIGHT, EXPLOSION_WIDTH, EXPLOSION_HEIGHT);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    public static AnimationTexture explosionTexture(Vector2d pos, int radius, int totalDuration) {
        return new AnimationTexture(pos, radius * 2, radius * 2, totalDuration, explosion);
    }
}
