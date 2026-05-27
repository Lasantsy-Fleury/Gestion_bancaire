package com.banque.gui.modern;

import com.banque.gui.components.TransactionDialog;
import com.banque.gui.components.TransferDialog;
import com.banque.gui.components.RoundedPanel;
import com.banque.gui.components.RoundedButton;
import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.formdev.flatlaf.FlatClientProperties;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TransactionsSection extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField accountFilter;
    private CardLayout tableCardLayout;
    private JPanel tableCard;
    private JLabel emptyLabel;
    private final Runnable onDataChanged;

    public TransactionsSection() {
        this(null);
    }

    public TransactionsSection(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        initHeader();
        initTable();
        loadAllMovements();
    }

    private void initHeader() {
        // Use a wrapper panel with FlowLayout for responsiveness (wrapping)
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("Mouvements bancaires");
        title.setFont(UIConstants.FONT_H1);
        header.add(title);
        header.add(Box.createHorizontalGlue()); // Pushes actions to the right

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));

        accountFilter = new JTextField();
        accountFilter.setPreferredSize(new Dimension(220, 40));
        accountFilter.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Numero de compte...");
        
        // Add left margin for icon and placeholder
        accountFilter.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, FontIcon.of(MaterialDesignM.MAGNIFY, 18, UIConstants.COLOR_TEXT_SECONDARY));
        accountFilter.putClientProperty(FlatClientProperties.STYLE, "arc: 12; padding: 0,10,0,10; margin: 0,5,0,5");
        
        Dimension btnSize = new Dimension(110, 40); // Uniform size for all buttons

        RoundedButton btnLoad = createUniformButton("Afficher", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, btnSize);
        btnLoad.addActionListener(e -> loadMovements());

        RoundedButton btnRefresh = createUniformButton("Actualiser", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, btnSize);
        btnRefresh.addActionListener(e -> loadAllMovements());

        RoundedButton btnVersement = createUniformButton("Versement", UIConstants.COLOR_SUCCESS, new Color(34, 139, 10), btnSize);
        btnVersement.addActionListener(e -> showTxDialog("Versement"));

        RoundedButton btnRetrait = createUniformButton("Retrait", UIConstants.COLOR_DANGER, new Color(178, 34, 34), btnSize);
        btnRetrait.addActionListener(e -> showTxDialog("Retrait"));

        RoundedButton btnTransfert = createUniformButton("Transfert", UIConstants.COLOR_SIDEBAR_BG, Color.BLACK, btnSize);
        btnTransfert.addActionListener(e -> showTransferDialog());

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Numero de compte :"));
        filterRow.add(accountFilter);
        filterRow.add(btnLoad);
        filterRow.add(btnRefresh);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionRow.setOpaque(false);
        actionRow.add(btnVersement);
        actionRow.add(btnRetrait);
        actionRow.add(btnTransfert);

        actions.add(filterRow);
        actions.add(actionRow);

        header.add(actions);
        add(header, BorderLayout.NORTH);
    }

    private RoundedButton createUniformButton(String text, Color bg, Color hover, Dimension size) {
        RoundedButton btn = new RoundedButton(text, bg, hover, 10);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        return btn;
    }

    private void initTable() {
        RoundedPanel tableContainer = new RoundedPanel(UIConstants.CORNER_RADIUS, Color.WHITE);
        tableContainer.setHasShadow(true);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Date", "Type", "Numero de cheque", "Montant", "Statut"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_BODY_BOLD);
        
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String type = (String) table.getValueAt(row, 1);
                if (value != null) {
                    if ("VERSEMENT".equals(type)) label.setForeground(UIConstants.COLOR_SUCCESS);
                    else if ("RETRAIT".equals(type)) label.setForeground(UIConstants.COLOR_DANGER);
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        emptyLabel = new JLabel("Aucune donnee a afficher pour ce compte.", SwingConstants.CENTER);
        emptyLabel.setFont(UIConstants.FONT_BODY);
        emptyLabel.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        tableCardLayout = new CardLayout();
        tableCard = new JPanel(tableCardLayout);
        tableCard.setOpaque(false);
        tableCard.add(scrollPane, "table");
        tableCard.add(emptyLabel, "empty");

        tableContainer.add(tableCard, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);
    }

    private void loadMovements() {
        String account = accountFilter.getText();
        if (account.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Saisissez un numero de compte pour afficher les mouvements.");
            return;
        }
        loadMovementsForAccount(account);
    }

    private void loadMovementsForAccount(String account) {
        try {
            String json = ApiClient.get("/mouvements?compte=" + account);
            List<Map<String, Object>> movements = ApiClient.getMapper().readValue(json, new TypeReference<List<Map<String, Object>>>(){});
            tableModel.setRowCount(0);
            for (Map<String, Object> m : movements) {
                tableModel.addRow(new Object[]{
                    m.get("date"),
                    m.get("type"),
                    m.get("numCheque") == null ? "-" : m.get("numCheque"),
                    m.get("montant") + " Ar",
                    "Valide"
                });
            }
            if (tableModel.getRowCount() == 0) {
                tableCardLayout.show(tableCard, "empty");
            } else {
                tableCardLayout.show(tableCard, "table");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
        }
    }

    private void refreshTableAfterAction() {
        String account = accountFilter.getText().trim();
        if (account.isEmpty()) {
            loadAllMovements();
        } else {
            loadMovementsForAccount(account);
        }
        if (onDataChanged != null) {
            SwingUtilities.invokeLater(onDataChanged);
        }
    }

    private void loadAllMovements() {
        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                String clientsJson = ApiClient.get("/client/recherche?q=");
                List<Map<String, Object>> clients = ApiClient.getMapper().readValue(clientsJson, new TypeReference<List<Map<String, Object>>>(){});
                List<Map<String, Object>> allMovements = new ArrayList<>();
                for (Map<String, Object> client : clients) {
                    String account = String.valueOf(client.get("numeroCompte"));
                    String mvtJson = ApiClient.get("/mouvements?compte=" + account);
                    List<Map<String, Object>> movements = ApiClient.getMapper().readValue(mvtJson, new TypeReference<List<Map<String, Object>>>(){});
                    for (Map<String, Object> movement : movements) {
                        movement.put("compte", account);
                        allMovements.add(movement);
                    }
                }
                allMovements.sort(Comparator.comparing(TransactionsSection::parseDate).reversed());
                return allMovements;
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> movements = get();
                    tableModel.setRowCount(0);
                    for (Map<String, Object> m : movements) {
                        tableModel.addRow(new Object[]{
                            m.get("date"),
                            m.get("type"),
                            m.get("numCheque") == null ? "-" : m.get("numCheque"),
                            m.get("montant") + " Ar",
                            "Valide"
                        });
                    }
                    if (tableModel.getRowCount() == 0) {
                        tableCardLayout.show(tableCard, "empty");
                    } else {
                        tableCardLayout.show(tableCard, "table");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TransactionsSection.this, "Erreur : " + e.getMessage());
                }
            }
        }.execute();
    }

    private static LocalDateTime parseDate(Map<String, Object> movement) {
        try {
            return LocalDateTime.parse(String.valueOf(movement.get("date")), DATE_TIME_FORMAT);
        } catch (Exception ex) {
            return LocalDateTime.MIN;
        }
    }

    private void showTxDialog(String type) {
        TransactionDialog dialog = new TransactionDialog((Frame) SwingUtilities.getWindowAncestor(this), type);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                String endpoint = type.equals("Versement") ? "/versement/ajouter" : "/retrait/ajouter";
                ApiClient.post(endpoint, dialog.getData());
                JOptionPane.showMessageDialog(this, type + " enregistre.");
                refreshTableAfterAction();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
            }
        }
    }

    private void showTransferDialog() {
        TransferDialog dialog = new TransferDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                ApiClient.post("/transfert", dialog.getData());
                JOptionPane.showMessageDialog(this, "Transfert enregistre.");
                refreshTableAfterAction();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
            }
        }
    }
}
