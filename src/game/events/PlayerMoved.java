package game.events;

import game.Game;

import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerMoved extends GameEvent {

    private final double newX, newY, angle;

    public PlayerMoved(double newX, double newY, double angle) {
        super(PLAYER_MOVED, false);
        this.newX = newX;
        this.newY = newY;
        this.angle = angle;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeDouble(newX);
        out.writeDouble(newY);
        out.writeDouble(angle);
    }

    @Override
    public void execute(Game game) {
        game.movePlayer(newX, newY, source);
        game.setPlayerAngle(angle, source);
    }
}
