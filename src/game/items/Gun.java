package game.items;

import game.events.ProjectileCreated;
import game.listeners.GameObjectHandler;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;

public abstract class Gun extends Item {

    private final int radius, projectileSpeed;

    protected Gun(int maxDelay, int radius, int bulletSpeed, int soundEffect) {
        super(maxDelay, soundEffect);
        this.radius = radius;
        this.projectileSpeed = bulletSpeed;
    }

    protected void createProjectile(GameObjectHandler handler, TileMap tileMap, Vector2d source, Vector2d projectileVector, int type){
        Vector2d bulletPos = new Vector2d(source);
        bulletPos.add(new Vector2d(projectileVector.x * (radius + 1), projectileVector.y * (radius + 1)));
        if (tileMap.isOpen(bulletPos)) {
            projectileVector.scale(projectileSpeed);
            handler.createEvent(new ProjectileCreated(bulletPos.x, bulletPos.y, projectileVector.x, projectileVector.y, type));
        }
    }
}
