package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.listeners.ProjectileListener;
import game.sound.Sound;
import game.textures.AnimationTexture;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Explosion extends Projectile {

    private static final int EXPLOSION_DURATION = 32;

    private static final Sound sound;

    static {
        Sound temp;
        try {
            InputStream in = ClassLoader.getSystemResourceAsStream("sounds/short-explosion.wav");
            if (in == null) throw new IllegalArgumentException("Could not open file sounds/short-explosion.wav");
            temp = new Sound(new BufferedInputStream(in));
        } catch (IOException | UnsupportedAudioFileException e){
            temp = new Sound();
        }
        sound = temp;
    }

    private final int radius;
    private int ticks;

    private final AnimationTexture texture;

    public Explosion(double x, double y, int radius, ProjectileListener listener) {
        super(new Vector2d(x, y), new Vector2d(0, 0), listener);
        this.ticks = EXPLOSION_DURATION;
        this.radius = radius;
        this.texture = Texture.explosionTexture(position, radius, EXPLOSION_DURATION);
    }

    @Override
    public Status tick(TileMap tileMap, LocalPlayer player, int id) {
        if (ticks == EXPLOSION_DURATION) sound.play();
        Vector2d playerPos = player.getPosition();
        if (Point.distanceSq(playerPos.x, playerPos.y, position.x, position.y) < radius * radius) listener.hurtPlayer(player, 2);
        ticks--;
        if (ticks == 0) {
            texture.remove();
            return Status.DEAD_PREDICTABLE;
        }
        return Status.ALIVE;
    }

    @Override
    public Texture getTexture() {
        return texture;
    }
}
