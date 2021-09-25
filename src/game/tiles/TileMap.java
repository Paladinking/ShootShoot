package game.tiles;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TileMap {

    public int getTileSize() {
        return tileSize;
    }

    public boolean isOpen(Vector2d nextPosition) {
        return isOpen(((int)nextPosition.x) / tileSize, ((int) nextPosition.y) / tileSize);
    }

    private enum TileType {
        EMPTY, WALL
    }

    private final int tileSize;

    private final TileType[][] tiles;

    public TileMap(int width, int height, int tileSize) {
        this.tiles = new TileType[height][];
        for (int i = 0; i < height; i++){
            tiles[i] = new TileType[width];
            Arrays.fill(tiles[i], TileType.EMPTY);
        }
        this.tileSize = tileSize;
    }

    public void draw(Graphics2D g){
        for (int y = 0; y < tiles.length; y++){
            for (int x = 0; x < tiles[0].length; x++){
                Color c = switch (tiles[y][x]){
                    case EMPTY -> Color.WHITE;
                    case WALL -> Color.BLACK;
                };
                g.setColor(c);
                g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
    }

    public void readFromImage(BufferedImage image){
        for (int y = 0; y < tiles.length; y++){
            for (int x = 0; x < tiles[0].length; x++){
                int pixel = image.getRGB(x, y);
                switch (pixel) {
                    case 0xff000000 -> tiles[y][x] = TileType.WALL;
                    case 0xffffffff -> tiles[y][x] = TileType.EMPTY;
                    default -> System.out.println(Integer.toHexString(pixel));
                }
            }
        }
    }

    public boolean isOpen(int x, int y){
        return tiles[y][x] == TileType.EMPTY;
    }
}
