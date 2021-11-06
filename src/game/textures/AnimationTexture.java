package game.textures;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AnimationTexture extends Texture {
    private final double increment;

    private final BufferedImage[] images;

    private double currentFrame;

    private final Vector2d position;

    private boolean dead;

    private final int width, height;

    public AnimationTexture(Vector2d position, int width, int height, int frameDuration, BufferedImage[] images) {
        this.increment =  1.0 / frameDuration * images.length;
        this.images = images;
        this.dead = false;
        this.position = position;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage image = images[(int) currentFrame];
        g.drawImage(image, (int) position.x - width / 2, (int) position.y - height / 2, width, height, null);
    }

    public void remove(){
        dead = true;
    }

    @Override
    public boolean tick() {
        currentFrame += increment;
        if ((int) currentFrame == images.length) currentFrame = 0;
        return dead;
    }
}
