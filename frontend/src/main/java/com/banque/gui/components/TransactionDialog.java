package com.banque.gui.components;

import com.banque.models.Client;
import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionDialog extends JDialog {
    private JComboBox<String> cmbCompte;
    private JTextField txtMontant;
    private JTextField txtCheque;
    private JLabel lblClientNom;
    private JLabel lblClientAdresse;
    private JLabel lblClientSolde;
    private final Map<String, Client> clientsByAccount = new LinkedHashMap<>();
    private boolean confirmed = false;
    private String type;

    public TransactionDialog(Frame owner, String type) {
        super(owner, type, true);
        this.type = type;
        
        setLayout(new BorderLayout());
        setSize(420, type.equals("Retrait") ? 420 : 370);
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(25, 25, 25, 25));
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        cmbCompte = createAccountCombo();
        txtMontant = createModernField("", "Montant en Ariary");
        txtCheque = createModernField("", "Numero de cheque");

        lblClientNom = createInfoLabel("-");
        lblClientAdresse = createInfoLabel("-");
        lblClientSolde = createInfoLabel("-");

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.3; content.add(createLabel("Numero de compte :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; content.add(cmbCompte, gbc);

        gbc.gridy = 1; gbc.gridx = 0; content.add(createLabel("Nom :"), gbc);
        gbc.gridx = 1; content.add(lblClientNom, gbc);

        gbc.gridy = 2; gbc.gridx = 0; content.add(createLabel("Adresse :"), gbc);
        gbc.gridx = 1; content.add(lblClientAdresse, gbc);

        gbc.gridy = 3; gbc.gridx = 0; content.add(createLabel("Solde :"), gbc);
        gbc.gridx = 1; content.add(lblClientSolde, gbc);

        gbc.gridy = 4; gbc.gridx = 0; content.add(createLabel("Montant :"), gbc);
        gbc.gridx = 1; content.add(txtMontant, gbc);

        if (type.equals("Retrait")) {
            gbc.gridy = 5; gbc.gridx = 0; content.add(createLabel("Numero de cheque :"), gbc);
            gbc.gridx = 1; content.add(txtCheque, gbc);
        }

        add(content, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actions.setBackground(new Color(245, 245, 245));
        
        RoundedButton btnCancel = new RoundedButton("Annuler", UIConstants.COLOR_TEXT_SECONDARY, UIConstants.COLOR_TEXT_PRIMARY, 10);
        btnCancel.addActionListener(e -> dispose());
        
        RoundedButton btnConfirm = new RoundedButton("Valider " + type, UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnConfirm.addActionListener(e -> {
            if (getSelectedAccount().isEmpty() || txtMontant.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Merci de renseigner tous les champs.");
                return;
            }
            try {
                int montant = Integer.parseInt(txtMontant.getText().trim());
                if (montant <= 0) {
                    JOptionPane.showMessageDialog(this, "Le montant doit etre superieur a zero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Montant invalide.");
                return;
            }
            confirmed = true;
            dispose();
        });

        actions.add(btnCancel);
        actions.add(btnConfirm);
        add(actions, BorderLayout.SOUTH);

        loadAccounts();
    }

    private JTextField createModernField(String text, String placeholder) {
        JTextField field = new JTextField(text);
        field.setPreferredSize(new Dimension(200, 35));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 0,6,0,6; padding: 6,14,6,14");
        return field;
    }

    private JComboBox<String> createAccountCombo() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setPreferredSize(new Dimension(200, 35));
        combo.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 0,6,0,6; padding: 6,14,6,14");
        combo.setEditable(false);
        combo.addActionListener(e -> updateClientInfo());
        return combo;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY);
        return label;
    }

    private void loadAccounts() {
        try {
            String json = ApiClient.get("/client/recherche?q=");
            List<Client> clients = ApiClient.getMapper().readValue(json, new TypeReference<List<Client>>(){});
            cmbCompte.removeAllItems();
            clientsByAccount.clear();
            for (Client client : clients) {
                clientsByAccount.put(client.getNumeroCompte(), client);
                cmbCompte.addItem(client.getNumeroCompte());
            }
            updateClientInfo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Impossible de charger les comptes : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateClientInfo() {
        String account = getSelectedAccount();
        Client client = clientsByAccount.get(account);
        if (client == null) {
            lblClientNom.setText("-");
            lblClientAdresse.setText("-");
            lblClientSolde.setText("-");
            return;
        }
        lblClientNom.setText(client.getNom());
        lblClientAdresse.setText(client.getAdresse());
        lblClientSolde.setText(client.getSolde() + " Ar");
    }

    private String getSelectedAccount() {
        Object selected = cmbCompte.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        return lbl;
    }

    public boolean isConfirmed() { return confirmed; }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("numCompte", getSelectedAccount());
        try {
            data.put("montant", Integer.parseInt(txtMontant.getText()));
        } catch (NumberFormatException e) {
            data.put("montant", 0);
        }
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (type.equals("Retrait")) {
            data.put("numCheque", txtCheque.getText());
        }
        return data;
    }
}
