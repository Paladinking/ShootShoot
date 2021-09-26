package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public abstract class Projectile {

    private static final int BULLET_MIN = 0, BULLET_MAX = 24, ROCKET = 25;

    public static boolean isBullet(int type){
        return type < ROCKET;
    }

    protected final Vector2d position, velocity;

    protected Projectile(Vector2d position, Vector2d velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public static boolean isRocket(int type) {
        return type == ROCKET;
    }

    public static int getBullet(int bounces) {
        return Math.max(BULLET_MIN, Math.min(BULLET_MAX, bounces));
    }

    public abstract void tick(TileMap tileMap, LocalPlayer player);

    public abstract boolean isDead();

    public abstract void draw(Graphics2D g);
}
