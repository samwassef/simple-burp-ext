package dev.burpext.jsinscope;

import javax.swing.table.AbstractTableModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Table model with deterministic insertion order and URL-based de-duplication. */
final class JavaScriptTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"URL", "Method", "Status", "MIME type", "Source", "Last seen"};
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final Map<String, JavaScriptEntry> entriesByUrl = new LinkedHashMap<>();
    private List<JavaScriptEntry> rows = List.of();

    void addOrUpdate(JavaScriptEntry entry) {
        String key = JavaScriptUrl.identity(entry.url());
        Integer existingRow = indexOf(key);
        entriesByUrl.put(key, entry);
        rows = new ArrayList<>(entriesByUrl.values());

        if (existingRow == null) {
            fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        } else {
            fireTableRowsUpdated(existingRow, existingRow);
        }
    }

    void clear() {
        int previousSize = rows.size();
        entriesByUrl.clear();
        rows = List.of();
        if (previousSize > 0) {
            fireTableRowsDeleted(0, previousSize - 1);
        }
    }

    private Integer indexOf(String key) {
        int index = 0;
        for (String existingKey : entriesByUrl.keySet()) {
            if (existingKey.equals(key)) {
                return index;
            }
            index++;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        JavaScriptEntry entry = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> entry.url();
            case 1 -> entry.method();
            case 2 -> entry.statusCode();
            case 3 -> entry.mimeType();
            case 4 -> entry.source();
            case 5 -> TIME_FORMAT.format(entry.lastSeen());
            default -> throw new IndexOutOfBoundsException("Unknown column: " + columnIndex);
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 2 ? Integer.class : String.class;
    }
}
