package game.entities.projectiles;

import game.Game;
import game.entities.LocalPlayer;
import game.listeners.ProjectileListener;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;

public abstract class Projectile {

    public static final int BULLET = 0, ROCKET = 1, MINE = 2, EXPLOSION = 3;

    public enum Status {
        ALIVE, DEAD_PREDICTABLE, DEAD_NOT_PREDICTABLE, REPLACED
    }

    public static boolean isBullet(int type) {
        return type < ROCKET;
    }

    protected final Vector2d position, velocity;

    protected final ProjectileListener listener;

    protected Projectile(Vector2d position, Vector2d velocity, ProjectileListener listener) {
        this.position = position;
        this.velocity = velocity;
        this.listener = listener;
    }

    public static int getDataProjectile(int data, int type){
        return (data << 16) | type;
    }

    public static Projectile getProjectile(double x, double y, double dx, double dy, int type, int source, ProjectileListener listener) {
        int projectileType = type & 0xffff, data = type >> 16;
        return switch (projectileType) {
            case BULLET -> new Bullet(x, y, dx, dy, data, source, listener);
            case ROCKET -> new Rocket(x, y, dx, dy);
            case MINE -> new Mine(x, y, listener);
            case EXPLOSION -> new Explosion(x, y, data, listener);
            default -> throw new IllegalArgumentException("Bad projectile type: " + projectileType + ", " + type);
        };
    }

    public void projectileEvent(){

    }

    public abstract Status tick(TileMap tileMap, LocalPlayer player, int id);

    public  Projectile getReplacement() {
        return null;
    }

    public abstract Texture getTexture();
}
