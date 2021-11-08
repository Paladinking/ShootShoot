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

    private BufferedImage image;

    public int getTileSize() {
        return tileSize;
    }

    public boolean isOpen(Vector2d nextPosition) {
        return isOpen(((int) nextPosition.x) / tileSize, ((int) nextPosition.y) / tileSize);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
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
        g.drawImage(image, 0, 0, tiles[0].length * tileSize, tiles.length * tileSize, null);
    }

    public void readFromImage(BufferedImage image) {
        final int wallColor = 0xff000000, emptyColor = 0xffffffff;
        for (int y = 0; y < tiles.length; y++) {
            for (int x = 0; x < tiles[0].length; x++) {
                int pixel = image.getRGB(x, y);
                switch (pixel) {
                    case wallColor -> tiles[y][x] = TileType.WALL;
                    case emptyColor -> tiles[y][x] = TileType.EMPTY;
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
            Vector2d overlap;
            if (isStraightWall(intersects)) overlap = getLargestOverlapVector(nextPosition, radius, intersects);
            else overlap = getAverageOverlapVector(nextPosition, radius, intersects);
            if (Double.isNaN(overlap.x)) return;// Should never happen but just in case...
            nextPosition.add(overlap);
        }
    }

    private boolean isStraightWall(List<Point> intersects) {
        if (intersects.size() == 2) {
            Point p1 = intersects.get(0);
            Point p2 = intersects.get(1);
            return (p1.x == p2.x && Math.abs(p1.y - p2.y) == 1) || (p1.y == p2.y && Math.abs(p1.x - p2.x) == 1);
        } else if (intersects.size() == 3) {
            Point p1 = intersects.get(0);
            Point p2 = intersects.get(1);
            Point p3 = intersects.get(2);
            return (p1.x == p2.x && p2.x == p3.x) || (p1.y == p2.y && p2.y == p3.y);
        }
        return false;
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

    private Vector2d getLargestOverlapVector(Vector2d pos, int radius, List<Point> points) {
        double largestOverlap = 0;
        Vector2d largest = new Vector2d(0, 0);
        for (Point p : points) {
            Vector2d vector = getRayToNearest(pos, p);
            double overlap = radius - vector.length();
            if (overlap > largestOverlap) {
                largestOverlap = overlap;
                largest = vector;
            }
        }
        largest.normalize();
        largest.scale(-largestOverlap);
        return largest;
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
