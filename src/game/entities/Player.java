package game.entities;

import game.listeners.GameEventHandler;
import game.textures.PlayerTexture;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.Map;

public class Player {

    protected final static int START_HP = 5;

    protected final Vector2d position;

    private int hurtTicks;
    protected int hp;

    protected final int radius, number;

    private final PlayerTexture texture;

    public Player(int x, int y, int diameter, int number){
        this.texture = new PlayerTexture(this, number);
        this.number = number;
        this.radius = diameter / 2;
        this.position = new Vector2d(x, y);
        this.hp = START_HP;
    }

    public void tick(TileMap tileMap, Map<Integer, Boolean> keyMap, Point mousePos, GameEventHandler handler){
        if (hurtTicks > 0) {
            hurtTicks--;
            if (hurtTicks == 0) texture.setState(PlayerTexture.State.NORMAL);
        }
    }

    public Texture getTexture(){
        return texture;
    }

    public void hurt(int amount) {
        hp = Math.max(0, hp - amount);
        hurtTicks = 10;
        texture.setState(PlayerTexture.State.HURT);
    }

    public boolean isDead() {
        return hp < 1;
    }

    public void setPosition(double newX, double newY){
        this.position.set(newX, newY);
    }

    public int getRadius() {
        return radius;
    }

    public Vector2d getPosition() {
        return position;
    }

    public void setAngle(double angle) {
        this.texture.setAngle(angle);
    }
}
