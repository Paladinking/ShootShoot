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
import game.textures.StartupCounter;
import game.textures.Texture;
import game.tiles.TileMap;
import game.ui.GamePanel;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Game implements KeyListener, MouseMotionListener, PlayerListener, ProjectileListener, GameEventHandler {

    public static final int WIDTH = 96, HEIGHT = 50, TILE_SIZE = 20;

    private static final int INITIAL_EVENT_CAPACITY = 32, STARTUP_TICKS = 150;

    private final int width, height;

    private final Object eventLock = new Object(), textureLock = new Object();

    private final double scalingFactor;

    private final Map<Integer, Boolean> keyMap;

    private GamePanel panel;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?> tickFuture;

    private GameClient client;

    private final Map<Integer, Projectile> projectiles;

    private final List<Texture> textures;

    private TileMap tileMap;

    private final List<Player> players;

    private LocalPlayer localPlayer;

    private int playerNumber, startUpTicks;

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
        this.tileMap = new TileMap(0, 0);
        initKeys();
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

    public void connect(String ip, int port) throws IOException {
        this.client = new GameClient(ip, port, this);
        client.connect();
    }

    public void setLevel(int level) {
        this.tileMap = panel.getTileMap(level);
        this.tileMap.setTileSize(TILE_SIZE);
    }

    public void start(GamePanel panel) {
        this.panel = panel;
        startUpTicks = STARTUP_TICKS;
        client.init();
        client.startReaderThread();
        textures.add(new StartupCounter(width / 2 * TILE_SIZE, height / 2 * TILE_SIZE, startUpTicks + startUpTicks / 3));
        tickFuture = executor.scheduleAtFixedRate(()-> {
            tick();
            EventQueue.invokeLater(panel::repaint);
        }, 16, 16, TimeUnit.MILLISECONDS);
    }


    public void restart() {
        tickFuture.cancel(false);
        client.joinReaderThread();
        players.clear();
        projectiles.clear();
        synchronized (textureLock) {
            textures.clear();
        }
        events.clear();
        client.addEvent(GameEvent.newGameReady());
        try {
            client.sendData();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        start(panel);
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
        client.addEvent(new GameEvent.PlayerHurt(1));
        if (player.isDead()) client.addEvent(GameEvent.playerDied());
    }

    public void tick() {
        synchronized (eventLock) {
            while (!events.isEmpty()) {
                GameEvent event = events.remove();
                event.execute(this);
            }
        }
        if (startUpTicks == 0) {
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
        } else {
            startUpTicks--;
        }
        synchronized (textureLock) {
            for (int i = 0; i < textures.size(); i++) {
                Texture texture = textures.get(i);
                if (texture.tick()) {
                    textures.remove(i);
                    i--;
                }
            }
        }
        try {
            client.sendData();
        } catch (IOException e) {
            e.printStackTrace();
            tickFuture.cancel(false);
        }

    }

    public void draw(Graphics2D g) {
        g.scale(scalingFactor, scalingFactor);

        tileMap.draw(g);
        double hpFraction, staminaFraction, shootDelayFraction;
        synchronized (textureLock) {
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

    public void createProjectile(double x, double y, double dx, double dy, int type, int index, int source) {
        Projectile projectile;
        if (Projectile.isBullet(type)) {
            projectile = new Bullet(x, y, dx, dy, type, source, this);
        } else if (Projectile.isRocket(type)) {
            projectile = new Rocket(x, y, dx, dy);
        } else {
            projectile = new Bullet(x, y, dx, dy, 0, source, this);
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

    public void createPlayer(int x, int y, int number, boolean isLocalPlayer) {
        Player player;
        if (isLocalPlayer) {
            localPlayer = new LocalPlayer(x, y, TILE_SIZE * 2, number, this);
            player = localPlayer;
            playerNumber = number;
        } else {
            player = new Player(x, y, TILE_SIZE * 2, number);
        }
        players.add(player);
        textures.add(player.getTexture());
    }


    /*
     * These methods are running on a different thread than the rest of the file.
     *
     */

    public void addEvent(GameEvent event) {
        synchronized (eventLock) {
            events.add(event);
        }
    }
}
