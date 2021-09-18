package test;

import javax.swing.*;
import java.awt.*;

public class MyClass {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("hej");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(800, 800));
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
