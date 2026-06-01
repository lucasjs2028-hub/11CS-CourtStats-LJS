package org.example.ui.panels;

import org.example.db.QueryEngine;
import org.example.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Design Spec 9 — how many times a player reached a certain round. */
public class RoundReachedPanel extends JPanel {

    private final JTextField         playerField = StyledComponents.field(22);
    private final JComboBox<String>  roundCombo  = StyledComponents.combo(
            "1st Round", "2nd Round", "3rd Round", "4th Round",
            "Quarterfinals", "Semifinals", "The Final");
    private final JButton            searchBtn  = StyledComponents.button("Count");
    private final JLabel             bigCount   = new JLabel(" ");
    private final JLabel             status     = StyledComponents.statusLabel();
    private final DisambiguationBar  disambig   = new DisambiguationBar();

    public RoundReachedPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel topArea = new JPanel();
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.setBackground(Theme.BG);
        topArea.add(buildTop());
        topArea.add(disambig);

        add(topArea,       BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(status,        BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> run());
        playerField.addActionListener(e -> run());
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.BG);
        p.add(StyledComponents.title("Round Reached"));
        p.add(Box.createHorizontalStrut(20));
        p.add(StyledComponents.label("Player:"));
        p.add(playerField);
        p.add(StyledComponents.label("Round:"));
        p.add(roundCombo);
        p.add(searchBtn);
        return p;
    }

    private JPanel buildCenter() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG);
        bigCount.setFont(new Font(Theme.TITLE.getFamily(), Font.BOLD, 72));
        bigCount.setForeground(Theme.ACCENT);
        p.add(bigCount);
        return p;
    }

    private void run() {
        String player = playerField.getText().trim();
        String round  = (String) roundCombo.getSelectedItem();
        if (player.isEmpty()) { status.setText("Enter a player name."); return; }
        status.setText("Counting…");
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() throws Exception {
                List<String>   resolved = QueryEngine.resolvePlayerNames(player);
                List<String[]> rows     = QueryEngine.timesReachedRound(player, round);
                return new Object[]{resolved, rows};
            }
            @Override protected void done() {
                try {
                    Object[] result = get();
                    @SuppressWarnings("unchecked") List<String>   resolved = (List<String>)   result[0];
                    @SuppressWarnings("unchecked") List<String[]> rows     = (List<String[]>) result[1];

                    disambig.update(resolved, exact -> {
                        playerField.setText(exact);
                        run();
                    });

                    String count  = rows.size() > 1 ? rows.get(1)[0] : "0";
                    String display = resolved.size() == 1 ? resolved.get(0) : player;
                    bigCount.setText(count);
                    status.setText(display + " reached \"" + round + "\" -> " + count + " time(s).");
                } catch (Exception ex) { status.setText("Error: " + ex.getMessage()); }
            }
        }.execute();
    }
}
