package game.tiles;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class TileMap {

    // The Point class is used to represent tiles, while Vector2d is used to represent exact positions.

    public TileMap(int width, int height) {
        this.tiles = new TileType[height][];
        for (int i = 0; i < height; i++) {
            tiles[i] = new TileType[width];
        }
    }

    public int getTileSize() {
        return tileSize;
    }

    public boolean isOpen(Vector2d nextPosition) {
        return isOpen(((int) nextPosition.x) / tileSize, ((int) nextPosition.y) / tileSize);
    }

    private enum TileType {
        EMPTY, WALL
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    private int tileSize;

    private final TileType[][] tiles;


    public void draw(Graphics2D g) {
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                Color c = switch (tiles[y][x]) {
                    case EMPTY -> Color.WHITE;
                    case WALL -> Color.BLACK;
                };
                g.setColor(c);
                g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
    }

    public void readFromImage(BufferedImage image) {
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                int pixel = image.getRGB(x, y);
                switch (pixel) {
                    case 0xff000000 -> tiles[y][x] = TileType.WALL;
                    case 0xffffffff -> tiles[y][x] = TileType.EMPTY;
                    default -> System.out.println(Integer.toHexString(pixel));
                }
            }
        }
    }

    public boolean isOpen(int x, int y) {
        return tiles[y][x] == TileType.EMPTY;
    }

    public void adjustPosition(Vector2d nextPosition, int radius) {
        for (int i = 0; i < 40; i++) {
            List<Point> intersects = allIntersects(nextPosition, radius);
            if (intersects.size() == 0) return;
            Vector2d overlap = getAverageOverlapVector(nextPosition, radius, intersects);
            if (Double.isNaN(overlap.x)) return; // Should never happen but just in case...
            nextPosition.add(overlap);
        }
    }

    private List<Point> allIntersects(Vector2d pos, int radius) {
        List<Point> intersects = new ArrayList<>();
        for (int x = ((int) pos.x - radius) / tileSize; x <= ((int) pos.x + radius) / tileSize; x++) {
            for (int y = ((int) pos.y - radius) / tileSize; y <= ((int) pos.y + radius) / tileSize; y++) {
                if (tiles[y][x] == TileType.WALL && intersectsCircle(pos, x, y, radius)) {
                    intersects.add(new Point(x, y));
                }
            }
        }
        return intersects;
    }

    private boolean intersectsCircle(Vector2d pos, int x, int y, int radius) {
        double nx = clamp(x * tileSize, (x + 1) * tileSize, pos.x);
        double ny = clamp(y * tileSize, (y + 1) * tileSize, pos.y);
        double rx = nx - pos.x, ry = ny - pos.y;
        return radius * radius > rx * rx + ry * ry;
    }

    private Vector2d getAverageOverlapVector(Vector2d pos, int radius, List<Point> points) {
        Vector2d average = new Vector2d(0, 0);
        for (Point p : points) {
            average.add(getOverlapVector(pos, radius, p));
        }
        average.scale(1.0 / points.size());
        return average;
    }

    private Vector2d getOverlapVector(Vector2d pos, int radius, Point point) {
        Vector2d vector = getRayToNearest(pos, point);
        double overlap = radius - vector.length();
        vector.normalize();
        vector.scale(-overlap);
        return vector;
    }

    private Vector2d getRayToNearest(Vector2d pos, Point point) {
        double nx = clamp(point.x * tileSize, (point.x + 1) * tileSize, pos.x);
        double ny = clamp(point.y * tileSize, (point.y + 1) * tileSize, pos.y);
        return new Vector2d(nx - pos.x, ny - pos.y);
    }

    private static double clamp(int min, int max, double value) {
        return Math.max(min, Math.min(value, max));
    }
}
