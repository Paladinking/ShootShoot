package game.data;

import game.Game;
import game.sound.Sound;
import game.tiles.Level;

import javax.imageio.ImageIO;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DataLoader {

    private static final String levelPath = "images/";

    private final static String[] LEVEL_TILE_NAMES = new String[]{"stage.png", "stage2.png"};
    private final static String[] LEVEL_IMAGE_NAMES = new String[]{"stage.png", "stage2Image.png"};

    private final Level[] levels = new Level[LEVEL_IMAGE_NAMES.length];

    public void readData() throws IOException {
        for (int i = 0; i < LEVEL_IMAGE_NAMES.length; i++) {
            BufferedImage tiles = ImageIO.read(ClassLoader.getSystemResource(levelPath + LEVEL_TILE_NAMES[i]));
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResource(levelPath + LEVEL_IMAGE_NAMES[i]));
            if (tiles.getWidth() != Game.WIDTH || tiles.getHeight() != Game.HEIGHT) throw new IOException("Bad level size");
            levels[i] = new Level(image, tiles);
        }
        for (int i = 0; i < SOUND_NAMES.length; i++){
            try {
                Sound sound = new Sound(ClassLoader.getSystemResourceAsStream(soundPath + SOUND_NAMES[i]));
                sounds[i] = sound;
            } catch (UnsupportedAudioFileException e){
                throw new IOException(e);
            }
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

    private static final String soundPath = "sounds/";

    private final static String[] SOUND_NAMES = new String[]{"minePlace.wav", "punsh.wav", "short-explosion.wav"};

    private final Sound[] sounds = new Sound[SOUND_NAMES.length];

    public Sound[] getSounds() {
        return sounds;
    }
}
