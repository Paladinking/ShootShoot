package game;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;

public class Player {

    private static final int SPEED = 3, SHOOT_DELAY = 100;

    private static final double FRICTION = 0.7;
    private final Vector2d position, velocity;

    private final int radius;

    private final Color color;

    private int shootDelay;

    private final PlayerListener listener;

    public Player(int x, int y, int diameter, Color color, PlayerListener listener) {
        this.position = new Vector2d(x, y);
        this.velocity = new Vector2d(0, 0);
        this.radius = diameter / 2;
        this.color = color;
        this.shootDelay = 0;
        this.listener = listener;
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillOval((int) position.x - radius, (int) position.y - radius, radius * 2, radius * 2);
    }

    private void handleInputs(Map<Integer, Boolean> keyMap, Point mousePos, TileMap tileMap){
        Vector2d acceleration = new Vector2d(0, 0);
        if (keyMap.get(KeyEvent.VK_W)) acceleration.y--;
        if (keyMap.get(KeyEvent.VK_A)) acceleration.x--;
        if (keyMap.get(KeyEvent.VK_S)) acceleration.y++;
        if (keyMap.get(KeyEvent.VK_D)) acceleration.x++;
        if (acceleration.lengthSquared() > 0) acceleration.normalize();
        acceleration.scale(SPEED);
        velocity.add(acceleration);
        velocity.scale(FRICTION);
        if (keyMap.get(KeyEvent.VK_SPACE) && shootDelay == 0){

            Vector2d bulletVector = new Vector2d(mousePos.x - position.x, mousePos.y - position.y);
            bulletVector.normalize();
            Vector2d bulletPos = new Vector2d(position);
            bulletPos.add(new Vector2d(bulletVector.x * radius, bulletVector.y * radius));
            if (tileMap.isOpen(bulletPos)) {
                shootDelay = SHOOT_DELAY;
                bulletVector.scale(Bullet.BULLET_SPEED);
                listener.firedShot(bulletPos, bulletVector);
            }
        }
        if (shootDelay > 0) shootDelay--;
    }

    public void tick(TileMap tileMap, Map<Integer, Boolean> keyMap, Point mousePos) {
        handleInputs(keyMap, mousePos, tileMap);
        if (velocity.lengthSquared() == 0) return;
        Vector2d nextPosition = new Vector2d(position.x + velocity.x, position.y + velocity.y);
        int tileSize = tileMap.getTileSize();
        int posX = (int) nextPosition.x, posY = (int) nextPosition.y;
        for (int x = (posX - radius) / tileSize; x <= (posX + radius) / tileSize; x++) {
            for (int y = (posY - radius) / tileSize; y <= (posY + radius) / tileSize; y++) {
                if (!tileMap.isOpen(x, y)) {
                    Vector2d nearestPoint = new Vector2d(
                            Math.max(x * tileSize, Math.min((x + 1) * tileSize, nextPosition.x)),
                            Math.max(y * tileSize, Math.min((y + 1) * tileSize, nextPosition.y)));
                    Vector2d rayToNearest = new Vector2d(nearestPoint.x - nextPosition.x, nearestPoint.y - nextPosition.y);
                    if (rayToNearest.length() == 0) nextPosition.set(position);
                    double overlap = radius - rayToNearest.length();
                    if (overlap > 0) {
                        rayToNearest.normalize();
                        rayToNearest.scale(-overlap);
                        nextPosition.add(rayToNearest);
                    }
                }

            }
        }
        position.set(nextPosition);

    }

    public void setPosition(double x, double y) {
        position.x = x;
        position.y = y;
    }


    public Vector2d getPosition() {
        return position;
    }
}
