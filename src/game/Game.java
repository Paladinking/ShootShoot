package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Game implements KeyListener, MouseMotionListener, PlayerListener {

    public static final int WIDTH = 96, HEIGHT = 54, TILE_SIZE = 20;

    private final int width, height;

    private final Map<Integer, Boolean> keyMap;

    private final List<Bullet> bullets;

    private TileMap tileMap;

    private final List<Player> players;

    private int playerNumber;

    private final Point mousePos;


    boolean hasReceivedData;
    ByteBuffer receivedData, sendData;

    private boolean hasShot;

    private final Vector2d shotPos = new Vector2d(0, 0), shotVel = new Vector2d(0, 0);

    private InputStream in;
    private OutputStream out;

    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        this.keyMap = new HashMap<>();
        this.mousePos = new Point(0, 0);
        this.bullets = new ArrayList<>();
        this.players = new ArrayList<>();
    }


    private void initKeys(){
        keyMap.put(KeyEvent.VK_W, false);
        keyMap.put(KeyEvent.VK_A, false);
        keyMap.put(KeyEvent.VK_S, false);
        keyMap.put(KeyEvent.VK_D, false);
        keyMap.put(KeyEvent.VK_SPACE, false);
    }

    public void init(){
        tileMap = new TileMap(WIDTH, HEIGHT, TILE_SIZE);
        tileMap.readFromImage(getLevelImage());
        initKeys();
    }

    private BufferedImage getLevelImage(){
        try {
            return ImageIO.read(ClassLoader.getSystemResource("images/stage.png"));
        } catch (IOException e){
            return new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public void start(JPanel panel, InputStream inputStream, OutputStream outputStream) throws IOException {
        this.in = inputStream;
        this.out = outputStream;
        receiveInitialData();
        new Thread(this::receiveData).start();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
                EventQueue.invokeLater(panel::repaint);
            }
        }, 16, 16);
    }

    private void receiveData() {
        while (true) {
            try {
                byte[] data = in.readNBytes(receivedData.capacity());
                receivedData.clear().put(data);
                hasReceivedData = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void receiveInitialData() throws IOException {
        byte[] data = in.readNBytes(Integer.BYTES);
        byte numberOfPlayers = data[3];
        data = in.readNBytes(Integer.BYTES + 2 * Integer.BYTES * numberOfPlayers);
        final Color[] colors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
        for (int i = 0; i < numberOfPlayers; i++){
            int x = data[2 + 2 * Integer.BYTES * i] * 256 + data[3 + 2 * Integer.BYTES * i];
            int y = data[6 + 2 * Integer.BYTES * i] * 256 + data[7 + 2 * Integer.BYTES * i];
            System.out.println(x+ ", " + y);
            Player player = new Player(x, y, TILE_SIZE * 2, colors[i], this);
            players.add(player);
        }
        playerNumber = data[data.length -1];

        receivedData = ByteBuffer.allocate(PlayerHandler.DATA_SIZE * numberOfPlayers);
        sendData = ByteBuffer.allocate(PlayerHandler.DATA_SIZE);
    }

    @Override
    public void firedShot(Vector2d pos, Vector2d velocity) {
        hasShot = true;
        shotPos.set(pos.x, pos.y);
        shotVel.set(velocity.x, velocity.y);
    }

    public void tick(){
        hasShot = false;
        handleReceivedData();
        Player player = players.get(playerNumber);
        player.tick(tileMap, keyMap, mousePos);
        for (Bullet bullet : bullets) bullet.tick(tileMap);
        for (int i = 0; i < bullets.size(); i++){
            if (bullets.get(i).isDead()){
                bullets.remove(i);
                i--;
            }
        }
        sendData.clear();
        Vector2d pos = player.getPosition();
        sendData.putDouble(pos.getX()).putDouble(pos.getY());
        sendData.put((byte) (hasShot ? 1 : 0));
        sendData.putDouble(shotPos.x).putDouble(shotPos.y).putDouble(shotVel.x).putDouble(shotVel.y);
        sendData();
    }

    private void sendData() {
        try {
            out.write(sendData.array());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handleReceivedData() {
        if (!hasReceivedData) return;
        hasReceivedData = false;
        receivedData.position(0);
        for (int i = 0; i < players.size(); i++){
            Player player = players.get(i);

            double x = receivedData.getDouble();
            double y = receivedData.getDouble();
            System.out.println(x + ", " + y);
            player.setPosition(x, y);
            boolean hasShot = receivedData.get() != 0;
            double shotX = receivedData.getDouble();
            double shotY = receivedData.getDouble();
            double shotDx = receivedData.getDouble();
            double shotDy = receivedData.getDouble();
            if (hasShot){
                bullets.add(new Bullet(shotX, shotY, shotDx, shotDy));
            }
        }
    }


    public void draw(Graphics2D g, Dimension panelSize){
        //g.translate(panelSize.width / 2 - width * TILE_SIZE / 2, panelSize.height / 2 - height * TILE_SIZE / 2);
        double scalingFactor = panelSize.width / ((double) WIDTH * TILE_SIZE);
        g.scale(scalingFactor, scalingFactor);
        tileMap.draw(g);
        for (Player player: players) player.draw(g);
        for (int i = 0; i < bullets.size(); i++) bullets.get(i).draw(g);
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) System.exit(0);
        if (keyMap.containsKey(keyCode)) keyMap.put(keyCode, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyMap.containsKey(keyCode)) keyMap.put(keyCode, false);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePos.setLocation(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.setLocation(e.getX(), e.getY());
    }


}
