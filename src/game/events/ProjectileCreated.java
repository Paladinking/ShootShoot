package game.events;

import game.Game;
import game.server.PlayerHandler;

import java.io.DataOutputStream;
import java.io.IOException;

public class ProjectileCreated extends GameEvent {
    private final double xPos, yPos, xVel, yVel;
    private final int type;

    private int index;

    public ProjectileCreated(double xPos, double yPos, double xVel, double yVel, int type) {
        super(PROJECTILE_CREATED, true);
        this.xPos = xPos;
        this.yPos = yPos;
        this.xVel = xVel;
        this.yVel = yVel;
        this.type = type;
        this.index = -1;
    }

    public ProjectileCreated(double x, double y, double dx, double dy, int type, int index) {
        this(x, y, dx, dy, type);
        this.index = index;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeDouble(xPos);
        out.writeDouble(yPos);
        out.writeDouble(xVel);
        out.writeDouble(yVel);
        out.writeInt(type);
        out.writeInt(index);
    }

    @Override
    public void execute(Game game) {
        game.createProjectile(xPos, yPos, xVel, yVel, type, index, source);
    }

    @Override
    public boolean handle(PlayerHandler handler) {
        this.index = PlayerHandler.getBulletIndex();
        return true;
    }
}
