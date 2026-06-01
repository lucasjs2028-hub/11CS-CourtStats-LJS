package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Design Spec 1 — search all matches for a player. */
public class PlayerSearchPanel extends JPanel {

    private final JTextField        nameField = StyledComponents.field(30);
    private final JButton           searchBtn = StyledComponents.button("Search");
    private final ResultsTable      table     = new ResultsTable();
    private final JLabel            status    = StyledComponents.statusLabel();
    private final DisambiguationBar disambig  = new DisambiguationBar();

    public PlayerSearchPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Stack title row + disambiguation bar vertically in NORTH
        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBackground(Theme.BG);
        topArea.add(buildTop());
        topArea.add(disambig);

        add(topArea, BorderLayout.NORTH);
        add(table,   BorderLayout.CENTER);
        add(status,  BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> runSearch());
        nameField.addActionListener(e -> runSearch());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Player Search"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("Player name:"));
        p.add(nameField);
        p.add(searchBtn);
        return p;
    }

    private void runSearch() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { status.setText("Enter a player name."); return; }
        status.setText("Searching…");
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<String>   resolved = QueryEngine.resolvePlayerNames(name);
                List<String[]> rows     = QueryEngine.searchByPlayer(name);
                return new Object[]{resolved, rows};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked") List<String>   resolved = (List<String>)   result[0];
                    @SuppressWarnings("unchecked") List<String[]> rows     = (List<String[]>) result[1];

                    // Show disambiguation chips if multiple players matched
                    disambig.update(resolved, exact -> {
                        nameField.setText(exact);
                        runSearch();
                    });

                    table.load(rows);
                    int count = table.rowCount();
                    if (count == 0) {
                        status.setText("0 results — player not found.");
                    } else {
                        String display = resolved.size() == 1 ? resolved.get(0) : name;
                        status.setText(count + " matches found for \"" + display + "\".");
                    }
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
