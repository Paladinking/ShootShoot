package game.tiles;

import java.awt.image.BufferedImage;

public class Level {

    private final BufferedImage image;

    public Level(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage(){
        return image;
    }

    public TileMap getTileMap() {
        int w = image.getWidth(), h = image.getHeight();
        TileMap tileMap = new TileMap(w,h);
        tileMap.readFromImage(image);
        return tileMap;
    }
}
