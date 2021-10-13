package game.events;

import game.Game;

import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerHurt extends GameEvent {

    private final int amount;

    public PlayerHurt(int amount) {
        super(PLAYER_HURT, false);
        this.amount = amount;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeInt(amount);
    }

    @Override
    public void execute(Game game) {
        game.hurtPlayer(amount, source);
    }
}
