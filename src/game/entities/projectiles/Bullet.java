package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.textures.BulletTexture;
import game.textures.Texture;
import game.tiles.TileMap;
import game.listeners.ProjectileListener;

import javax.vecmath.Vector2d;

public class Bullet extends Projectile {

    private int remainingBounces;

    private final BulletTexture texture;

    private final ProjectileListener listener;

    public Bullet(Vector2d position, Vector2d velocity, int bounces, int source, ProjectileListener listener) {
        super(position, velocity);
        this.remainingBounces = bounces;
        this.texture = new BulletTexture((int)position.x, (int)position.y, source);
        this.listener = listener;
    }

    public Bullet(double shotX, double shotY, double shotDx, double shotDy, int bounces, int source, ProjectileListener listener) {
        this(new Vector2d(shotX, shotY), new Vector2d(shotDx, shotDy), bounces,source, listener);
    }

    public void tick(TileMap tileMap, LocalPlayer player) {
        final int tileSize = tileMap.getTileSize();
        Vector2d normalVel = new Vector2d(velocity);
        normalVel.normalize();
        final Vector2d step = new Vector2d(position);
        double toMove = velocity.length();
        boolean bounced = false;
        boolean hitPlayer = false;
        while (toMove > 0) {
            step.x += normalVel.x * tileSize;
            step.y += normalVel.y * tileSize;
            toMove -= tileSize;
            if (!tileMap.isOpen(step)){
                step.x -= normalVel.x * tileSize;
                step.y -= normalVel.y * tileSize;
                for (int i = 0; i < tileSize; i++){
                    step.x += normalVel.x;
                    if (!tileMap.isOpen(step)) {
                        bounced = true;
                        normalVel.x *= -1;
                        velocity.x *= -1;
                        step.x += normalVel.x * 2;
                        texture.addPoint(step);
                    }
                    step.y += normalVel.y;
                    if (!tileMap.isOpen(step)){
                        bounced = true;
                        normalVel.y *= -1;
                        velocity.y *= -1;
                        step.y += normalVel.y * 2;
                        texture.addPoint(step);
                    }
                }
            }
            if (player.intersects(step)) {
                hitPlayer = true;
            }
        }
        position.set(step);
        if (bounced) remainingBounces--;
        if (hitPlayer && !player.isDead()){
            listener.hitPlayer(player);
            remainingBounces = -1;
        }
        if (remainingBounces >= 0) texture.addPoint(position);

    }

    public boolean isDead() {
        return remainingBounces < 0;
    }

    @Override
    public Texture getTexture() {
        return texture;
    }
}
