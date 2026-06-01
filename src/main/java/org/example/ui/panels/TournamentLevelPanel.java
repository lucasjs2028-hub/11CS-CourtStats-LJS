package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Design Spec 10 — filter by tournament level (ATP250, ATP500, Masters, Grand Slam). */
public class TournamentLevelPanel extends JPanel {

    private final JTextField        playerField = StyledComponents.field(22);
    private final JComboBox<String> levelCombo  = StyledComponents.combo(
            "ATP250", "ATP500", "Masters", "Masters 1000", "Grand Slam",
            "International", "International Gold");
    private final JButton           searchBtn   = StyledComponents.button("Filter");
    private final ResultsTable      table       = new ResultsTable();
    private final JLabel            status      = StyledComponents.statusLabel();
    private final DisambiguationBar disambig    = new DisambiguationBar();

    public TournamentLevelPanel() {
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
        add(status,  BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> run());
        playerField.addActionListener(e -> run());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Tournament Level"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("Player (optional):"));
        p.add(playerField);
        p.add(StyledComponents.label("Level:"));
        p.add(levelCombo);
        p.add(searchBtn);
        return p;
    }

    private void run() {
        String player = playerField.getText().trim();
        String level  = (String) levelCombo.getSelectedItem();
        status.setText("Searching…");
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<String>   resolved = player.isEmpty()
                        ? List.of() : QueryEngine.resolvePlayerNames(player);
                List<String[]> rows     = QueryEngine.filterByLevel(player.isEmpty() ? null : player, level);
                return new Object[]{resolved, rows};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked") List<String>   resolved = (List<String>)   result[0];
                    @SuppressWarnings("unchecked") List<String[]> rows     = (List<String[]>) result[1];

                    if (!player.isEmpty()) {
                        disambig.update(resolved, exact -> {
                            playerField.setText(exact);
                            run();
                        });
                    } else {
                        disambig.clear();
                    }

                    table.load(rows);
                    String display = (resolved.size() == 1) ? resolved.get(0) : player;
                    String msg = table.rowCount() == 0
                            ? "0 results — no matches at this level"
                              + (player.isEmpty() ? "." : " for \"" + display + "\".")
                            : table.rowCount() + " matches at " + level
                              + (player.isEmpty() ? "." : " for \"" + display + "\".");
                    status.setText(msg);
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
