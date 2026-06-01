package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Design Spec 3 — Head-to-head between two players. */
public class H2HPanel extends JPanel {

    private final JTextField        p1Field   = StyledComponents.field(20);
    private final JTextField        p2Field   = StyledComponents.field(20);
    private final JButton           searchBtn = StyledComponents.button("Get H2H");
    private final ResultsTable      table     = new ResultsTable();
    private final JLabel            status    = StyledComponents.statusLabel();
    private final JLabel            scoreLbl  = new JLabel(" ");
    private final DisambiguationBar disambig1 = new DisambiguationBar(); // for player 1
    private final DisambiguationBar disambig2 = new DisambiguationBar(); // for player 2

    public H2HPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBackground(Theme.BG);
        topArea.add(buildTop());
        topArea.add(disambig1);
        topArea.add(disambig2);

        add(topArea, BorderLayout.NORTH);
        add(table,   BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Theme.BG);
        scoreLbl.setFont(new Font(Theme.BODY.getFamily(), Font.BOLD, 15));
        scoreLbl.setForeground(Theme.ACCENT);
        bottom.add(scoreLbl, BorderLayout.NORTH);
        bottom.add(status,   BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> run());
        p1Field.addActionListener(e -> run());
        p2Field.addActionListener(e -> run());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Head-to-Head"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("Player 1:"));
        p.add(p1Field);
        p.add(StyledComponents.label("Player 2:"));
        p.add(p2Field);
        p.add(searchBtn);
        return p;
    }

    private void run() {
        String n1 = p1Field.getText().trim();
        String n2 = p2Field.getText().trim();
        if (n1.isEmpty() || n2.isEmpty()) { status.setText("Enter both player names."); return; }
        if (n1.equalsIgnoreCase(n2))      { status.setText("Cannot compare a player against themselves."); return; }
        status.setText("Searching…");

        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<String>   names1 = QueryEngine.resolvePlayerNames(n1);
                List<String>   names2 = QueryEngine.resolvePlayerNames(n2);
                List<String[]> rows   = QueryEngine.headToHead(n1, n2);
                return new Object[]{names1, names2, rows};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked") List<String>   names1 = (List<String>)   result[0];
                    @SuppressWarnings("unchecked") List<String>   names2 = (List<String>)   result[1];
                    @SuppressWarnings("unchecked") List<String[]> rows   = (List<String[]>) result[2];

                    // Disambiguation for each player independently
                    disambig1.update(names1, exact -> { p1Field.setText(exact); run(); });
                    disambig2.update(names2, exact -> { p2Field.setText(exact); run(); });

                    table.load(rows);
                    int total = table.rowCount();
                    if (total == 0) {
                        scoreLbl.setText(" ");
                        status.setText("No H2H record found for these two players.");
                    } else {
                        // Count wins using exact resolved names when available
                        String label1 = names1.size() == 1 ? names1.get(0) : n1;
                        String label2 = names2.size() == 1 ? names2.get(0) : n2;
                        int p1wins = 0, p2wins = 0;
                        for (int i = 1; i < rows.size(); i++) {
                            String winner = rows.get(i)[7];
                            if (winner != null && winner.trim().equalsIgnoreCase(label1.trim())) p1wins++;
                            else p2wins++;
                        }
                        scoreLbl.setText(label1 + "  " + p1wins + " – " + p2wins + "  " + label2);
                        status.setText(total + " matches found.");
                    }
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
