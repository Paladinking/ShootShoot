package ui;


import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyGridLayout implements LayoutManager2 {

    private int width, height;
    private final int[] columns, rows;

    private final Map<Component, GridConstraints> constraintsMap;

    public MyGridLayout(int width, int height){
        this.columns = new int[width];
        this.rows = new int[height];
        Arrays.fill(columns, 1);
        Arrays.fill(rows, 1);
        this.width = width;
        this.height = height;
        this.constraintsMap = new HashMap<>();
    }

    public MyGridLayout(int[] columns, int[] rows){
        for (int column : columns) this.width += column;
        for (int row : rows) this.height += row;
        this.columns = columns;
        this.rows = rows;
        this.constraintsMap = new HashMap<>();
    }

    public MyGridLayout(int[] columns, int height) {
        for (int column : columns) this.width += column;
        this.columns = columns;
        this.rows = new int[height];
        Arrays.fill(rows, 1);
        this.height = height;
        this.constraintsMap = new HashMap<>();
    }

    public MyGridLayout(int width, int[] rows) {
        for (int row : rows) this.height += row;
        this.rows = rows;
        this.columns = new int[width];
        Arrays.fill(columns, 1);
        this.width = width;
        this.constraintsMap = new HashMap<>();
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints instanceof GridConstraints) constraintsMap.put(comp, (GridConstraints) constraints);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        constraintsMap.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent){
        if (constraintsMap.keySet().size() == 0){
            return new Dimension(0, 0);
        }
        Component c = parent.getComponent(0);
        GridConstraints constraints = constraintsMap.get(c);
        return new Dimension(c.getPreferredSize().width * width / constraints.width, c.getPreferredSize().height * height / constraints.height);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        if (constraintsMap.keySet().size() == 0){
            return new Dimension(0, 0);
        }
        Component c = parent.getComponent(0);
        GridConstraints constraints = constraintsMap.get(c);
        return new Dimension(c.getMinimumSize().width * width / constraints.width, c.getMinimumSize().height * height / constraints.height);
    }

    @Override
    public void layoutContainer(Container parent) {
        Component[] components = parent.getComponents();
        Dimension size = parent.getSize();
        for (Component c: components){
            GridConstraints constraints = constraintsMap.get(c);
            c.setBounds(getXPos(constraints.x, size.width), getYPos(constraints.y, size.height), getWidth(constraints.x, constraints.width, size.width), getHeight(constraints.y, constraints.height, size.height));
        }
    }


    public int getXPos(int column, int containerWidth){
        double sum = 0;
        for (int i= 0; i < column; i++) sum += columns[i];
        return (int)((sum / width) * containerWidth);
    }

    public int getYPos(int row, int containerHeight){
        double sum = 0;
        for (int i= 0; i < row; i++) sum += rows[i];
        return (int)((sum / height) * containerHeight);
    }

    public int getWidth(int column, int w, int containerWidth){
        double sum = 0;
        for (int i = 0; i < w; i++) sum+= columns[i + column];
        return (int)((sum / width) * containerWidth);
    }

    public int getHeight(int row, int h, int containerHeight){
        double sum = 0;
        for (int i = 0; i < h; i++) sum+= rows[i + row];
        return (int)((sum / height) * containerHeight);
    }
}
