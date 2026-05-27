package com.banque;

import com.banque.gui.components.LoginDialog;
import com.banque.gui.modern.ModernMainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            // Set FlatLaf for modern look
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (!login.isAuthenticated()) {
                return;
            }
            ModernMainFrame frame = new ModernMainFrame();
            frame.setVisible(true);
        });
    }
}
