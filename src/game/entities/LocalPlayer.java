package game.entities;

import game.entities.projectiles.Bullet;
import game.items.weaponds.Shotgun;
import game.items.weaponds.Sniper;
import game.items.weaponds.Weapon;
import game.tiles.TileMap;
import game.listeners.PlayerListener;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;

public class LocalPlayer extends Player {

    private static final int SPEED = 3, SHOOT_DELAY = 100, MAX_STAMINA = 100;

    private static final double FRICTION = 0.7, MINIMUM_VELOCITY = 0.05, SPRINT_FACTOR = 2.0;
    private final Vector2d velocity;

    private Weapon activeWeapon;

    private boolean sprintEnded;

    private int stamina;

    private final PlayerListener listener;

    public LocalPlayer(int x, int y, int diameter, Color color, PlayerListener listener) {
        super(x, y, diameter, color);
        this.velocity = new Vector2d(0, 0);
        this.listener = listener;
        this.activeWeapon = new Sniper(SHOOT_DELAY, radius, Bullet.BULLET_SPEED);
        this.activeWeapon = new Shotgun(SHOOT_DELAY, radius, Bullet.BULLET_SPEED / 2, 3);
    }

    private void handleInputs(Map<Integer, Boolean> keyMap) {
        boolean sprinting = keyMap.get(KeyEvent.VK_SHIFT) && stamina > 0 && !sprintEnded;
        if (sprinting) {
            stamina-=2;
            if (stamina <= 0){
                sprintEnded = true;
            }
        } else if (stamina  < MAX_STAMINA) {
            stamina++;
        }
        if (sprintEnded && !keyMap.get(KeyEvent.VK_SHIFT)) sprintEnded = false;


        Vector2d acceleration = new Vector2d(0, 0);
        if (keyMap.get(KeyEvent.VK_W)) acceleration.y--;
        if (keyMap.get(KeyEvent.VK_A)) acceleration.x--;
        if (keyMap.get(KeyEvent.VK_S)) acceleration.y++;
        if (keyMap.get(KeyEvent.VK_D)) acceleration.x++;
        if (acceleration.lengthSquared() > 0) acceleration.normalize();
        acceleration.scale(sprinting ? SPEED * SPRINT_FACTOR:  SPEED);
        velocity.add(acceleration);
        velocity.scale(FRICTION);

        if (keyMap.get(KeyEvent.VK_SPACE) && activeWeapon.isReady()) {
            listener.playerUsedWeapon(activeWeapon, this);
        }
    }

    public void tick(TileMap tileMap, Map<Integer, Boolean> keyMap) {
        super.tick(tileMap, keyMap);
        if (isDead()) return;
        activeWeapon.tick();
        handleInputs(keyMap);
        if (velocity.lengthSquared() < MINIMUM_VELOCITY) {
            return;
        }
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
        listener.playerMoved(position);

    }

    public void setPosition(double x, double y) {
        position.x = x;
        position.y = y;
    }


    public Vector2d getPosition() {
        return position;
    }

    public boolean intersects(Vector2d position) {
        return Point.distanceSq(position.x, position.y, this.position.x, this.position.y) < radius * radius;
    }


    public double getHpFraction() {
        return ((double) hp) / Player.START_HP;
    }

    public double getShootDelayFraction() {
        return activeWeapon.getShootDelayFraction();
    }

    public double getStaminaFraction() {
        return ((double) stamina) / MAX_STAMINA;
    }
}
