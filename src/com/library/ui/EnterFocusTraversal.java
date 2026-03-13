package com.library.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Makes ENTER behave like TAB for faster data entry across the app.
 *
 * - ENTER: focus next component
 * - SHIFT+ENTER: focus previous component
 *
 * Notes:
 * - Skips buttons so ENTER still "clicks" when a button is focused.
 * - Skips editable JTextArea so ENTER can insert new lines if needed.
 */
public final class EnterFocusTraversal {
    private static volatile boolean installed = false;
    private static final String CLIENT_PROP_DISABLE_ENTER_TRAVERSAL = "lms.disableEnterTraversal";

    private EnterFocusTraversal() {}

    public static void install() {
        if (installed) return;
        installed = true;

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            if (e.getKeyCode() != KeyEvent.VK_ENTER) return false;
            if (e.isAltDown() || e.isControlDown() || e.isMetaDown()) return false;

            Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (focused == null) return false;

            if (focused instanceof JComponent) {
                Object disabled = ((JComponent) focused).getClientProperty(CLIENT_PROP_DISABLE_ENTER_TRAVERSAL);
                if (Boolean.TRUE.equals(disabled)) return false;
            }

            // Keep default behavior for buttons (ENTER activates/clicks).
            if (focused instanceof AbstractButton) return false;

            // Allow newline on editable text areas.
            if (focused instanceof JTextArea && ((JTextArea) focused).isEditable()) return false;

            // Most data-entry controls (or their editors) are text fields.
            if (focused instanceof JTextField || focused instanceof JComboBox) {
                if (e.isShiftDown()) focused.transferFocusBackward();
                else focused.transferFocus();
                e.consume();
                return true;
            }

            return false;
        });
    }
}
