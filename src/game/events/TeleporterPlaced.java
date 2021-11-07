package game.events;

import game.entities.projectiles.Projectile;
import game.server.PlayerHandler;

import java.io.DataOutputStream;
import java.io.IOException;

public class TeleporterPlaced extends EmptyEvent {

    final double x, y;

    public TeleporterPlaced(double x, double y) {
        super(TELEPORTER_USED);
        this.x = x;
        this.y = y;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeDouble(x);
        out.writeDouble(y);
    }

    @Override
    public boolean handle(PlayerHandler handler) {
        int index = PlayerHandler.getBulletIndex();
        GameEvent event = new ProjectileCreated(x, y, 0, 0, Projectile.TELEPORT_PLATFORM, index);
        handler.addEvent(event);
        handler.addSelfEvent(event);
        handler.addSelfEvent(new ProjectileEvent(index));
        return false;
    }
}
