package game;

import game.client.GameClient;
import game.entities.Bullet;
import game.entities.Player;
import game.events.GameEvent;
import game.listeners.BulletListener;
import game.listeners.PlayerListener;
import game.tiles.TileMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Game implements KeyListener, MouseMotionListener, PlayerListener, BulletListener {

    public static final int WIDTH = 96, HEIGHT = 54, TILE_SIZE = 20;

    private static final int INITIAL_EVENT_CAPACITY = 32;

    private final int width, height;

    private final double scalingFactor;

    private final Map<Integer, Boolean> keyMap;

    private Timer timer;

    private GameClient client;

    private final Map<Integer, Bullet> bullets;

    private TileMap tileMap;

    private final List<Player> players;

    private int playerNumber;

    private final Point mousePos;

    private byte damageTaken;

    private final Queue<GameEvent> events;

    public Game(int width, int height, int pixelWidth, int pixelHeight) {
        this.width = width;
        this.height = height;
        this.scalingFactor = pixelWidth / ((double) width * TILE_SIZE);
        this.keyMap = new HashMap<>();
        this.mousePos = new Point(0, 0);
        this.bullets = new HashMap<>();
        this.players = new ArrayList<>();
        this.events = new ArrayDeque<>(INITIAL_EVENT_CAPACITY);
    }


    private void initKeys() {
        keyMap.put(KeyEvent.VK_W, false);
        keyMap.put(KeyEvent.VK_A, false);
        keyMap.put(KeyEvent.VK_S, false);
        keyMap.put(KeyEvent.VK_D, false);
        keyMap.put(KeyEvent.VK_SPACE, false);
    }

    public void init() {
        tileMap = new TileMap(WIDTH, HEIGHT, TILE_SIZE);
        tileMap.readFromImage(getLevelImage());
        initKeys();
    }

    private BufferedImage getLevelImage() {
        try {
            return ImageIO.read(ClassLoader.getSystemResource("images/stage.png"));
        } catch (IOException e) {
            return new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public void start(JPanel panel, String ip, int port) throws IOException {
        this.client = new GameClient(ip, port);
        client.connect();
        receiveInitialData();
        new Thread(this::receiveData).start();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
                EventQueue.invokeLater(panel::repaint);
            }
        }, 16, 16);
    }


    private void receiveInitialData() throws IOException {
        int numberOfPlayers = client.readInt();
        final Color[] colors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
        for (int i = 0; i < numberOfPlayers; i++) {
            int x = client.readInt(), y = client.readInt();
            Player player = new Player(x, y, TILE_SIZE * 2, colors[i], this);
            players.add(player);
        }
        playerNumber = client.readInt();
    }

    @Override
    public void playerFiredShot(Vector2d pos, Vector2d velocity) {
        events.add(new GameEvent.BulletCreated(pos.x, pos.y, velocity.x, velocity.y));
    }

    @Override
    public void playerMoved(Vector2d pos){
        events.add(new GameEvent.PlayerMoved(pos.x, pos.y));
    }

    @Override
    public void shotPlayer(Player player) {
        player.hurt(1);
        damageTaken++;
    }

    public synchronized void tick() {
        damageTaken = 0;
        Player player = players.get(playerNumber);
        for (Player p : players) {
            p.tick(tileMap, keyMap, mousePos, p == player);
        }
        Iterator<Map.Entry<Integer, Bullet>> it = bullets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Bullet> entry = it.next();
            Bullet bullet = entry.getValue();
            bullet.tick(tileMap, player);
            if (bullet.isDead()) {
                it.remove();
                events.add(new GameEvent.BulletRemoved(entry.getKey()));
            }
        }
        if (damageTaken > 0) events.add(new GameEvent.PlayerHurt(damageTaken));
        try {
            client.sendData(events);
        } catch (IOException e) {
            e.printStackTrace();
            timer.cancel();
        }
    }

    public void draw(Graphics2D g) {
        g.scale(scalingFactor, scalingFactor);
        tileMap.draw(g);
        synchronized (this) {
            for (Player player : players) if (!player.isDead()) player.draw(g);
            for (Bullet bullet : bullets.values()) bullet.draw(g);
        }
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
        mousePos.x *= scalingFactor;
        mousePos.y *= scalingFactor;
    }


    /*
     * These methods are running on a different thread than the rest of the program.
     *
     */


    private void receiveData() {
        try {
            while (true) {
                GameEvent[][] events = new GameEvent[players.size()][];
                client.receiveData(players.size(), events);
                for (int i = 0; i < events.length; i++){
                    executeEvents(i, events[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeEvents(int player, GameEvent[] events){
        for (GameEvent event : events){
            event.execute(this, player);
        }
    }

    public void createBullet(double xPos, double yPos, double xVel, double yVel, int index) {
        synchronized (this) {
            bullets.put(index, new Bullet(new Vector2d(xPos, yPos), new Vector2d(xVel, yVel), this));
        }
    }

    public void removeBullet(int index, int source) {
        if (source == playerNumber) return;
        synchronized (this) {
            bullets.remove(index);
        }
    }

    public void hurtPlayer(int amount, int source){
        if (source == playerNumber) return;
        synchronized (this) {
            players.get(source).hurt(amount);
        }
    }

    public void movePlayer(double newX, double newY, int source){
        if (source == playerNumber) return;
        synchronized (this) {
            players.get(source).setPosition(newX, newY);
        }
    }



}
