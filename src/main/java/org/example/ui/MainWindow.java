package org.example.ui;

import org.example.db.DatabaseConnection;
import org.example.ui.panels.*;
import javax.swing.*;
import java.awt.*;

/** Main application window. */
public class   MainWindow extends JFrame {

    public MainWindow() {
        setTitle("CourtStats — ATP Tennis 2000–2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildTabs(),    BorderLayout.CENTER);
    }

    // ── Sidebar with app title ──────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(200, 0));
        side.setBackground(Theme.PANEL);
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(28, 18, 18, 18));

        JLabel logo = new JLabel("CourtStats");
        logo.setFont(new Font("SF Pro Display", Font.BOLD, 20));
        logo.setForeground(Theme.ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logo);

        JLabel sub = new JLabel("ATP 2000 – 2026");
        sub.setFont(Theme.SMALL);
        sub.setForeground(Theme.SUBTEXT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(sub);

        side.add(Box.createVerticalStrut(28));
        String[] labels = {
            "Player Search", "Biggest Upset", "Head-to-Head",
            "Win Rate", "Date Range", "Champions",
            "Grand Slams", "Round Reached", "Level Filter"
        };
        for (String l : labels) {
            JLabel lbl = new JLabel("• " + l);
            lbl.setFont(Theme.BODY);
            lbl.setForeground(Theme.SUBTEXT);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            side.add(lbl);
        }

        side.add(Box.createVerticalGlue());

        JLabel version = new JLabel("CS1104 Semester Project");
        version.setFont(Theme.SMALL);
        version.setForeground(Theme.BORDER);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(version);

        return side;
    }

    // ── Tabbed pane with one tab per design spec ────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(Theme.BG);
        tabs.setForeground(Theme.TEXT);
        tabs.setFont(Theme.BODY);

        // Match the tab header colours to the dark theme
        UIManager.put("TabbedPane.selected",            Theme.CARD);
        UIManager.put("TabbedPane.background",          Theme.PANEL);
        UIManager.put("TabbedPane.foreground",          Theme.TEXT);
        UIManager.put("TabbedPane.selectedForeground",  Theme.ACCENT);
        UIManager.put("TabbedPane.contentAreaColor",    Theme.BG);
        tabs.updateUI();

        tabs.addTab("1 · Player Search",    new PlayerSearchPanel());
        tabs.addTab("2 · Biggest Upset",    new BiggestUpsetPanel());
        tabs.addTab("3 · Head-to-Head",     new H2HPanel());
        tabs.addTab("4-5 · Win Rate",       new WinRatePanel());
        tabs.addTab("6 · Date Range",       new DateRangePanel());
        tabs.addTab("7 · Champions",        new TournamentWinnersPanel());
        tabs.addTab("8 · Grand Slams",      new GrandSlamPanel());
        tabs.addTab("9 · Round Reached",    new RoundReachedPanel());
        tabs.addTab("10 · Level Filter",    new TournamentLevelPanel());

        return tabs;
    }

    public void launch() {
        // Verify DB connection on startup
        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseConnection.get();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Could not connect to database:\n" + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            setVisible(true);
        });
    }
}
