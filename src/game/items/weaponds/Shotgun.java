package game.items.weaponds;

import game.entities.projectiles.Projectile;
import game.events.GameEvent;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Collection;

public class Shotgun extends Weapon {

    private static final int BOUNCES  = 1;

    private static final double SHOT_ANGLE = Math.PI / 90;

    private final int numberOfShots;

    public Shotgun(int maxDelay, int radius, int bulletSpeed, int numberOfShots) {
        super(maxDelay, radius, bulletSpeed);
        this.numberOfShots = numberOfShots;
    }

    private static void rotate(Vector2d vector, double angle){
        double rx = (vector.x * Math.cos(angle)) - (vector.y * Math.sin(angle));
        vector.y = (vector.x * Math.sin(angle)) + (vector.y * Math.cos(angle));
        vector.x = rx;
    }

    @Override
    public void use(Collection<GameEvent> events, TileMap tileMap, Vector2d source, Point destination) {
        Vector2d bulletVector = new Vector2d(destination.x - source.x, destination.y - source.y);
        bulletVector.normalize();
        for (int i = 0; i < numberOfShots; i++){
            Vector2d bulletVel = new Vector2d(bulletVector);
            //noinspection IntegerDivisionInFloatingPointContext
            rotate(bulletVel, (i - (numberOfShots / 2)) * SHOT_ANGLE);
            createProjectile(events, tileMap, source, bulletVel, Projectile.getBullet(BOUNCES));
        }
        setDelay();
    }
}
