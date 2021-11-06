package game.textures;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class BulletTexture extends Texture {

    private static final int MIN_DRAW_POINTS = 6;

    private final static Color[] colors = new Color[]{Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW};

    private final static Stroke stroke = new BasicStroke(5);

    private final Queue<Point> pointsToDraw;

    private final Color color;

    public BulletTexture(int x, int y, int number) {
        this.pointsToDraw = new ArrayDeque<>();
        this.color = colors[number];
        for (int i = 0; i < MIN_DRAW_POINTS; i++) pointsToDraw.add(new Point(x, y));
    }

    public void draw(Graphics2D g) {
        if (pointsToDraw.isEmpty()) return;
        g.setColor(color);
        g.setStroke(stroke);
        Iterator<Point> it = pointsToDraw.iterator();
        Point p1, p2 = it.next();
        while (it.hasNext()) {
            p1 = p2;
            p2 = it.next();
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public void addPoint(Vector2d vector){
        pointsToDraw.add(new Point((int)vector.x, (int)vector.y));
    }

    @Override
    public boolean tick() {
        pointsToDraw.remove();
        return pointsToDraw.isEmpty();
    }

}
