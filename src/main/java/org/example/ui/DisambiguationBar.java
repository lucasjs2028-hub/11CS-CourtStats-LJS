package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Shows up between the search bar and results whenever a name query
 * matches more than one player (e.g. "Djokovic N." and "Djokovic M.").
 *
 * Usage:
 *   bar.update(resolvedNames, exactName -> { field.setText(exactName); runSearch(); });
 *
 * Hidden automatically when 0 or 1 names are resolved.
 */
public class DisambiguationBar extends JPanel {

    private final JLabel prompt = new JLabel();

    public DisambiguationBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));
        setBackground(Theme.PANEL);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));

        prompt.setFont(Theme.SMALL);
        prompt.setForeground(Theme.SUBTEXT);
        setVisible(false);
    }

    /**
     * Rebuilds the bar for the given name list.
     * @param names     resolved names from QueryEngine.resolvePlayerNames()
     * @param onSelect  called with the exact DB name when user clicks a chip
     */
    public void update(List<String> names, Consumer<String> onSelect) {
        removeAll();

        if (names.size() <= 1) {
            setVisible(false);
            revalidate();
            repaint();
            return;
        }

        prompt.setText("Multiple players found — select one:");
        add(prompt);

        for (String name : names) {
            add(chip(name, onSelect));
        }

        setVisible(true);
        revalidate();
        repaint();
    }

    /** Hides the bar without needing a name list. */
    public void clear() {
        removeAll();
        setVisible(false);
        revalidate();
        repaint();
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private static JButton chip(String name, Consumer<String> onSelect) {
        JButton btn = new JButton(name);
        btn.setFont(Theme.SMALL);
        btn.setBackground(Theme.CARD);
        btn.setForeground(Theme.TEXT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(Theme.ACCENT);
                btn.setForeground(Color.BLACK);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(Theme.CARD);
                btn.setForeground(Theme.TEXT);
            }
        });
        btn.addActionListener(e -> onSelect.accept(name));
        return btn;
    }
}
