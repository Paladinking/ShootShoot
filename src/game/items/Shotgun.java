package game.items;

import game.entities.projectiles.Projectile;
import game.listeners.GameObjectHandler;
import game.sound.Sound;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public class Shotgun extends Gun {

    private static final int BOUNCES = 1;

    private static final double SHOT_ANGLE = Math.PI / 90;

    private final int numberOfShots;

    public Shotgun(int maxDelay, int radius, int bulletSpeed, int numberOfShots) {
        super(maxDelay, radius, bulletSpeed, Sound.SNIPER);
        this.numberOfShots = numberOfShots;
    }

    private static void rotate(Vector2d vector, double angle) {
        double rx = (vector.x * Math.cos(angle)) - (vector.y * Math.sin(angle));
        vector.y = (vector.x * Math.sin(angle)) + (vector.y * Math.cos(angle));
        vector.x = rx;
    }

    @Override
    protected void use(GameObjectHandler handler, TileMap tileMap, Vector2d source, Point destination) {
        Vector2d bulletVector = new Vector2d(destination.x - source.x, destination.y - source.y);
        bulletVector.normalize();
        for (int i = 0; i < numberOfShots; i++) {
            Vector2d bulletVel = new Vector2d(bulletVector);
            //noinspection IntegerDivisionInFloatingPointContext
            rotate(bulletVel, (i - (numberOfShots / 2)) * SHOT_ANGLE);
            createProjectile(handler, tileMap, source, bulletVel, Projectile.getDataProjectile(BOUNCES, Projectile.BULLET));
        }
    }
}
