package game.tiles;

import java.awt.image.BufferedImage;

public class Level {

    private final BufferedImage image, tileImage;

    public Level(BufferedImage image, BufferedImage tileImage) {
        this.image = image;
        this.tileImage = tileImage;
    }

    public BufferedImage getImage(){
        return image;
    }

    public TileMap getTileMap() {
        int w = tileImage.getWidth(), h = tileImage.getHeight();
        TileMap tileMap = new TileMap(w,h);
        tileMap.readFromImage(tileImage);
        tileMap.setImage(image);
        return tileMap;
    }
}
