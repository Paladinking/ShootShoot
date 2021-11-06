package game.entities;

import game.events.PlayerMoved;
import game.items.weaponds.MinePlacer;
import game.items.weaponds.Shotgun;
import game.items.weaponds.Sniper;
import game.items.weaponds.Weapon;
import game.listeners.GameEventHandler;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;

public class LocalPlayer extends Player {

    private static final int SPEED = 3, SHOOT_DELAY = 100, MAX_STAMINA = 100, BULLET_SPEED = 100, SHOTGUN_SHOTS = 3,
        INVINCIBILITY_TICKS = 16;

    private static final double FRICTION = 0.7, MINIMUM_VELOCITY = 0.05, SPRINT_FACTOR = 2.0;
    private final Vector2d velocity;

    private final Weapon[] weapons;

    private Weapon activeWeapon, sideWeapon;

    private boolean sprintEnded;

    private int stamina, invTicks;

    public LocalPlayer(int x, int y, int diameter, int number) {
        super(x, y, diameter, number);
        this.velocity = new Vector2d(0, 0);
        this.weapons = new Weapon[]{
                new Sniper(SHOOT_DELAY, radius, BULLET_SPEED),
                new Shotgun(SHOOT_DELAY, radius, BULLET_SPEED / 3, SHOTGUN_SHOTS)
        };
        this.activeWeapon = weapons[0];
        this.sideWeapon = new MinePlacer(SHOOT_DELAY);
        this.sprintEnded = false;
    }

    private void handleInputs(Map<Integer, Boolean> keyMap, Point mousePos, TileMap tileMap, GameEventHandler handler) {
        boolean sprinting = keyMap.get(KeyEvent.VK_SHIFT) && stamina > 0 && !sprintEnded;
        if (sprinting) {
            stamina -= 2;
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

        if (keyMap.get(KeyEvent.VK_1)) activeWeapon = weapons[0];
        if (keyMap.get(KeyEvent.VK_2)) activeWeapon = weapons[1];
        if (keyMap.get(KeyEvent.VK_SPACE)) {
            activeWeapon.tryUse(handler, tileMap, position, mousePos);
        }
        if (keyMap.get(KeyEvent.VK_Q)) {
            sideWeapon.tryUse(handler, tileMap, position, new Point((int)position.x, (int)position.y));
        }

    }

    @Override
    public void tick(TileMap tileMap, Map<Integer, Boolean> keyMap, Point mousePos, GameEventHandler handler) {
        super.tick(tileMap, keyMap, mousePos, handler);
        if (isDead()) return;
        if (invTicks > 0) invTicks--;
        activeWeapon.tick();
        sideWeapon.tick();
        handleInputs(keyMap, mousePos, tileMap, handler);
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
        double angle = Math.atan2(mousePos.y - position.y, mousePos.x - position.x);
        setAngle(angle);
        handler.addEvent(new PlayerMoved(position.x, position.y, angle));
    }

    public void setPosition(double x, double y) {
        position.x = x;
        position.y = y;
    }

    @Override
    public void hurt(int amount){
        super.hurt(amount);
        invTicks = INVINCIBILITY_TICKS;
    }


    public boolean isInvincible(){
        return invTicks > 0;
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
        return activeWeapon.getDelayFraction();
    }

    public double getStaminaFraction() {
        return ((double) stamina) / MAX_STAMINA;
    }
}
