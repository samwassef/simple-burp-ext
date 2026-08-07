package dev.burpext.jsinscope;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Small adapter because all three DocumentListener callbacks perform the same operation. */
final class SimpleDocumentListener implements DocumentListener {
    private final Runnable action;

    SimpleDocumentListener(Runnable action) {
        this.action = action;
    }

    @Override public void insertUpdate(DocumentEvent event) { action.run(); }
    @Override public void removeUpdate(DocumentEvent event) { action.run(); }
    @Override public void changedUpdate(DocumentEvent event) { action.run(); }
}
