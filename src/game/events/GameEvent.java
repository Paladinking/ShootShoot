package game.events;

import game.Game;
import game.server.PlayerHandler;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class GameEvent {

    private static final byte TOTAL_EVENTS = 11;

    protected static final byte
            PROJECTILE_CREATED = 0,
            PROJECTILE_REMOVED = 1,
            PLAYER_HURT = 2,
            PLAYER_MOVED = 3,
            PLAYER_DIED = 4,
            PLAYER_CHANGED_ANGLE = 6,
            NEW_GAME_READY = 5,
            PROJECTILE_EVENT = 7,
            SOUND_PLAYED = 9,
            TELEPORTER_USED = 10;

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
            boolean propagateBack = in.readBoolean();
            return new ProjectileRemoved(index, propagateBack);
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
            return new PlayerChangedAngle(angle);
        };
        readers[PLAYER_DIED] = in -> playerDied();
        readers[NEW_GAME_READY] = in -> newGameReady();
        readers[PROJECTILE_EVENT] = in -> {
            int id = in.readInt();
            return new ProjectileEvent(id);
        };
        readers[SOUND_PLAYED] = in -> {
            int id = in.readInt();
            return new SoundPlayed(id);
        };
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
        return read2(type, in);
    }

    @Deprecated
    public static GameEvent read1(int type, DataInput in) throws IOException {
        return readers[type].read(in);
    }

    private static GameEvent read2(int t, DataInput in) throws IOException {
        switch (t) {
            case PROJECTILE_CREATED -> {
                double x = in.readDouble();
                double y = in.readDouble();
                double dx = in.readDouble();
                double dy = in.readDouble();
                int type = in.readInt();
                int index = in.readInt();
                return new ProjectileCreated(x, y, dx, dy, type, index);
            }
            case PROJECTILE_REMOVED -> {
                int index = in.readInt();
                boolean propagateBack = in.readBoolean();
                return new ProjectileRemoved(index, propagateBack);
            }
            case PLAYER_HURT -> {
                int amount = in.readInt();
                return new PlayerHurt(amount);
            }
            case PLAYER_MOVED -> {
                double x = in.readDouble();
                double y = in.readDouble();
                double angle = in.readDouble();
                return new PlayerMoved(x, y, angle);
            }
            case PLAYER_CHANGED_ANGLE -> {
                double angle = in.readDouble();
                return new PlayerChangedAngle(angle);
            }
            case PLAYER_DIED -> {
                return playerDied();
            }
            case NEW_GAME_READY -> {
                return newGameReady();
            }
            case PROJECTILE_EVENT -> {
                int id = in.readInt();
                return new ProjectileEvent(id);
            }
            case SOUND_PLAYED -> {
                int id = in.readInt();
                return new SoundPlayed(id);
            }
            case TELEPORTER_USED -> {
                double x = in.readDouble();
                double y = in.readDouble();
                return new TeleporterPlaced(x, y);
            }
            default -> throw new IOException(""+t);
        }
    }

    public static void write(GameEvent event, DataOutputStream out) throws IOException {
        out.write(event.type);
        event.write(out);
    }

    public abstract void execute(Game game);

    public boolean handle(PlayerHandler handler) {
        return true;
    }

    protected interface EventReader {
        GameEvent read(DataInput in) throws IOException;
    }

    public static GameEvent playerDied() {
        return new EmptyEvent(PLAYER_DIED) {
            @Override
            public boolean handle(PlayerHandler handler) {
                handler.died();
                return false;
            }
        };
    }

    public static GameEvent newGameReady() {
        return new EmptyEvent(NEW_GAME_READY) {
            @Override
            public boolean handle(PlayerHandler handler) {
                handler.stopReaderThread();
                return false;
            }
        };
    }
}
