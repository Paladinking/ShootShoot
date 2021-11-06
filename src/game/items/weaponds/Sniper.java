package game.items.weaponds;

import game.entities.projectiles.Projectile;
import game.events.GameEvent;
import game.listeners.GameEventHandler;
import game.sound.Sound;
import game.tiles.TileMap;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class Sniper extends Gun {

    private static final int BOUNCES = 5;

    protected static final Sound sound;

    static {
        Sound temp;
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("sounds/punsh.wav");
            if (in == null) throw new IllegalArgumentException("Could not open file sounds/punsh.wav");
            temp = new Sound(new BufferedInputStream(in));
        } catch (IOException | UnsupportedAudioFileException e){
            temp = new Sound();
        }
        sound = temp;
    }

    public Sniper(int maxDelay, int radius, int bulletSpeed) {
        super(maxDelay, radius, bulletSpeed, sound);
    }

    @Override
    protected void use(GameEventHandler handler, TileMap tileMap, Vector2d source, Point destination) {
        Vector2d bulletVector = new Vector2d(destination.x - source.x, destination.y - source.y);
        bulletVector.normalize();
        createProjectile(handler, tileMap, source, bulletVector, Projectile.getDataProjectile(BOUNCES, Projectile.BULLET));
    }
}
