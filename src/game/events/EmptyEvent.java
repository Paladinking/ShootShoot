package game.events;

import game.Game;

import java.io.DataOutputStream;
import java.io.IOException;

class EmptyEvent extends GameEvent {

    protected EmptyEvent(int type) {
        super(type, false);
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {

    }

    @Override
    public void execute(Game game) {

    }
}
