package game;

import game.client.GameClient;
import game.entities.Player;
import game.entities.projectiles.Bullet;
import game.entities.LocalPlayer;
import game.entities.projectiles.Projectile;
import game.entities.projectiles.Rocket;
import game.events.GameEvent;
import game.items.weaponds.Weapon;
import game.listeners.GameEventHandler;
import game.listeners.ProjectileListener;
import game.listeners.PlayerListener;
import game.textures.Texture;
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

public class Game implements KeyListener, MouseMotionListener, PlayerListener, ProjectileListener, GameEventHandler {

    public static final int WIDTH = 96, HEIGHT = 50, TILE_SIZE = 20;

    private static final int INITIAL_EVENT_CAPACITY = 32;

    private final int width, height;

    private volatile boolean receivingData;

    private boolean running;

    private final Object lock = new Object();

    private Thread readerThread;

    private final double scalingFactor;

    private final Map<Integer, Boolean> keyMap;

    private Timer timer;

    private GameClient client;

    private final Map<Integer, Projectile> projectiles;

    private final List<Texture> textures;

    private TileMap tileMap;

    private final List<Player> players;

    private LocalPlayer localPlayer;

    private int playerNumber;

    private final Point mousePos;

    private final Queue<GameEvent> events;

    public Game(int width, int height, int pixelWidth, int pixelHeight) {
        this.width = width;
        this.height = height;
        this.scalingFactor = pixelWidth / ((double) width * TILE_SIZE);
        this.keyMap = new HashMap<>();
        this.mousePos = new Point(0, 0);
        this.projectiles = new HashMap<>();
        this.players = new ArrayList<>();
        this.textures = new ArrayList<>();
        this.events = new ArrayDeque<>(INITIAL_EVENT_CAPACITY);
    }


    private void initKeys() {
        keyMap.put(KeyEvent.VK_W, false);
        keyMap.put(KeyEvent.VK_A, false);
        keyMap.put(KeyEvent.VK_S, false);
        keyMap.put(KeyEvent.VK_D, false);
        keyMap.put(KeyEvent.VK_SPACE, false);
        keyMap.put(KeyEvent.VK_SHIFT, false);
        keyMap.put(KeyEvent.VK_1, false);
        keyMap.put(KeyEvent.VK_2, false);
    }

    public void init() {
        tileMap = new TileMap(WIDTH, HEIGHT, TILE_SIZE);
        tileMap.readFromImage(getLevelImage());
        initKeys();
    }

    private BufferedImage getLevelImage() {
        try {
            return ImageIO.read(ClassLoader.getSystemResource("images/stage2.png"));
        } catch (IOException e) {
            return new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public void start(JPanel panel, String ip, int port) throws IOException {
        this.client = new GameClient(ip, port);
        client.connect();
        receiveInitialData();
        running = true;
        receivingData = true;
        readerThread = new Thread(this::receiveData);
        readerThread.start();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
                EventQueue.invokeLater(panel::repaint);
            }
        }, 16, 16);
    }


    public void restart() {
        timer.cancel();
        receivingData = false;
        running = false;
        while (readerThread.isAlive()) {
            try {
                readerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void receiveInitialData() throws IOException {
        int numberOfPlayers = client.readInt();
        playerNumber = client.readInt();
        final Color[] colors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
        for (int i = 0; i < numberOfPlayers; i++) {
            int x = client.readInt(), y = client.readInt();
            Player player;
            if (i == playerNumber) {
                localPlayer = new LocalPlayer(x, y, TILE_SIZE * 2, colors[i], this);
                player = localPlayer;
            } else {
                player = new Player(x, y, TILE_SIZE * 2, colors[i]);
            }
            players.add(player);
            textures.add(player.getTexture());
        }
    }

    @Override
    public void playerUsedWeapon(Weapon weapon, LocalPlayer player) {
        Vector2d pos = player.getPosition();
        weapon.use(client, tileMap, pos, mousePos);
    }

    @Override
    public void playerMoved(Vector2d pos) {
        client.addEvent(new GameEvent.PlayerMoved(pos.x, pos.y));
    }

    @Override
    public void hitPlayer(LocalPlayer player) {
        player.hurt(1);
    }

    public void tick() {
        synchronized (lock) {
            while (!events.isEmpty()) {
                GameEvent event = events.remove();
                event.execute(this);
            }
            if (running) {
                for (Player p : players) {
                    p.tick(tileMap, keyMap);
                }
                Iterator<Map.Entry<Integer, Projectile>> it = projectiles.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Projectile> entry = it.next();
                    Projectile projectile = entry.getValue();
                    projectile.tick(tileMap, localPlayer);
                    if (projectile.isDead()) {
                        it.remove();
                        client.addEvent(new GameEvent.ProjectileRemoved(entry.getKey()));
                    }
                }
                for (int i = 0; i < textures.size(); i++) {
                    Texture texture = textures.get(i);
                    if (texture.tick()) {
                        textures.remove(i);
                        i--;
                    }
                }
                int damageTaken = localPlayer.getDamageTaken();
                if (damageTaken > 0) {
                    client.addEvent(new GameEvent.PlayerHurt(damageTaken));
                    if (localPlayer.isDead()) {
                        client.addEvent(GameEvent.playerDied());
                    }
                }
            }
            try {
                client.sendData();
            } catch (IOException e) {
                e.printStackTrace();
                timer.cancel();
            }
        }
    }

    public void draw(Graphics2D g) {
        g.scale(scalingFactor, scalingFactor);
        tileMap.draw(g);
        double hpFraction, staminaFraction, shootDelayFraction;
        synchronized (lock) {
            for (Texture texture : textures) texture.draw(g);
            hpFraction = localPlayer.getHpFraction();
            staminaFraction = localPlayer.getStaminaFraction();
            shootDelayFraction = localPlayer.getShootDelayFraction();
        }
        g.setColor(Color.RED);
        g.fillRect(10, TILE_SIZE * height + 10, 200, 20);
        g.setColor(Color.GREEN);
        g.fillRect(10, TILE_SIZE * height + 10, (int) (hpFraction * 200), 20);

        g.setColor(Color.BLACK);
        g.fillRect(250, TILE_SIZE * height + 10, 200, 20);
        g.setColor(Color.BLUE);
        g.fillRect(250, TILE_SIZE * height + 10, (int) (staminaFraction * 200), 20);

        g.setColor(Color.BLACK);
        g.fillRect(480, TILE_SIZE * height + 10, 200, 20);
        g.setColor(Color.ORANGE);
        g.fillRect(480, TILE_SIZE * height + 10, (int) (shootDelayFraction * 200), 20);
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
        mousePos.x /= scalingFactor;
        mousePos.y /= scalingFactor;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos.setLocation(e.getX(), e.getY());
        mousePos.x /= scalingFactor;
        mousePos.y /= scalingFactor;
    }

    public void createProjectile(double x, double y, double dx, double dy, int type, int index) {
        Projectile projectile;
        if (Projectile.isBullet(type)) {
            projectile = new Bullet(x, y, dx, dy, type, this);
        } else if (Projectile.isRocket(type)) {
            projectile = new Rocket(x, y, dx, dy);
        } else {
            projectile = new Bullet(x, y, dx, dy, 0, this);
        }
        projectiles.put(index, projectile);
        textures.add(projectile.getTexture());

    }

    public void removeProjectile(int index, int source) {
        if (source == playerNumber) return;
        projectiles.remove(index);

    }

    public void hurtPlayer(int amount, int source) {
        if (source == playerNumber) return;
        players.get(source).hurt(amount);

    }

    public void movePlayer(double newX, double newY, int source) {
        if (source == playerNumber) return;
        players.get(source).setPosition(newX, newY);
    }


    public void addEvent(GameEvent event) {
        events.add(event);
    }


    /*
     * These methods are running on a different thread than the rest of the file.
     *
     */


    private void receiveData() {
        try {
            while (receivingData) {
                client.receiveData(players.size(), this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            timer.cancel();
        }
    }

    public void stopReaderThread() {
        receivingData = false;
    }
}
