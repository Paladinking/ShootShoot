package game.entities.projectiles;

import game.entities.LocalPlayer;
import game.listeners.GameObjectHandler;
import game.textures.AnimationTexture;
import game.textures.MineTexture;
import game.textures.Texture;
import game.tiles.TileMap;

import javax.vecmath.Vector2d;

public class TeleportPlatform extends Projectile {

    protected TeleportPlatform(double x, double y, GameObjectHandler handler) {
        super(new Vector2d(x, y), new Vector2d(0, 0), handler);
        this.texture = Texture.teleporterTexture(x, y);
    }

    private final AnimationTexture texture;

    @Override
    public Status tick(TileMap tileMap, LocalPlayer player, int id) {
        return Status.ALIVE;
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public void removed(){
        texture.remove();
    }

    @Override
    public void projectileEvent(int id) {
        handler.getPlayer().setTeleporterPlatform(id, position.x, position.y);
    }
}
