package org.example.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/** Reusable dark-themed JTable that accepts List<String[]> data. */
public class ResultsTable extends JScrollPane {

    private final JTable table;
    private final DefaultTableModel model;

    public ResultsTable() {
        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable();
        setViewportView(table);
        getViewport().setBackground(Theme.BG);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        setBackground(Theme.BG);
    }

    private void styleTable() {
        table.setBackground(Theme.BG);
        table.setForeground(Theme.TEXT);
        table.setFont(Theme.BODY);
        table.setRowHeight(26);
        table.setGridColor(Theme.BORDER);
        table.setSelectionBackground(Theme.ACCENT.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.TABLE_HDR);
        header.setForeground(Theme.ACCENT);
        header.setFont(Theme.HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setReorderingAllowed(false);

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(sel ? Theme.ACCENT.darker()
                        : (row % 2 == 0 ? Theme.BG : Theme.TABLE_ALT));
                setForeground(sel ? Color.WHITE : Theme.TEXT);
                setFont(Theme.BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
    }

    /** Replace table contents. First row of data is treated as column headers. */
    public void load(List<String[]> data) {
        model.setRowCount(0);
        model.setColumnCount(0);
        if (data == null || data.size() < 1) return;

        for (String h : data.get(0)) model.addColumn(h);
        for (int i = 1; i < data.size(); i++) model.addRow(data.get(i));

        // Auto-size columns
        for (int c = 0; c < table.getColumnCount(); c++) {
            int width = 80;
            TableColumn col = table.getColumnModel().getColumn(c);
            // header
            Component hc = table.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(table, col.getHeaderValue(), false, false, -1, c);
            width = Math.max(width, hc.getPreferredSize().width + 16);
            // rows (sample up to 200)
            for (int r = 0; r < Math.min(200, table.getRowCount()); r++) {
                Component rc = table.getDefaultRenderer(Object.class)
                        .getTableCellRendererComponent(table, table.getValueAt(r, c), false, false, r, c);
                width = Math.max(width, rc.getPreferredSize().width + 16);
            }
            col.setPreferredWidth(Math.min(width, 260));
        }
    }

    public int rowCount() { return model.getRowCount(); }
}

