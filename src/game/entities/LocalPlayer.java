package game.entities;

import game.events.PlayerMoved;
import game.items.*;
import game.listeners.GameObjectHandler;
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

    private final Item[] items;

    private Item activeItem, sideItem;

    private final Teleporter teleporter = new Teleporter(SHOOT_DELAY);

    private boolean sprintEnded;

    private int stamina, invTicks;

    public LocalPlayer(int x, int y, int diameter, int number) {
        super(x, y, diameter, number);
        this.velocity = new Vector2d(0, 0);
        this.items = new Item[]{
                new Sniper(SHOOT_DELAY, radius, BULLET_SPEED),
                new Shotgun(SHOOT_DELAY, radius, BULLET_SPEED / 3, SHOTGUN_SHOTS)
        };
        this.activeItem = items[0];
        this.sideItem = new MinePlacer(SHOOT_DELAY);
        this.sideItem = teleporter;
        this.sprintEnded = false;
    }

    private void handleInputs(Map<Integer, Boolean> keyMap, Point mousePos, TileMap tileMap, GameObjectHandler handler) {
        boolean sprinting = keyMap.get(KeyEvent.VK_SHIFT) && stamina > 0 && !sprintEnded;
        if (sprinting) {
            stamina -= 2;
            if (stamina <= 0) {
                sprintEnded = true;
            }
        } else if (stamina < MAX_STAMINA) {
            stamina++;
        }
        if (sprintEnded && !keyMap.get(KeyEvent.VK_SHIFT)) sprintEnded = false;

        Vector2d acceleration = new Vector2d(0, 0);
        if (keyMap.get(KeyEvent.VK_W)) acceleration.y--;
        if (keyMap.get(KeyEvent.VK_A)) acceleration.x--;
        if (keyMap.get(KeyEvent.VK_S)) acceleration.y++;
        if (keyMap.get(KeyEvent.VK_D)) acceleration.x++;
        if (acceleration.lengthSquared() > 0) acceleration.normalize();
        acceleration.scale(sprinting ? SPEED * SPRINT_FACTOR : SPEED);
        velocity.add(acceleration);
        velocity.scale(FRICTION);

        if (keyMap.get(KeyEvent.VK_1)) activeItem = items[0];
        if (keyMap.get(KeyEvent.VK_2)) activeItem = items[1];
        if (keyMap.get(KeyEvent.VK_SPACE)) {
            activeItem.tryUse(handler, tileMap, position, mousePos);
        }
        if (keyMap.get(KeyEvent.VK_Q)) {
            sideItem.tryUse(handler, tileMap, position, new Point((int) position.x, (int) position.y));
        }

    }

    @Override
    public void tick(TileMap tileMap, Map<Integer, Boolean> keyMap, Point mousePos, GameObjectHandler handler) {
        super.tick(tileMap, keyMap, mousePos, handler);
        if (isDead()) return;
        if (invTicks > 0) invTicks--;
        activeItem.tick();
        sideItem.tick();
        handleInputs(keyMap, mousePos, tileMap, handler);
        if (velocity.lengthSquared() < MINIMUM_VELOCITY) {
            return;
        }
        Vector2d nextPosition = new Vector2d(position.x + velocity.x, position.y + velocity.y);
        tileMap.adjustPosition(nextPosition, radius);
        position.set(nextPosition);
        double angle = Math.atan2(mousePos.y - position.y, mousePos.x - position.x);
        setAngle(angle);
        handler.createEvent(new PlayerMoved(position.x, position.y, angle));
    }


    public void setPosition(double x, double y) {
        position.x = x;
        position.y = y;
    }

    @Override
    public void hurt(int amount) {
        super.hurt(amount);
        invTicks = INVINCIBILITY_TICKS;
    }

    public int getNumber() {
        return number;
    }


    public boolean isInvincible() {
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
        return activeItem.getDelayFraction();
    }

    public double getStaminaFraction() {
        return ((double) stamina) / MAX_STAMINA;
    }

    public void setTeleporterPlatform(int id, double x, double y) {
        teleporter.setPlatform(id, x, y);
    }

    public void teleport(GameObjectHandler handler, double x, double y) {
        position.set(x, y);
        velocity.set(0, 0);
        handler.createEvent(new PlayerMoved(x, y, Math.PI / 2));
    }
}
