package org.example;

import org.example.ui.MainWindow;
import org.example.ui.Theme;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Apply dark system look before any component is created
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background",           Theme.BG);
            UIManager.put("OptionPane.background",      Theme.PANEL);
            UIManager.put("OptionPane.messageForeground", Theme.TEXT);
            UIManager.put("Label.foreground",           Theme.TEXT);
            UIManager.put("ScrollPane.background",      Theme.BG);
            UIManager.put("Viewport.background",        Theme.BG);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new MainWindow().launch());
    }
}
