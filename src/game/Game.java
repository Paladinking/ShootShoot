package game;

import game.client.GameClient;
import game.entities.Player;
import game.entities.projectiles.Bullet;
import game.entities.LocalPlayer;
import game.entities.projectiles.Projectile;
import game.entities.projectiles.Rocket;
import game.events.GameEvent;
import game.items.weaponds.Weapon;
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

public class Game implements KeyListener, MouseMotionListener, PlayerListener, ProjectileListener {

    public static final int WIDTH = 96, HEIGHT = 50, TILE_SIZE = 20;

    private static final int INITIAL_EVENT_CAPACITY = 32;

    private final int width, height;

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

    private byte damageTaken;

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
        playerNumber = client.readInt();
        final Color[] colors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
        for (int i = 0; i < numberOfPlayers; i++) {
            int x = client.readInt(), y = client.readInt();
            Player player;
            if (i == playerNumber){
                localPlayer = new LocalPlayer(x, y, TILE_SIZE * 2, colors[i], this);
                player = localPlayer;
            } else {
                player = new Player(x,y, TILE_SIZE * 2, colors[i]);
            }
            players.add(player);
            textures.add(player.getTexture());
        }
    }

    @Override
    public void playerUsedWeapon(Weapon weapon, LocalPlayer player) {
        Vector2d pos = player.getPosition();
        weapon.use(events,tileMap, pos, mousePos);
    }

    @Override
    public void playerMoved(Vector2d pos){
        events.add(new GameEvent.PlayerMoved(pos.x, pos.y));
    }

    @Override
    public void hitPlayer(LocalPlayer player) {
        player.hurt(1);
        damageTaken++;
    }

    public synchronized void tick() {
        damageTaken = 0;
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
                events.add(new GameEvent.ProjectileRemoved(entry.getKey()));
            }
        }
        for (int i = 0; i < textures.size(); i++){
            Texture texture = textures.get(i);
            if (texture.tick()) {
                textures.remove(i);
                i--;
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
        double hpFraction, staminaFraction, shootDelayFraction;
        synchronized (this) {
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


    /*
     * These methods are running on a different thread than the rest of the file.
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

    public void createProjectile(double x, double y, double dx, double dy, int type, int index) {
        Projectile projectile;
        if (Projectile.isBullet(type)){
            projectile = new Bullet(x, y, dx, dy, type, this);
        } else if (Projectile.isRocket(type)) {
            projectile = new Rocket(x, y, dx, dy);
        } else {
            projectile = new Bullet(x, y, dx, dy, 0, this);
        }
        synchronized (this) {
            projectiles.put(index, projectile);
            textures.add(projectile.getTexture());
        }
    }

    public void removeProjectile(int index, int source) {
        if (source == playerNumber) return;
        synchronized (this) {
            projectiles.remove(index);
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
