package com.banque.gui;

import com.banque.models.Client;
import com.banque.services.ApiClient;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ClientPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public ClientPanel() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new FlowLayout());
        JTextField txtNumero = new JTextField(10);
        JTextField txtNom = new JTextField(15);
        JTextField txtSolde = new JTextField(10);
        JButton btnAdd = new JButton("Ajouter");
        JButton btnRefresh = new JButton("Actualiser");

        formPanel.add(new JLabel("Numero de compte :"));
        formPanel.add(txtNumero);
        formPanel.add(new JLabel("Nom complet :"));
        formPanel.add(txtNom);
        formPanel.add(new JLabel("Solde initial :"));
        formPanel.add(txtSolde);
        formPanel.add(btnAdd);
        formPanel.add(btnRefresh);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Numero de compte", "Nom", "Solde"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadClients());
        btnAdd.addActionListener(e -> {
            try {
                Map<String, Object> client = new HashMap<>();
                client.put("numeroCompte", txtNumero.getText());
                client.put("nom", txtNom.getText());
                client.put("adresse", "");
                client.put("solde", Integer.parseInt(txtSolde.getText().isEmpty() ? "0" : txtSolde.getText()));

                ApiClient.post("/client/ajout", client);
                JOptionPane.showMessageDialog(this, "Client enregistre.");
                loadClients();
                txtNumero.setText("");
                txtNom.setText("");
                txtSolde.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        loadClients();
    }

    private void loadClients() {
        try {
            String json = ApiClient.get("/client/recherche?q=");
            List<Client> clients = ApiClient.getMapper().readValue(json, new TypeReference<List<Client>>(){});
            tableModel.setRowCount(0);
            for (Client c : clients) {
                tableModel.addRow(new Object[]{c.getNumeroCompte(), c.getNom(), c.getSolde()});
            }
        } catch (Exception e) {
            System.err.println("Serveur indisponible ou erreur reseau : " + e.getMessage());
        }
    }
}
