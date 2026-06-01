package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Design Spec 7 — tournament champions by year. */
public class TournamentWinnersPanel extends JPanel {

    private final JTextField yearField = StyledComponents.field(8);
    private final JButton    searchBtn = StyledComponents.button("Get Champions");
    private final ResultsTable table   = new ResultsTable();
    private final JLabel     status    = StyledComponents.statusLabel();

    public TournamentWinnersPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        add(buildTop(), BorderLayout.NORTH);
        add(table,      BorderLayout.CENTER);
        add(status,     BorderLayout.SOUTH);
        searchBtn.addActionListener(e -> run());
        yearField.addActionListener(e -> run());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Tournament Champions"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("Year (2000–2026):"));
        p.add(yearField);
        p.add(searchBtn);
        return p;
    }

    private void run() {
        String yr = yearField.getText().trim();
        if (yr.isEmpty()) { status.setText("Enter a year."); return; }
        try {
            int y = Integer.parseInt(yr);
            if (y > LocalDate.now().getYear()) { status.setText("Year out of range."); return; }
        } catch (NumberFormatException ex) { status.setText("Invalid year."); return; }

        status.setText("Searching…");
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() throws Exception {
                return QueryEngine.tournamentWinnersByYear(yr);
            }
            @Override protected void done() {
                try {
                    List<String[]> rows = get();
                    table.load(rows);
                    status.setText(table.rowCount() == 0
                            ? "No finals data found for " + yr + "."
                            : table.rowCount() + " tournament champions in " + yr + ".");
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
