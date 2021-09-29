package game.ui;

import game.tiles.Level;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class LevelSelector extends JPanel {

    private final static String path = "images/";

    private static final Font FONT = new Font("Dialog", Font.PLAIN, 52);
    private static final int GAP = 20;

    private final static String[] LEVEL_IMAGE_NAMES = new String[]{"stage.png", "stage2.png"};

    private final Level[] levels = new Level[LEVEL_IMAGE_NAMES.length];

    private final int width, height;

    private volatile int selected = -1;

    public LevelSelector(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void readLevelImages() throws IOException {
        for (int i = 0; i < LEVEL_IMAGE_NAMES.length; i++) {
            BufferedImage image = ImageIO.read(ClassLoader.getSystemResource(path + LEVEL_IMAGE_NAMES[i]));
            if (image.getWidth() != width || image.getHeight() != height) throw new IOException("Bad level size");
            levels[i] = new Level(image);
        }
        setLayout(new GridBagLayout());
        int columns = getPreferredSize().width / (2 * (width + GAP));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);
        for (int i = 0; i < levels.length; i++) {
            constraints.gridy = i / columns;
            LevelComponent levelComponent = new LevelComponent(levels[i]);
            levelComponent.setPreferredSize(new Dimension(width * 2, height * 2));
            add(levelComponent, constraints);
        }


    }

    private static class LevelComponent extends JComponent {

        private final BufferedImage image;

        public LevelComponent(Level level) {
            this.image = level.getImage();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public Level getLevel(int level) {
        return levels[level];
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(FONT);
        String s = "Choose a Level";
        int w = g.getFontMetrics().stringWidth(s);
        g.drawString(s, getWidth() / 2 - w / 2, getHeight() / 8);

    }


    public int selectLevel() {
        final Object lock = new Object();
        for (int i = 0; i < getComponentCount(); i++) {
            LevelComponent levelComponent = (LevelComponent) getComponent(i);
            final int index = i;
            EventQueue.invokeLater(() -> {
                levelComponent.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        synchronized (lock) {
                            selected = index;
                            lock.notify();
                        }
                    }
                });
            });
        }
        while (selected == -1) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {

                }
            }
        }
        return selected;
    }
}
