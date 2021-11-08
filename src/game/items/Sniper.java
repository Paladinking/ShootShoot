package game.items;

import game.entities.projectiles.Projectile;
import game.listeners.GameObjectHandler;
import game.sound.Sound;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public class Sniper extends Gun {

    private static final int BOUNCES = 5;

    public Sniper(int maxDelay, int bulletSpeed) {
        super(maxDelay, bulletSpeed, Sound.SNIPER);
    }

    @Override
    protected void use(GameObjectHandler handler, TileMap tileMap, Vector2d source, Point destination, int radius) {
        Vector2d bulletVector = new Vector2d(destination.x - source.x, destination.y - source.y);
        bulletVector.normalize();
        int bullet = Projectile.getDataProjectile(BOUNCES, Projectile.BULLET);
        createProjectile(handler, tileMap, source, bulletVector, bullet, radius);
    }
}
