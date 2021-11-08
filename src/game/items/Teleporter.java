package game.items;

import game.events.ProjectileRemoved;
import game.events.TeleporterPlaced;
import game.listeners.GameObjectHandler;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public class Teleporter extends Item {

    private boolean deployed;

    private int platformId;

    private double x, y;

    public Teleporter(int maxDelay) {
        super(maxDelay, -1);
        this.deployed = false;
    }

    @Override
    protected void use(GameObjectHandler handler, TileMap tileMap, Vector2d source, Point destination, int radius) {
        if (deployed){
            if (x != -1) {
                handler.getPlayer().teleport(handler, x, y);
                handler.createEvent(new ProjectileRemoved(platformId, true));
                deployed = false;
            }
        } else {
            handler.createEvent(new TeleporterPlaced(source.x, source.y));
            deployed = true;
        }
    }

    public void setPlatform(int id, double x, double y) {
        this.platformId = id;
        this.x = x;
        this.y = y;
    }
}
