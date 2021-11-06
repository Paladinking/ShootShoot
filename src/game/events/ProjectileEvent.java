package game.events;

import game.Game;

import java.io.DataOutputStream;
import java.io.IOException;

public class ProjectileEvent extends GameEvent {
    private final int id;

    public ProjectileEvent(int id) {
        super(PROJECTILE_EVENT, false);
        this.id = id;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeInt(id);
    }

    @Override
    public void execute(Game game) {
        game.affectProjectile(id);
    }
}
