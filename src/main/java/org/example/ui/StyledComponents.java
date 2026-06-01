package org.example.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;

/** Factory methods for consistently styled dark-theme widgets. */
public class StyledComponents {

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.TITLE);
        l.setForeground(Theme.ACCENT);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT);
        return l;
    }

    public static JLabel subLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL);
        l.setForeground(Theme.SUBTEXT);
        return l;
    }

    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(Theme.CARD);
        f.setForeground(Theme.TEXT);
        f.setCaretColor(Theme.ACCENT);
        f.setFont(Theme.BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    public static JComboBox<String> combo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(Theme.CARD);
        cb.setForeground(Theme.TEXT);
        cb.setFont(Theme.BODY);
        cb.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object v,
                    int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, v, idx, sel, foc);
                setBackground(sel ? Theme.ACCENT.darker() : Theme.CARD);
                setForeground(Theme.TEXT);
                setFont(Theme.BODY);
                setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                return this;
            }
        });
        return cb;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(Theme.ACCENT);
        b.setForeground(Color.BLACK);
        b.setFont(new Font(Theme.BODY.getFamily(), Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(Theme.ACCENT.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(Theme.ACCENT); }
        });
        return b;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return p;
    }

    public static JLabel statusLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(Theme.SMALL);
        l.setForeground(Theme.SUBTEXT);
        return l;
    }
}
