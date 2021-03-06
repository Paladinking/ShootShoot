package game.events;

import game.Game;
import game.client.GameClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ServerEvent extends GameEvent {

    private static final int TOTAL_EVENTS = 1;

    protected static final int NEW_GAME = 0;

    private static final GameEvent.EventReader[] readers;

    static {
        readers = new EventReader[TOTAL_EVENTS];
        readers[NEW_GAME] = in -> new NewGame();
    }

    protected ServerEvent(int type) {
        super(type, false);
    }

    public static void write(ServerEvent event, DataOutputStream out) throws IOException {
        out.write(event.type);
        event.write(out);
    }

    public static ServerEvent read(DataInputStream in) throws IOException{
        int type = in.read();
        return (ServerEvent) readers[type].read(in);
    }

    public abstract void executeImmediate(GameClient client);

    public static class NewGame extends ServerEvent {
        public NewGame() {
            super(NEW_GAME);
        }

        @Override
        protected void write(DataOutputStream out) throws IOException {

        }

        @Override
        public void execute(Game game) {
            game.restart();
        }

        @Override
        public void executeImmediate(GameClient client) {
            client.stopReaderThread();
        }
    }


}
