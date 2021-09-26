package game.entities;

import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Map;

public class Player {

    protected final static int START_HP = 5;

    protected final Vector2d position;

    private int hurtTicks;
    protected int hp;

    protected final int radius;

    private final Color color;

    public Player(int x, int y, int diameter, Color color){
        this.color = color;
        this.radius = diameter / 2;
        this.position = new Vector2d(x, y);
        this.hp = START_HP;
    }

    public void tick(TileMap tileMap, Map<Integer, Boolean> keyMap){
        if (hurtTicks > 0) hurtTicks--;
    }

    public void draw(Graphics2D g) {
        g.setColor(hurtTicks > 0 ? Color.orange : color);
        g.fillOval((int) position.x - radius, (int) position.y - radius, radius * 2, radius * 2);
    }

    public void hurt(int amount) {
        hp -= amount;
        hurtTicks = 10;
    }

    public boolean isDead() {
        return hp < 1;
    }

    public void setPosition(double newX, double newY){
        this.position.set(newX, newY);
    }
}
