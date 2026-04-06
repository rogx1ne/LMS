import com.formdev.flatlaf.FlatIntelliJLaf;
import com.library.ui.LoginFrame;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Treat ENTER like TAB for faster navigation between inputs.
        com.library.ui.EnterFocusTraversal.install();

        // 1. SET FLATLAF LOOK AND FEEL
        try {
            FlatIntelliJLaf.setup();
            
            // Global Modern UI Customization (applies to ALL components)
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("ScrollBar.width", 12);

        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
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
