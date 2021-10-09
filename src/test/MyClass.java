package test;

import java.awt.geom.Point2D;

public class MyClass {
    public static void main(String[] args) {
    }


    public static boolean intersects(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double tn = (x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4),
                un = (x1 - x3) * (y1 - y2) - (y1 - y3) * (x1 - x2),
                d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        return d < 0 ? tn <= 0.0 && tn >= d && un <= 0.0 && un >= d : tn >= 0.0 && tn <= d && un >= 0.0 && un <= d;
    }
}
