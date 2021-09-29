package game.textures;

import java.awt.*;

public class StartupCounter extends Texture {

    private static final String[] text = new String[]{"3", "2", "1", "GO!"};
    private static final Color[] colors = new Color[]{Color.BLACK, Color.BLACK, Color.red, Color.ORANGE};

    private final int x, y, startUpTicks, divider;

    private int ticks;

    private static final Font TEXT_FONT = new Font("Dialog", Font.BOLD, 50);

    public StartupCounter(int x, int y, int startUpTicks){
        this.x = x;
        this.y = y;
        this.startUpTicks = startUpTicks;
        this.divider = startUpTicks / text.length;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setFont(TEXT_FONT);
        int i = ticks / divider;
        g.setColor(colors[i]);
        String s  = text[i];
        int x = this.x - g.getFontMetrics().stringWidth(s) / 2;
        g.drawString(s, x, y);
    }

    @Override
    public boolean tick() {
        ticks++;
        return ticks == startUpTicks;
    }
}
