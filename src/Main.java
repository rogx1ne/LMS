import com.library.ui.LoginFrame; // Or DashboardFrame if you skip login
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // 1. FORCE THE THEME (This fixes the black boxes)
        try {
            // Option A: Nimbus (Modern, Clean, respects your colors)
            UIManager.setLookAndFeel(new NimbusLookAndFeel());

            // Fix specific Nimbus colors to ensure high contrast
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.BLACK);
            UIManager.put("TextArea.background", Color.WHITE);
            UIManager.put("TextArea.foreground", Color.BLACK);
            UIManager.put("ComboBox.background", Color.WHITE);
            UIManager.put("ComboBox.foreground", Color.BLACK);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. LAUNCH THE APP
        SwingUtilities.invokeLater(() -> {
            // If you have a LoginFrame, start that.
            // If you are testing Dashboard directly, use DashboardFrame.
             new com.library.ui.LoginFrame().setVisible(true);
            //new com.library.ui.DashboardFrame().setVisible(true);
        });
    }
}
