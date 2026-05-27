package com.banque.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Gestion bancaire");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Clients", new ClientPanel());
        tabbedPane.add("Transactions", new TransactionPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }
}
