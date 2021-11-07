package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.events.SoundPlayed;
import game.listeners.GameObjectHandler;
import game.sound.Sound;
import game.textures.AnimationTexture;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public class Explosion extends Projectile {

    private static final int EXPLOSION_DURATION = 32;

    private final int radius;
    private int ticks;

    private final AnimationTexture texture;

    public Explosion(double x, double y, int radius, GameObjectHandler handler) {
        super(new Vector2d(x, y), new Vector2d(0, 0), handler);
        this.ticks = EXPLOSION_DURATION;
        this.radius = radius;
        this.texture = Texture.explosionTexture(position, radius, EXPLOSION_DURATION);
    }

    @Override
    public Status tick(TileMap tileMap, LocalPlayer player, int id) {
        if (ticks == EXPLOSION_DURATION) {
            handler.playSound(Sound.EXPLOSION);
            handler.createEvent(new SoundPlayed(Sound.EXPLOSION));
        }
        Vector2d playerPos = player.getPosition();
        if (Point.distanceSq(playerPos.x, playerPos.y, position.x, position.y) < radius * radius) handler.hurtPlayer(player, 2);
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
