package game.items.weaponds;

import game.entities.projectiles.Bullet;
import game.entities.projectiles.Projectile;
import game.events.GameEvent;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Collection;

public class Sniper extends Weapon {

    public Sniper(int maxDelay, int radius, int bulletSpeed) {
        super(maxDelay, radius, bulletSpeed);
    }


    @Override
    public void use(Collection<GameEvent> events, TileMap tileMap, Vector2d source, Point destination) {
        Vector2d bulletVector = new Vector2d(destination.x - source.x, destination.y - source.y);
        bulletVector.normalize();
        createProjectile(events, tileMap, source, bulletVector, Projectile.getBullet(5));
        setDelay();
    }
}
