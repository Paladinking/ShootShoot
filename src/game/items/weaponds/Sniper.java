package game.items.weaponds;

import game.entities.projectiles.Projectile;
import game.events.GameEvent;
import game.listeners.GameEventHandler;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Collection;

public class Sniper extends Weapon {

    private static final int BOUNCES = 5;

    public Sniper(int maxDelay, int radius, int bulletSpeed) {
        super(maxDelay, radius, bulletSpeed);
    }

    @Override
    public void use(GameEventHandler handler, TileMap tileMap, Vector2d source, Point destination) {
        Vector2d bulletVector = new Vector2d(destination.x - source.x, destination.y - source.y);
        bulletVector.normalize();
        createProjectile(handler, tileMap, source, bulletVector, Projectile.getBullet(BOUNCES));
        setDelay();
    }
}
