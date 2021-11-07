package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.listeners.GameObjectHandler;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;

public abstract class Projectile {

    public static final int BULLET = 0, ROCKET = 1, MINE = 2, EXPLOSION = 3, TELEPORT_PLATFORM = 4;

    public enum Status {
        ALIVE, DEAD_PREDICTABLE, DEAD_NOT_PREDICTABLE, REPLACED
    }

    public static boolean isBullet(int type) {
        return type < ROCKET;
    }

    protected final Vector2d position, velocity;

    protected final GameObjectHandler handler;

    protected Projectile(Vector2d position, Vector2d velocity, GameObjectHandler handler) {
        this.position = position;
        this.velocity = velocity;
        this.handler = handler;
    }

    public static int getDataProjectile(int data, int type){
        return (data << 16) | type;
    }

    public static Projectile getProjectile(double x, double y, double dx, double dy, int type, int source, GameObjectHandler handler) {
        int projectileType = type & 0xffff, data = type >> 16;
        return switch (projectileType) {
            case BULLET -> new Bullet(x, y, dx, dy, data, source, handler);
            case ROCKET -> new Rocket(x, y, dx, dy);
            case MINE -> new Mine(x, y, handler);
            case EXPLOSION -> new Explosion(x, y, data, handler);
            case TELEPORT_PLATFORM -> new TeleportPlatform(x, y, handler);
            default -> throw new IllegalArgumentException("Bad projectile type: " + projectileType + ", " + type);
        };
    }

    public void projectileEvent(int id){

    }

    public void removed(){

    }

    public abstract Status tick(TileMap tileMap, LocalPlayer player, int id);

    public Projectile getReplacement() {
        return null;
    }

    public abstract Texture getTexture();
}
