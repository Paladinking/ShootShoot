package game.events;

import game.Game;

import java.io.DataOutputStream;
import java.io.IOException;

public class SoundPlayed extends GameEvent {

    private final int id;

    public SoundPlayed(int id) {
        super(SOUND_PLAYED, false);
        this.id = id;
    }

    @Override
    protected void write(DataOutputStream out) throws IOException {
        out.writeInt(id);
    }

    @Override
    public void execute(Game game) {
        game.playSound(id);
    }
}
