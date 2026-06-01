package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Design Specs 4 & 5 — win % by surface and by court condition. */
public class WinRatePanel extends JPanel {

    private final JTextField         playerField  = StyledComponents.field(22);
    private final JComboBox<String>  surfaceCombo = StyledComponents.combo(
            "Hard", "Clay", "Grass", "Carpet");
    private final JComboBox<String>  courtCombo   = StyledComponents.combo(
            "Outdoor", "Indoor");
    private final JButton            surfaceBtn   = StyledComponents.button("Surface Win %");
    private final JButton            courtBtn     = StyledComponents.button("Court Win %");
    private final ResultsTable       table        = new ResultsTable();
    private final JLabel             status       = StyledComponents.statusLabel();
    private final JLabel             bigResult    = new JLabel(" ");
    private final JLabel             resolvedName = new JLabel(" ");
    private final DisambiguationBar  disambig     = new DisambiguationBar();

    public WinRatePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBackground(Theme.BG);
        topArea.add(buildTop());
        topArea.add(disambig);

        add(topArea, BorderLayout.NORTH);
        add(table,   BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 2));
        bottom.setBackground(Theme.BG);
        resolvedName.setFont(Theme.SMALL);
        resolvedName.setForeground(Theme.SUBTEXT);
        resolvedName.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        bigResult.setFont(new Font(Theme.BODY.getFamily(), Font.BOLD, 20));
        bigResult.setForeground(Theme.ACCENT);
        bottom.add(resolvedName, BorderLayout.NORTH);
        bottom.add(bigResult,    BorderLayout.CENTER);
        bottom.add(status,       BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        surfaceBtn.addActionListener(e -> runSurface());
        courtBtn.addActionListener(e -> runCourt());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Win Rate"));
        p.add(Box.createHorizontalStrut(10));
        p.add(StyledComponents.label("Player:"));
        p.add(playerField);
        p.add(StyledComponents.label("Surface:"));
        p.add(surfaceCombo);
        p.add(surfaceBtn);
        p.add(Box.createHorizontalStrut(10));
        p.add(StyledComponents.label("Court:"));
        p.add(courtCombo);
        p.add(courtBtn);
        return p;
    }

    private void runSurface() {
        String player  = playerField.getText().trim();
        String surface = (String) surfaceCombo.getSelectedItem();
        if (player.isEmpty()) { status.setText("Enter a player name."); return; }
        status.setText("Calculating…");
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<String>   resolved = QueryEngine.resolvePlayerNames(player);
                List<String[]> rows     = QueryEngine.winRateBySurface(player, surface);
                return new Object[]{resolved, rows};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked") List<String>   resolved = (List<String>)   result[0];
                    @SuppressWarnings("unchecked") List<String[]> rows     = (List<String[]>) result[1];

                    disambig.update(resolved, exact -> {
                        playerField.setText(exact);
                        runSurface();
                    });
                    showResolvedName(resolved, player);
                    table.load(rows);

                    String display = resolved.size() == 1 ? resolved.get(0) : player;
                    if (rows.size() > 1 && !"0".equals(rows.get(1)[2])) {
                        bigResult.setText(display + " on " + surface + ":  "
                                + rows.get(1)[0] + "  (" + rows.get(1)[1]
                                + " wins / " + rows.get(1)[2] + " matches)");
                        status.setText(" ");
                    } else {
                        bigResult.setText(" ");
                        status.setText("0 results — player has not played on " + surface + ".");
                    }
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }

    private void runCourt() {
        String player = playerField.getText().trim();
        String court  = (String) courtCombo.getSelectedItem();
        if (player.isEmpty()) { status.setText("Enter a player name."); return; }
        status.setText("Calculating…");
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<String>   resolved = QueryEngine.resolvePlayerNames(player);
                List<String[]> rows     = QueryEngine.winRateByCourt(player, court);
                return new Object[]{resolved, rows};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked") List<String>   resolved = (List<String>)   result[0];
                    @SuppressWarnings("unchecked") List<String[]> rows     = (List<String[]>) result[1];

                    disambig.update(resolved, exact -> {
                        playerField.setText(exact);
                        runCourt();
                    });
                    showResolvedName(resolved, player);
                    table.load(rows);

                    String display = resolved.size() == 1 ? resolved.get(0) : player;
                    if (rows.size() > 1 && !"0".equals(rows.get(1)[2])) {
                        bigResult.setText(display + " (" + court + "):  "
                                + rows.get(1)[0] + "  (" + rows.get(1)[1]
                                + " wins / " + rows.get(1)[2] + " matches)");
                        status.setText(" ");
                    } else {
                        bigResult.setText(" ");
                        status.setText("0 results — player has not played " + court + ".");
                    }
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }

    private void showResolvedName(List<String> names, String query) {
        if (names.isEmpty()) {
            resolvedName.setText("No player found matching \"" + query + "\"");
        } else if (names.size() == 1) {
            resolvedName.setText("Showing stats for: " + names.get(0));
        } else {
            resolvedName.setText("Showing combined stats for " + names.size() + " players — select one above");
        }
    }
}
