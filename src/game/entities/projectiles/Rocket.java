package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;
import java.awt.*;

public class Rocket extends Projectile {

    public Rocket(double x, double y, double dx, double dy) {
        super(new Vector2d(x, y), new Vector2d(dx, dy), null);
    }

    @Override
    public Status tick(TileMap tileMap, LocalPlayer player, int id) {
        return Status.ALIVE;
    }

    @Override
    public Texture getTexture() {
        return null;
    }
}
