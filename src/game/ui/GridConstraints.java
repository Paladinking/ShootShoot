package game.ui;

public class GridConstraints {

    public int x, y, width, height;

    public GridConstraints(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "GridConstraints{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }

    public GridConstraints(int x, int y){
        this(x, y, 1, 1);
    }
}
