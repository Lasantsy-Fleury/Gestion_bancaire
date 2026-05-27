package com.banque.gui.modern;

import com.banque.gui.components.*;
import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignV;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignL;
import org.kordamp.ikonli.swing.FontIcon;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ModernMainFrame extends JFrame {
    private JPanel sidebar;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private List<SidebarItem> sidebarItems;

    public ModernMainFrame() {
        setTitle("BankPro - Tableau de bord");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        sidebarItems = new ArrayList<>();
        initSidebar();
        initContentArea();

        add(sidebar, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }

    private void initSidebar() {
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, getHeight()));
        sidebar.setBackground(UIConstants.COLOR_SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo Section
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        logoPanel.setOpaque(false);
        JLabel logoLabel = new JLabel("BankPro");
        logoLabel.setFont(UIConstants.FONT_H1);
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        logoPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 70));
        sidebar.add(logoPanel);

        // Navigation Items
        addSidebarItem("Tableau de bord", FontIcon.of(MaterialDesignV.VIEW_DASHBOARD), true);
        addSidebarItem("Clients", FontIcon.of(MaterialDesignA.ACCOUNT_GROUP), false);
        addSidebarItem("Transactions", FontIcon.of(MaterialDesignS.SWAP_HORIZONTAL), false);
        addSidebarItem("Rapports", FontIcon.of(MaterialDesignC.CLIPBOARD_TEXT), false);
        addSidebarItem("Parametres", FontIcon.of(MaterialDesignC.COG), false);

        sidebar.add(Box.createVerticalGlue());

        // Logout Item
        addSidebarItem("Se deconnecter", FontIcon.of(MaterialDesignL.LOGOUT), false);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private void addSidebarItem(String name, FontIcon icon, boolean active) {
        SidebarItem item = new SidebarItem(name, icon, active);
        item.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 50));
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ("Se deconnecter".equalsIgnoreCase(name)) {
                    handleLogout();
                    return;
                }
                updateSidebarSelection(item);
                cardLayout.show(contentArea, name);
            }
        });
        sidebarItems.add(item);
        sidebar.add(item);
    }

    private void updateSidebarSelection(SidebarItem selectedItem) {
        for (SidebarItem item : sidebarItems) {
            item.setActive(item == selectedItem);
        }
    }

    private void initContentArea() {
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIConstants.COLOR_BACKGROUND);

        ClientsSection clientsSection = new ClientsSection();
        TransactionsSection transactionsSection = new TransactionsSection(clientsSection::refreshData);

        contentArea.add(new DashboardPanel(), "Tableau de bord");
        contentArea.add(transactionsSection, "Transactions");
        contentArea.add(clientsSection, "Clients");
        contentArea.add(new ReportsSection(), "Rapports");
        contentArea.add(createPlaceholderPanel("Parametres"), "Parametres");
    }

    private void handleLogout() {
        ApiClient.setAuthToken(null);
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);
        if (!loginDialog.isAuthenticated()) {
            dispose();
            return;
        }

        SidebarItem dashboardItem = null;
        for (SidebarItem item : sidebarItems) {
            if ("Tableau de bord".equalsIgnoreCase(item.getItemName())) {
                dashboardItem = item;
                break;
            }
        }
        if (dashboardItem != null) {
            updateSidebarSelection(dashboardItem);
        }
        cardLayout.show(contentArea, "Tableau de bord");
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.COLOR_BACKGROUND);
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(UIConstants.FONT_H1);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new ModernMainFrame().setVisible(true);
        });
    }
}
