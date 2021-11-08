package game.items;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ItemSet {

    public static final int SNIPER = 0, SHOTGUN = 1, MINE_PLACER = 2, TELEPORTER = 3;

    private static final int SHOOT_DELAY = 100, BULLET_SPEED = 100, SHOTGUN_SHOTS = 3;

    private final Item[] items;
    private static final BufferedImage[] icons;

    static {
        icons = new BufferedImage[4];
        try {
            icons[SNIPER] = ImageIO.read(ClassLoader.getSystemResource("images/icons/SniperIcon.png"));
            icons[SHOTGUN] = ImageIO.read(ClassLoader.getSystemResource("images/icons/ShotgunIcon.png"));
            icons[MINE_PLACER] = ImageIO.read(ClassLoader.getSystemResource("images/icons/MineIcon.png"));
            icons[TELEPORTER] = ImageIO.read(ClassLoader.getSystemResource("images/icons/TeleporterIcon.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private int activeItem, secondaryItem;

    public ItemSet() {
        items = new Item[4];
        items[SNIPER] = new Sniper(SHOOT_DELAY, BULLET_SPEED);
        items[SHOTGUN] = new Shotgun(SHOOT_DELAY, BULLET_SPEED / 3, SHOTGUN_SHOTS);
        items[MINE_PLACER] = new MinePlacer(SHOOT_DELAY);
        items[TELEPORTER] = new Teleporter(SHOOT_DELAY);
        activeItem = SNIPER;
        secondaryItem = MINE_PLACER;
    }

    public void setPrimaryItem(int itemId) {
        this.activeItem = itemId;
    }

    public void setSideItem(int itemId) {
        this.secondaryItem = itemId;
    }

    public Item getPrimaryItem(){
        return items[activeItem];
    }

    public Item getSideItem() {
        return items[secondaryItem];
    }

    public void tick() {
        items[activeItem].tick();
        items[secondaryItem].tick();
    }

    public Teleporter getTeleporter() {
        return (Teleporter) items[TELEPORTER];
    }

    public BufferedImage[] getIcons(){
        return icons;
    }

    public int getPrimary() {
        return activeItem;
    }

    public int getSecondary() {
        return secondaryItem;
    }
}
