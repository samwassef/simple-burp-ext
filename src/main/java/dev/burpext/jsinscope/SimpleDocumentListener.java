package dev.burpext.jsinscope;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Small adapter because all three DocumentListener callbacks perform the same operation. */
final class SimpleDocumentListener implements DocumentListener {
    // Callers supply one operation instead of repeating it across three listener methods.
    private final Runnable action;

    SimpleDocumentListener(Runnable action) {
        this.action = action;
    }

    // Swing reports insertions, removals, and styled changes separately; all invalidate the filter.
    @Override public void insertUpdate(DocumentEvent event) { action.run(); }
    @Override public void removeUpdate(DocumentEvent event) { action.run(); }
    @Override public void changedUpdate(DocumentEvent event) { action.run(); }
}
