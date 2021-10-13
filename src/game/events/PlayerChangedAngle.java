package game.events;

import game.Game;

import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerChangedAngle extends GameEvent {

    private final double angle;

    public PlayerChangedAngle(double angle) {
        super(PLAYER_CHANGED_ANGLE, false);
        this.angle = angle;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeDouble(angle);
    }

    @Override
    public void execute(Game game) {
        game.setPlayerAngle(angle, source);
    }
}
