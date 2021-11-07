package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.events.ProjectileEvent;
import game.listeners.GameObjectHandler;
import game.textures.MineTexture;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public class Mine extends Projectile {

    private static final int TRIGGER_RADIUS = 76, TRIGGER_RADIUS_SQUARED = TRIGGER_RADIUS * TRIGGER_RADIUS, STARTUP_TICKS = 50,
            TRIGGER_TIME = 9, EXPLOSION_RADIUS = 90;

    private int startupTicks, explosionCountDown;

    private final MineTexture texture;

    private boolean triggered;

    protected Mine(double x, double y, GameObjectHandler handler) {
        super(new Vector2d(x, y), new Vector2d(0, 0), handler);
        triggered = false;
        this.texture = new MineTexture((int) position.x, (int) position.y);
        this.startupTicks = STARTUP_TICKS;
        this.explosionCountDown = TRIGGER_TIME;
    }

    @Override
    public Status tick(TileMap tileMap, LocalPlayer player, int id ) {
        if (startupTicks > 0) {
            startupTicks--;
            if (startupTicks == 0) {
                texture.setState(MineTexture.State.ACTIVE);
            }
        } else if (triggered) {
            explosionCountDown--;
            if (explosionCountDown == 0) {
                texture.setState(MineTexture.State.EXPLODED);
                return Status.REPLACED;
            }
        } else {
            Vector2d playerPos = player.getPosition();
            if (Point.distanceSq(position.x, position.y, playerPos.x, playerPos.y) < TRIGGER_RADIUS_SQUARED && !player.isDead()) {
                projectileEvent(-1);
                handler.createEvent(new ProjectileEvent(id));
            }
        }
        return Status.ALIVE;
    }


    @Override
    public Projectile getReplacement() {
        int explosionType = Projectile.getDataProjectile(EXPLOSION_RADIUS, Projectile.EXPLOSION);
        return Projectile.getProjectile(position.x, position.y,0, 0, explosionType, 0, handler);
    }

    @Override
    public void projectileEvent(int id) {
        triggered = true;
        texture.setState(MineTexture.State.TRIGGERED);
    }

    @Override
    public Texture getTexture() {
        return texture;
    }
}
