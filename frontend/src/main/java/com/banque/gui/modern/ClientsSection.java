package com.banque.gui.modern;

import com.banque.gui.components.ClientDialog;
import com.banque.gui.components.RoundedPanel;
import com.banque.gui.components.RoundedButton;
import com.banque.models.Client;
import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.formdev.flatlaf.FlatClientProperties;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.swing.FontIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientsSection extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public ClientsSection() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        initHeader();
        initTable();
        loadData("");
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Gestion des clients");
        title.setFont(UIConstants.FONT_H1);
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Rechercher par nom, compte ou adresse...");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, FontIcon.of(MaterialDesignM.MAGNIFY, 18, UIConstants.COLOR_TEXT_SECONDARY));
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc: 12; padding: 0,10,0,10; margin: 0,5,0,5");
        
        searchField.addActionListener(e -> loadData(searchField.getText()));
        
        RoundedButton btnSearch = new RoundedButton("Rechercher", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnSearch.addActionListener(e -> loadData(searchField.getText()));

        RoundedButton btnRefresh = new RoundedButton("Actualiser", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnRefresh.addActionListener(e -> loadData(""));

        RoundedButton btnAdd = new RoundedButton("+ Nouveau client", UIConstants.COLOR_SUCCESS, new Color(34, 139, 34), 10);
        btnAdd.addActionListener(e -> showClientDialog(null));

        actions.add(searchField);
        actions.add(btnSearch);
        actions.add(btnRefresh);
        actions.add(btnAdd);

        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initTable() {
        RoundedPanel tableContainer = new RoundedPanel(UIConstants.CORNER_RADIUS, Color.WHITE);
        tableContainer.setHasShadow(true);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Numero de compte", "Nom", "Adresse", "Solde actuel"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // Add Edit/Delete buttons panel at bottom
        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerActions.setOpaque(false);
        
        RoundedButton btnEdit = new RoundedButton("Modifier la selection", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                editClient(id);
            }
        });

        RoundedButton btnDelete = new RoundedButton("Supprimer la selection", UIConstants.COLOR_DANGER, new Color(178, 34, 34), 10);
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                deleteClient(id);
            }
        });

        footerActions.add(btnEdit);
        footerActions.add(btnDelete);
        tableContainer.add(footerActions, BorderLayout.SOUTH);

        add(tableContainer, BorderLayout.CENTER);
    }

    private void loadData(String query) {
        try {
            String json = ApiClient.get("/client/recherche?q=" + query);
            List<Client> clients = ApiClient.getMapper().readValue(json, new TypeReference<List<Client>>(){});
            tableModel.setRowCount(0);
            for (Client c : clients) {
                tableModel.addRow(new Object[]{
                    c.getNumeroCompte(), 
                    c.getNom(), 
                    c.getAdresse(), 
                    c.getSolde() + " Ar"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showClientDialog(Client client) {
        ClientDialog dialog = new ClientDialog((Frame) SwingUtilities.getWindowAncestor(this), client);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            try {
                Client data = dialog.getClientData();
                if (client == null) {
                    ApiClient.post("/client/ajout", data);
                } else {
                    ApiClient.post("/client/modifier", data);
                }
                loadData("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
            }
        }
    }

    private void editClient(String id) {
        try {
            String json = ApiClient.get("/client/recherche?q=" + id);
            List<Client> results = ApiClient.getMapper().readValue(json, new TypeReference<List<Client>>(){});
            if (!results.isEmpty()) {
                showClientDialog(results.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteClient(String id) {
        int confirm = JOptionPane.showConfirmDialog(this, "Confirmer la suppression du client " + id + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ApiClient.post("/client/supprimer?compte=" + id, null);
                loadData("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
            }
        }
    }

    public void refreshData() {
        String query = searchField == null ? "" : searchField.getText().trim();
        loadData(query);
    }
}
