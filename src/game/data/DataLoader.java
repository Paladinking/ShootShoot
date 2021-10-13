package game.data;

import game.Game;
import game.tiles.Level;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DataLoader {

    private static final String levelPath = "images/";

    private final static String[] LEVEL_IMAGE_NAMES = new String[]{"stage.png", "stage2.png"};

    private final Level[] levels = new Level[LEVEL_IMAGE_NAMES.length];

    public void readData() throws IOException {
        for (int i = 0; i < LEVEL_IMAGE_NAMES.length; i++) {
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResource(levelPath + LEVEL_IMAGE_NAMES[i]));
            if (image.getWidth() != Game.WIDTH || image.getHeight() != Game.HEIGHT) throw new IOException("Bad level size");
            levels[i] = new Level(image);
        }
    }
    public int getLevelWidth() {
        return Game.WIDTH;
    }

    public int getLevelHeight() {
        return Game.HEIGHT;
    }

    public Level[] getLevels() {
        return levels;
    }
}
