package game.events;

import game.Game;

import java.io.DataOutputStream;
import java.io.IOException;

public class ProjectileRemoved extends GameEvent {

    private final int index;

    public ProjectileRemoved(int index) {
        super(PROJECTILE_REMOVED, false);
        this.index = index;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeInt(index);
    }

    @Override
    public void execute(Game game) {
        game.removeProjectile(index);
    }
}
