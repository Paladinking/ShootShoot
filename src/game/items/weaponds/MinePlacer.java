package game.items.weaponds;

import game.entities.projectiles.Projectile;
import game.events.ProjectileCreated;
import game.listeners.GameEventHandler;
import game.sound.Sound;
import game.tiles.TileMap;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MinePlacer extends Weapon {

    private static final Sound sound;

    static {
        Sound temp;
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("sounds/minePlace.wav");
            if (in == null) throw new IllegalArgumentException("Could not open file sounds/minePlace.wav");
            temp = new Sound(new BufferedInputStream(in));
        } catch (IOException | UnsupportedAudioFileException e){
            temp = new Sound();
        }
        sound = temp;
    }

    public MinePlacer(int maxDelay) {
        super(maxDelay, sound);
    }

    @Override
    protected void use(GameEventHandler handler, TileMap tileMap, Vector2d source, Point destination) {
        handler.addEvent(new ProjectileCreated(destination.x, destination.y, 0, 0, Projectile.MINE));
    }
}
