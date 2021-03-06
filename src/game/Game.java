package game;

import game.client.GameClient;
import game.entities.Player;
import game.entities.LocalPlayer;
import game.entities.projectiles.Projectile;
import game.events.GameEvent;
import game.events.PlayerChangedAngle;
import game.events.PlayerHurt;
import game.events.ProjectileRemoved;
import game.items.ItemSet;
import game.listeners.GameObjectHandler;
import game.sound.Sound;
import game.textures.StartupCounter;
import game.textures.Texture;
import game.tiles.Level;
import game.tiles.TileMap;
import game.ui.GamePanel;

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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Game implements KeyListener, MouseMotionListener, GameObjectHandler {

    public static final int WIDTH = 96, HEIGHT = 50, TILE_SIZE = 20;

    private static final int INITIAL_EVENT_CAPACITY = 32, STARTUP_TICKS = 150;

    private final int width, height;

    private final Object eventLock = new Object(), textureLock = new Object(), localEventLock = new Object();

    private final double scalingFactor;

    private final Level[] levels;

    private final Sound[] sounds;

    private final Map<Integer, Boolean> keyMap;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private ScheduledFuture<?> tickFuture;

    private GameClient client;

    private final Map<Integer, Projectile> projectiles;
    private final List<Player> players;
    private LocalPlayer localPlayer;
    private final List<Texture> textures;

    private TileMap tileMap;

    boolean friendlyFire;

    private int startUpTicks;

    private final Point mousePos;

    private final Queue<GameEvent> events;
    private GamePanel panel;

    public Game(int width, int height, int pixelWidth, int pixelHeight, Level[] levels, Sound[] sounds) {
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
        this.levels = levels;
        this.sounds = sounds;
        initKeys();
    }


    private void initKeys() {
        keyMap.put(KeyEvent.VK_W, false);
        keyMap.put(KeyEvent.VK_A, false);
        keyMap.put(KeyEvent.VK_S, false);
        keyMap.put(KeyEvent.VK_D, false);
        keyMap.put(KeyEvent.VK_Q, false);
        keyMap.put(KeyEvent.VK_SPACE, false);
        keyMap.put(KeyEvent.VK_SHIFT, false);
        keyMap.put(KeyEvent.VK_1, false);
        keyMap.put(KeyEvent.VK_2, false);
        keyMap.put(KeyEvent.VK_3, false);
        keyMap.put(KeyEvent.VK_4, false);
    }

    public void connect(String ip, int port) throws IOException {
        this.client = new GameClient(ip, port, this);
        client.connect();
    }

    public void setOptions(int level, boolean friendlyFire) {
        this.tileMap = levels[level].getTileMap();
        this.tileMap.setTileSize(TILE_SIZE);
        this.friendlyFire = friendlyFire;
    }

    public void start(GamePanel gamePanel) {
        this.panel = gamePanel;
        startUpTicks = STARTUP_TICKS;
        client.init();
        client.startReaderThread();
        textures.add(new StartupCounter(width / 2 * TILE_SIZE, height / 2 * TILE_SIZE, startUpTicks + startUpTicks / 3));
        tickFuture = executor.scheduleAtFixedRate(() -> {
            try {
                this.tick();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            EventQueue.invokeLater(gamePanel::repaint);
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
        start(this.panel);
    }

    @Override
    public void hurtPlayer(LocalPlayer player, int amount) {
        if (player.isDead() || player.isInvincible()) return;
        player.hurt(amount);
        client.addEvent(new PlayerHurt(1));
        if (player.isDead()) client.addEvent(GameEvent.playerDied());
    }

    @Override
    public boolean doFriendlyFire() {
        return friendlyFire;
    }

    @Override
    public void createEvent(GameEvent event) {
        client.addEvent(event);
    }

    public void tick() {
        synchronized (eventLock) {
            while (!events.isEmpty()) {
                GameEvent event = events.remove();
                event.execute(this);
            }
        }
        if (startUpTicks == 0) {
            synchronized (localEventLock) {
                for (Player p : players) {
                    p.tick(tileMap, keyMap, mousePos, this);
                }
            }
            Iterator<Map.Entry<Integer, Projectile>> it = projectiles.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Projectile> entry = it.next();
                Projectile projectile = entry.getValue();
                Projectile.Status status = projectile.tick(tileMap, localPlayer, entry.getKey());
                if (status != Projectile.Status.ALIVE) {
                    switch (status) {
                        case DEAD_NOT_PREDICTABLE:
                            client.addEvent(new ProjectileRemoved(entry.getKey()));
                        case DEAD_PREDICTABLE:
                            it.remove();
                            projectile.removed();
                            break;
                        case REPLACED:
                            Projectile newValue = projectile.getReplacement();
                            entry.setValue(newValue);
                            synchronized (textureLock) {
                                textures.add(newValue.getTexture());
                            }
                            break;
                    }
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
        final double hpFraction, staminaFraction, primaryShootDelayFraction, secondaryShootDelayFraction;
        synchronized (textureLock) {
            for (Texture texture : textures) texture.draw(g);
        }
        hpFraction = localPlayer.getHpFraction();
        staminaFraction = localPlayer.getStaminaFraction();

        final ItemSet items = localPlayer.getItems();
        primaryShootDelayFraction = items.getPrimaryItem().getDelayFraction();
        secondaryShootDelayFraction = items.getSideItem().getDelayFraction();

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
        g.fillRect(480, TILE_SIZE * height + 10, (int) (primaryShootDelayFraction * 200), 20);

        g.setColor(Color.BLACK);
        g.fillRect(480, TILE_SIZE * height + 40, 200, 20);
        g.setColor(Color.ORANGE);
        g.fillRect(480, TILE_SIZE * height + 40, (int) (secondaryShootDelayFraction * 200), 20);

        final int primarySelected = items.getPrimary(), secondarySelected = items.getSecondary();

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawRect(745 + 60 * primarySelected, TILE_SIZE * height + 5, 60, 60);
        g.drawRect(865 + 60 * secondarySelected, TILE_SIZE * height + 5, 60, 60);

        BufferedImage[] icons = items.getIcons();
        for (int i = 0; i < icons.length / 2; i++) {
            g.drawImage(icons[i], 750 + 60 * i, TILE_SIZE * height + 10, 50, 50, null);
        }
        for (int i = icons.length / 2; i < icons.length; i++) {
            g.drawImage(icons[i], 870 + 60 * i, TILE_SIZE * height + 10, 50, 50, null);
        }


    }


    public void createProjectile(double x, double y, double dx, double dy, int type, int index, int source) {
        Projectile projectile = Projectile.getProjectile(x, y, dx, dy, type, source, this);
        projectiles.put(index, projectile);
        synchronized (textureLock) {
            textures.add(projectile.getTexture());
        }

    }

    public void removeProjectile(int index) {
        projectiles.remove(index).removed();
    }


    public void affectProjectile(int id) {
        projectiles.get(id).projectileEvent(id);
    }

    public void hurtPlayer(int amount, int source) {
        players.get(source).hurt(amount);
    }

    public void setPlayerAngle(double angle, int source) {
        players.get(source).setAngle(angle);
    }

    public void movePlayer(double newX, double newY, int source) {
        players.get(source).setPosition(newX, newY);
    }

    public void createPlayer(int x, int y, int number, boolean isLocalPlayer) {
        Player player;
        if (isLocalPlayer) {
            localPlayer = new LocalPlayer(x, y, TILE_SIZE * 2, number);
            player = localPlayer;
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

    @Override
    public void playSound(int sound) {
        if (sound != -1) sounds[sound].play();
    }

    @Override
    public LocalPlayer getPlayer() {
        return localPlayer;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) System.exit(0);
        synchronized (localEventLock) {
            if (keyMap.containsKey(keyCode)) keyMap.put(keyCode, true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        synchronized (localEventLock) {
            if (keyMap.containsKey(keyCode)) keyMap.put(keyCode, false);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePositionChanged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePositionChanged(e);
    }

    private void mousePositionChanged(MouseEvent e) {
        double x = e.getX() / scalingFactor, y = e.getY() / scalingFactor;
        Vector2d pos = localPlayer.getPosition();
        double angle = Math.atan2(y - pos.y, x - pos.x);
        synchronized (localEventLock) {
            mousePos.setLocation(x, y);
            localPlayer.setAngle(angle);
        }
        client.addEvent(new PlayerChangedAngle(angle));
    }


    // Unused event...
    @Override
    public void keyTyped(KeyEvent e) {

    }

}
