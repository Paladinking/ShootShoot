package game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class PlayerHandler {

    public static final int DATA_SIZE = 2 * Double.BYTES + 1 + 4 * Double.BYTES;

    private final OutputStream out;
    private final InputStream in;

    private double x, y;

    public boolean hasShot;

    private double shotX, shotY, shotDx, shotDy;

    public PlayerHandler(Socket socket) throws IOException {
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }

    public void start() {
        while (true) {
            try {
                byte[] data;
                data = in.readNBytes(DATA_SIZE);
                ByteBuffer buffer = ByteBuffer.wrap(data);
                x = buffer.getDouble();
                y = buffer.getDouble();
                hasShot = buffer.get() != 0 || hasShot;
                if (hasShot) {
                    shotX = buffer.getDouble();
                    shotY = buffer.getDouble();
                    shotDx = buffer.getDouble();
                    shotDy = buffer.getDouble();

                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public byte[] getData() {
        ByteBuffer buffer = ByteBuffer.allocate(DATA_SIZE);
        byte hasShot = (byte) (this.hasShot ? 1 : 0);
        this.hasShot = false;
        buffer.putDouble(x).putDouble(y).put(hasShot).putDouble(shotX).putDouble(shotY).putDouble(shotDx).putDouble(shotDy);
        return buffer.array();
    }

    public void sendData(byte[] data) throws IOException {
        out.write(data);
        out.flush();
    }

    public void sendInitialData(int index, int players, int x, int y) throws IOException {
        this.x = x;
        this.y = y;
        ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES + 2 * Integer.BYTES * players);
        buffer.putInt(players);
        for (int i = 0; i < players; i++) {
            buffer.putInt(x).putInt(y);
        }
        buffer.putInt(index);
        byte[] data = buffer.array();
        out.write(data);
        out.flush();
    }

    @Override
    public String toString() {
        return "PlayerHandler{" +
               "x=" + x +
               ", y=" + y +
               ", hasShot=" + hasShot +
               '}';
    }
}
