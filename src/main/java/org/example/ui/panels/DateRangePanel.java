package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Design Spec 6 — filter matches by date range. */
public class DateRangePanel extends JPanel {

    private final JTextField fromField = StyledComponents.field(12);
    private final JTextField toField   = StyledComponents.field(12);
    private final JButton    searchBtn = StyledComponents.button("Filter");
    private final ResultsTable table   = new ResultsTable();
    private final JLabel     status    = StyledComponents.statusLabel();

    public DateRangePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        add(buildTop(), BorderLayout.NORTH);
        add(table,      BorderLayout.CENTER);
        add(status,     BorderLayout.SOUTH);
        searchBtn.addActionListener(e -> run());
        fromField.addActionListener(e -> run());
        toField.addActionListener(e -> run());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Date Range Filter"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("From (YYYY-MM-DD):"));
        p.add(fromField);
        p.add(StyledComponents.label("To (YYYY-MM-DD):"));
        p.add(toField);
        p.add(searchBtn);
        return p;
    }

    private void run() {
        String from = fromField.getText().trim();
        String to   = toField.getText().trim();
        if (from.isEmpty() || to.isEmpty()) { status.setText("Enter both dates."); return; }

        // Validate dates
        try {
            LocalDate f = LocalDate.parse(from);
            LocalDate t = LocalDate.parse(to);
            if (t.isAfter(LocalDate.now())) { status.setText("'To' date is out of range (future)."); return; }
            if (f.isAfter(t))               { status.setText("'From' must be before 'To'."); return; }
        } catch (Exception ex) { status.setText("Invalid date format. Use YYYY-MM-DD."); return; }

        status.setText("Searching…");
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() throws Exception {
                return QueryEngine.filterByDateRange(from, to);
            }
            @Override protected void done() {
                try {
                    List<String[]> rows = get();
                    table.load(rows);
                    status.setText(table.rowCount() == 0
                            ? "0 results for this date range."
                            : table.rowCount() + " matches between " + from + " and " + to + ".");
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
