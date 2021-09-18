package game;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Bullet {

    public static final int BULLET_SPEED = 100;
    private final static int BOUNCES = 5;

    private int remainingBounces;
    private final Vector2d position, velocity;
    private final List<Point> pointsToDraw;

    public Bullet(Vector2d position, Vector2d velocity) {
        this.position = position;
        this.velocity = velocity;
        this.remainingBounces = BOUNCES;
        this.pointsToDraw = new ArrayList<>();
        pointsToDraw.add(new Point((int) position.x, (int)position.y));
    }

    public Bullet(double shotX, double shotY, double shotDx, double shotDy) {
        this(new Vector2d(shotX, shotY), new Vector2d(shotDx, shotDy));
    }

    public void draw(Graphics2D g){
        g.setColor(Color.ORANGE);
        //g.fillOval((int)position.x - 10, (int)position.y - 10, 20, 20);
        g.setStroke(new BasicStroke(6));
        for (int i = 0; i < pointsToDraw.size()-1; i++) {
            g.drawLine(pointsToDraw.get(i).x, pointsToDraw.get(i).y, pointsToDraw.get(i+1).x, pointsToDraw.get(i+1).y);
        }
    }

    public void tick(TileMap tileMap) {
        pointsToDraw.remove(pointsToDraw.size() - 1);
        pointsToDraw.add(new Point((int)position.x, (int)position.y));
        final int tileSize = tileMap.getTileSize();
        Vector2d normalVel = new Vector2d(velocity);
        normalVel.normalize();
        final Vector2d step = new Vector2d(position);
        double toMove = velocity.length();
        boolean bounced = false;
        while (toMove > 0) {
            step.x += normalVel.x * tileSize;
            step.y += normalVel.y * tileSize;
            toMove -= tileSize;
            if (!tileMap.isOpen(step)){
                step.x -= normalVel.x * tileSize;
                step.y -= normalVel.y * tileSize;
                if (!tileMap.isOpen(step)){
                    System.out.println("WTF!!!!");
                }
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
        }
        position.set(step);
        if (bounced) remainingBounces--;
        pointsToDraw.add(new Point((int) position.x, (int)position.y));
        if (pointsToDraw.size()>10) {
            pointsToDraw.remove(0);
        }
    }

    public boolean isDead() {
        return remainingBounces < 0;
    }
}
