package game.events;

import game.Game;
import game.server.PlayerHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class GameEvent {

    private static final byte TOTAL_EVENTS = 7;

    protected static final byte
            PROJECTILE_CREATED = 0,
            PROJECTILE_REMOVED = 1,
            PLAYER_HURT = 2,
            PLAYER_MOVED = 3,
            PLAYER_DIED = 4,
            PLAYER_CHANGED_ANGLE = 6,
            NEW_GAME_READY = 5;

    private static final EventReader[] readers = new EventReader[TOTAL_EVENTS];

    static {
        readers[PROJECTILE_CREATED] = in -> {
            double x = in.readDouble();
            double y = in.readDouble();
            double dx = in.readDouble();
            double dy = in.readDouble();
            int type = in.readInt();
            int index = in.readInt();
            return new ProjectileCreated(x, y, dx, dy, type, index);
        };
        readers[PROJECTILE_REMOVED] = in -> {
            int index = in.readInt();
            return new ProjectileRemoved(index);
        };
        readers[PLAYER_HURT] = in -> {
            int amount = in.readInt();
            return new PlayerHurt(amount);
        };
        readers[PLAYER_MOVED] = in -> {
            double x = in.readDouble();
            double y = in.readDouble();
            double angle = in.readDouble();
            return new PlayerMoved(x, y, angle);
        };
        readers[PLAYER_CHANGED_ANGLE] = in -> {
            double angle = in.readDouble();
            return  new PlayerChangedAngle(angle);
        };
        readers[PLAYER_DIED] = in -> playerDied();
        readers[NEW_GAME_READY] = in -> newGameReady();
    }

    protected final int type;

    public final boolean propagateBack;

    public int source;

    protected GameEvent(int type, boolean propagateBack) {
        this.type = type;
        this.propagateBack = propagateBack;
    }

    protected abstract void write(DataOutputStream out) throws IOException;

    public static GameEvent read(DataInputStream in) throws IOException {
        int type = in.read();
        return readers[type].read(in);
    }

    public static void write(GameEvent event, DataOutputStream out) throws IOException {
        out.write(event.type);
        event.write(out);
    }

    public abstract void execute(Game game);

    public void handle(PlayerHandler handler) {

    }

    protected interface EventReader {
        GameEvent read(DataInputStream in) throws IOException;
    }

    public static GameEvent playerDied() {
        return new EmptyEvent(PLAYER_DIED) {
            @Override
            public void handle(PlayerHandler handler) {
                handler.died();
            }
        };
    }

    public static GameEvent newGameReady() {
        return new EmptyEvent(NEW_GAME_READY) {
            @Override
            public void handle(PlayerHandler handler) {
                handler.stopReaderThread();
            }
        };
    }
}
