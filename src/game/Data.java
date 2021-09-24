package game;

public abstract class Data {

    public static final int DATA_SIZE = 2 * Double.BYTES + 1 + 4 * Double.BYTES + 1;

    public static final int BULLET_CREATED_SIZE = Integer.BYTES + 4 * Double.BYTES, BULLET_REMOVED_SIZE = Integer.BYTES;

    public static final int MAX_BULLETS = 256;
    public static final byte CREATED = 0, REMOVED = 1;
}
