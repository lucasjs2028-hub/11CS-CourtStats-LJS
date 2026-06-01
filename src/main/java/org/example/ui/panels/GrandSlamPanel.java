package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Design Spec 8 — filter to Grand Slam matches only (optional player filter). */
public class GrandSlamPanel extends JPanel {

    private final JTextField        playerField = StyledComponents.field(22);
    private final JButton           searchBtn   = StyledComponents.button("Filter Grand Slams");
    private final ResultsTable      table       = new ResultsTable();
    private final JLabel            status      = StyledComponents.statusLabel();
    private final DisambiguationBar disambig    = new DisambiguationBar();

    public GrandSlamPanel() {
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
        p.add(StyledComponents.title("Grand Slams"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("Player (optional):"));
        p.add(playerField);
        p.add(searchBtn);
        p.add(StyledComponents.subLabel("Leave blank to see all Grand Slam matches."));
        return p;
    }

    private void run() {
        String player = playerField.getText().trim();
        status.setText("Searching…");
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<String>   resolved = player.isEmpty()
                        ? List.of() : QueryEngine.resolvePlayerNames(player);
                List<String[]> rows     = QueryEngine.grandSlamMatches(player.isEmpty() ? null : player);
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
                            ? "0 results — player has no Grand Slam matches."
                            : table.rowCount() + " Grand Slam matches"
                              + (player.isEmpty() ? "." : " for \"" + display + "\".");
                    status.setText(msg);
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
