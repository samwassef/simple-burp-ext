package dev.burpext.jsinscope;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Swing view for the suite tab. All model mutations are marshalled onto Swing's EDT. */
final class JavaScriptPanel extends JPanel {
    // The model owns row state; the panel owns only controls, layout, and thread hand-off.
    private final JavaScriptTableModel model = new JavaScriptTableModel();

    JavaScriptPanel(Runnable refreshAction) {
        super(new BorderLayout(8, 8));

        JTable table = new JTable(model);
        // Results table: sortable columns and extra width for full resource URLs.
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(650);

        @SuppressWarnings("unchecked")
        // Filter control: quote user input so it behaves as plain text, not a regular expression.
        TableRowSorter<JavaScriptTableModel> sorter = (TableRowSorter<JavaScriptTableModel>) table.getRowSorter();
        JTextField filter = new JTextField(30);
        filter.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String text = filter.getText().trim();
            sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        }));

        JButton refresh = new JButton("Refresh from site map");
        // Actions: request a background site-map scan or clear only the local display state.
        refresh.addActionListener(event -> refreshAction.run());
        JButton clear = new JButton("Clear");
        clear.addActionListener(event -> model.clear());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING));
        toolbar.add(new JLabel("Filter:"));
        toolbar.add(filter);
        toolbar.add(refresh);
        toolbar.add(clear);

        // Layout: controls above a scrollable results table.
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    void addOrUpdate(JavaScriptEntry entry) {
        // HTTP callbacks run on Burp worker threads; Swing components must only be changed on EDT.
        SwingUtilities.invokeLater(() -> model.addOrUpdate(entry));
    }
}
