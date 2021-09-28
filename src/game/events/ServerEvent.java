package game.events;

import game.Game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ServerEvent extends GameEvent {

    private static final int TOTAL_EVENTS = 1;

    private static final int NEW_GAME = 0;

    private static final GameEvent.EventReader[] readers;

    static {
        readers = new EventReader[TOTAL_EVENTS];
        readers[NEW_GAME] = in -> new NewGame();
    }

    protected ServerEvent(int type) {
        super(type);
    }

    public static ServerEvent read(DataInputStream in) throws IOException{
        int type = in.read();
        return (ServerEvent) readers[type].read(in);
    }

    public abstract void executeImmediate(Game game);

    public static class NewGame extends ServerEvent{
        public NewGame() {
            super(NEW_GAME);
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            out.write(NEW_GAME);
        }

        @Override
        public void execute(Game game) {
            game.restart();
        }

        @Override
        public void executeImmediate(Game game) {
            game.stopReaderThread();
        }
    }


}
