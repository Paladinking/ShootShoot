package game.events;

import game.Game;
import game.server.PlayerHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class GameEvent {

    private static final byte TOTAL_EVENTS = 4;

    private static final byte PROJECTILE_CREATED = 0, PROJECTILE_REMOVED = 1, PLAYER_HURT = 2, PLAYER_MOVED = 3;

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
            return new PlayerMoved(x, y);
        };
    }

    public abstract void write(DataOutputStream out) throws IOException;

    public static GameEvent read(DataInputStream in) throws IOException {
        int type = in.read();

        return readers[type].read(in);
    }

    public abstract void execute(Game game, int source);

    public void handle(PlayerHandler handler) {

    }

    private interface EventReader {
        GameEvent read(DataInputStream in) throws IOException;
    }

    public static class ProjectileCreated extends GameEvent {
        private final double xPos, yPos, xVel, yVel;
        private final int type;

        private int index;

        public ProjectileCreated(double xPos, double yPos, double xVel, double yVel, int type) {
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
        public void write(DataOutputStream out) throws IOException {
            out.write(PROJECTILE_CREATED);
            out.writeDouble(xPos);
            out.writeDouble(yPos);
            out.writeDouble(xVel);
            out.writeDouble(yVel);
            out.writeInt(type);
            out.writeInt(index);
        }

        @Override
        public void execute(Game game, int source) {
            game.createProjectile(xPos, yPos, xVel, yVel, type, index);
        }

        @Override
        public void handle(PlayerHandler handler){
            this.index = PlayerHandler.getBulletIndex();
        }



    }

    public static class ProjectileRemoved extends GameEvent {

        private final int index;

        public ProjectileRemoved(int index){
            this.index = index;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            out.write(PROJECTILE_REMOVED);
            out.writeInt(index);
        }

        @Override
        public void execute(Game game, int source) {
            game.removeProjectile(index, source);
        }
    }

    public static class PlayerHurt extends GameEvent {

        private final int amount;

        public PlayerHurt(int amount) {
            this.amount = amount;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            out.write(PLAYER_HURT);
            out.writeInt(amount);
        }

        @Override
        public void execute(Game game, int source) {
            game.hurtPlayer(amount, source);
        }
    }

    public static class PlayerMoved extends GameEvent {

        private final double newX, newY;

        public PlayerMoved(double newX, double newY) {
            this.newX = newX;
            this.newY = newY;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            out.write(PLAYER_MOVED);
            out.writeDouble(newX);
            out.writeDouble(newY);
        }

        @Override
        public void execute(Game game, int source) {
            game.movePlayer(newX, newY, source);
        }

        @Override
        public String toString() {
            return "PlayerMoved{" +
                   "x=" + newX +
                   ", y=" + newY +
                   '}';
        }
    }
}
