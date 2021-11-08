package game.ui;

import json.FancyJSonParser;
import json.JsonObject;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class OptionsMenu extends JPanel {

    private static final String OPTIONS_FILE_NAME = "Options.json";

    private JsonObject optionsObject;

    private int players;

    public void loadOptions() {
        FancyJSonParser parser = new FancyJSonParser();
        try {
            optionsObject = parser.readFile(OPTIONS_FILE_NAME);
        } catch (IOException e) {
            System.out.println("Could not load " + OPTIONS_FILE_NAME);
            optionsObject = createOptions();
        }
    }

    public void initOptionsComponents(MainMenu mainMenu) {
        setLayout(new MyGridLayout(40, 40));

        add(new JLabel("Players:"), new GridConstraints(16, 8, 2, 1));
        ButtonGroup nrOfPlayers = new ButtonGroup();
        players = optionsObject.getInt("Players");
        JRadioButton[] jRadioButtons = new JRadioButton[4];
        for (int i = 1; i < 5; i++) {
            JRadioButton button = new JRadioButton("" + i);
            final int index = i;
            button.addActionListener((e) -> players = index);
            jRadioButtons[i - 1] = button;
            if (index == players) button.setSelected(true);
            nrOfPlayers.add(button);
            add(button, new GridConstraints(18 + i, 8, 1, 1));
        }

        add(new JLabel("Port:"), new GridConstraints(16, 10, 2, 1));
        JTextField defaultPort = new JTextField("" + optionsObject.getInt("Port"), 6);
        ((AbstractDocument) defaultPort.getDocument()).setDocumentFilter(new NumberFilter(65535));
        add(defaultPort, new GridConstraints(19, 10, 3, 1));

        add(new JLabel("Ip address:"), new GridConstraints(16, 12, 2, 1));
        JTextField defaultIp = new JTextField(optionsObject.getString("Ip"), 6);
        add(defaultIp, new GridConstraints(19, 12, 3, 1));

        add(new JLabel("Friendly Fire:"), new GridConstraints(16, 14, 2, 1));
        JCheckBox friendlyFire = new JCheckBox();
        friendlyFire.setSelected(optionsObject.getBoolean("FriendlyFire"));
        add(friendlyFire, new GridConstraints(19, 14, 1, 1));

        JButton done = new JButton("Done");
        done.addActionListener((e) -> {
            String ip = defaultIp.getText();
            optionsObject.put("Ip", ip);
            try {
                int port = Integer.parseInt(defaultIp.getText());
                optionsObject.put("Port", port);
            } catch (NumberFormatException ignored) {
            }
            optionsObject.put("Players", players);
            boolean hasFriendlyFire = friendlyFire.isSelected();
            optionsObject.put("FriendlyFire", hasFriendlyFire);
            saveFile();
            mainMenu.closeOptionsMenu();
        });
        add(done, new GridConstraints(15, 16 , 2 ,2 ));

        JButton reset = new JButton("Reset" );
        reset.addActionListener((e)-> {
            players = optionsObject.getInt("Players");
            jRadioButtons[players - 1].setSelected(true);
            defaultPort.setText("" + optionsObject.getInt("Port"));
            defaultIp.setText(optionsObject.getString("Ip"));
            friendlyFire.setSelected(optionsObject.getBoolean("FriendlyFire"));
        });
        add(reset, new GridConstraints(18, 16, 2, 2));

    }

    private JsonObject createOptions() {
        JsonObject options = new JsonObject();
        options.put("Players", 2);
        options.put("FriendlyFire", false);
        options.put("Port", 6066);
        options.put("Ip", "127.0.0.1");
        return options;
    }

    private void saveFile() {
        try {
            new FileOutputStream(OPTIONS_FILE_NAME).write(optionsObject.toJsonString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored){
            System.out.println("Could not save to " + OPTIONS_FILE_NAME);
        }
    }

    public int getPort() {
        return optionsObject.getInt("Port");
    }

    public int getPlayers() {
        return optionsObject.getInt("Players");
    }

    public String getIp() {
        return optionsObject.getString("Ip");
    }

    public boolean getFriendlyFire(){
        return optionsObject.getBoolean("FriendlyFire");
    }

    private static class NumberFilter extends DocumentFilter {

        private final int maxValue;

        private NumberFilter(int maxValue) {
            this.maxValue = maxValue;
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            text = text.replaceAll("[^0-9]", "");
            int documentLength = fb.getDocument().getLength();
            String oldText = fb.getDocument().getText(0, documentLength);
            String fullText = oldText.substring(0, offset) + text + oldText.substring(offset + length);
            if (!fullText.equals("") && Integer.parseInt(fullText) > maxValue) {
                super.replace(fb, 0, documentLength, "" + maxValue, attrs);
            } else {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

}
