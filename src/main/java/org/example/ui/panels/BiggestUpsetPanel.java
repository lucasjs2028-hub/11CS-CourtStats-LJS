package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Design Spec 2 — biggest upset in a tournament. */
public class BiggestUpsetPanel extends JPanel {

    private final JTextField tournField = StyledComponents.field(28);
    private final JButton    searchBtn  = StyledComponents.button("Find Upset");
    private final ResultsTable table    = new ResultsTable();
    private final JLabel     status     = StyledComponents.statusLabel();

    public BiggestUpsetPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        add(buildTop(), BorderLayout.NORTH);
        add(table,      BorderLayout.CENTER);
        add(status,     BorderLayout.SOUTH);
        searchBtn.addActionListener(e -> run());
        tournField.addActionListener(e -> run());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Biggest Upsets"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("Tournament (partial name):"));
        p.add(tournField);
        p.add(searchBtn);
        return p;
    }

    private void run() {
        String t = tournField.getText().trim();
        if (t.isEmpty()) { status.setText("Enter a tournament name."); return; }
        status.setText("Searching…");
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() throws Exception {
                return QueryEngine.biggestUpset(t);
            }
            @Override protected void done() {
                try {
                    List<String[]> rows = get();
                    table.load(rows);
                    status.setText(table.rowCount() == 0
                            ? "0 results — no ranking data or tournament not found."
                            : "Top " + table.rowCount() + " upsets shown (sorted by ranking gap).");
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
