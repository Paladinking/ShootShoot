package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.entities.Player;
import game.tiles.TileMap;
import game.listeners.ProjectileListener;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Bullet extends Projectile {

    public static final int BULLET_SPEED = 100;

    private int remainingBounces;

    private final List<Point> pointsToDraw;

    private final ProjectileListener listener;

    public Bullet(Vector2d position, Vector2d velocity, int bounces, ProjectileListener listener) {
        super(position, velocity);
        this.remainingBounces = bounces;
        this.pointsToDraw = new ArrayList<>();
        pointsToDraw.add(new Point((int) position.x, (int)position.y));
        this.listener = listener;
    }

    public Bullet(double shotX, double shotY, double shotDx, double shotDy, int bounces, ProjectileListener listener) {
        this(new Vector2d(shotX, shotY), new Vector2d(shotDx, shotDy), bounces, listener);
    }

    public void draw(Graphics2D g){
        g.setColor(Color.ORANGE);
        g.setStroke(new BasicStroke(6));
        for (int i = 0; i < pointsToDraw.size()-1; i++) {
            g.drawLine(pointsToDraw.get(i).x, pointsToDraw.get(i).y, pointsToDraw.get(i+1).x, pointsToDraw.get(i+1).y);
        }
    }

    public void tick(TileMap tileMap, LocalPlayer player) {
        pointsToDraw.remove(pointsToDraw.size() - 1);
        pointsToDraw.add(new Point((int)position.x, (int)position.y));
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
                    if (!tileMap.isOpen(step)){
                        bounced = true;
                        normalVel.x *= -1;
                        velocity.x *= -1;
                        step.x += normalVel.x * 2;
                        pointsToDraw.add(new Point((int) step.x, (int) step.y));
                    }
                    step.y += normalVel.y;
                    if (!tileMap.isOpen(step)){
                        bounced = true;
                        normalVel.y *= -1;
                        velocity.y *= -1;
                        step.y += normalVel.y * 2;
                        pointsToDraw.add(new Point((int) step.x, (int) step.y));
                    }
                }
            }
            if (player.intersects(step)) {
                hitPlayer = true;
            }
        }
        position.set(step);
        if (bounced) remainingBounces--;
        if (hitPlayer){
            listener.hitPlayer(player);
            remainingBounces = -1;
        }
        pointsToDraw.add(new Point((int) position.x, (int)position.y));
        if (pointsToDraw.size()>10) {
            pointsToDraw.remove(0);
        }
    }

    public boolean isDead() {
        return remainingBounces < 0;
    }

    public Vector2d getPosition() {
        return position;
    }

}
