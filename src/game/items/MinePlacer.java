package game.items;

import game.entities.projectiles.Projectile;
import game.events.ProjectileCreated;
import game.listeners.GameObjectHandler;
import game.sound.Sound;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public class MinePlacer extends Item {

    public MinePlacer(int maxDelay) {
        super(maxDelay, Sound.MINE_PLACE);
    }

    @Override
    protected void use(GameObjectHandler handler, TileMap tileMap, Vector2d source, Point destination) {
        handler.createEvent(new ProjectileCreated(destination.x, destination.y, 0, 0, Projectile.MINE));
    }
}
