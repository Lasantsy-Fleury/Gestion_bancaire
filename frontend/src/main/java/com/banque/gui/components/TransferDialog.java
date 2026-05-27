package com.banque.gui.components;

import com.banque.models.Client;
import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferDialog extends JDialog {
    private JComboBox<String> cmbSource;
    private JComboBox<String> cmbDest;
    private JTextField txtMontant;
    private boolean confirmed = false;

    public TransferDialog(Frame owner) {
        super(owner, "Transfert interne", true);
        
        setLayout(new BorderLayout());
        setSize(400, 350);
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(25, 25, 25, 25));
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        cmbSource = createAccountCombo();
        cmbDest = createAccountCombo();
        txtMontant = createModernField("", "Montant en Ariary");

        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.4; content.add(createLabel("Compte source :"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.6; content.add(cmbSource, gbc);

        gbc.gridy = 1; gbc.gridx = 0; content.add(createLabel("Compte destination :"), gbc);
        gbc.gridx = 1; content.add(cmbDest, gbc);

        gbc.gridy = 2; gbc.gridx = 0; content.add(createLabel("Montant :"), gbc);
        gbc.gridx = 1; content.add(txtMontant, gbc);

        add(content, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actions.setBackground(new Color(245, 245, 245));
        
        RoundedButton btnCancel = new RoundedButton("Annuler", UIConstants.COLOR_TEXT_SECONDARY, UIConstants.COLOR_TEXT_PRIMARY, 10);
        btnCancel.addActionListener(e -> dispose());
        
        RoundedButton btnConfirm = new RoundedButton("Executer le transfert", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnConfirm.addActionListener(e -> {
            if (getSelectedSource().isEmpty() || getSelectedDestination().isEmpty() || txtMontant.getText().isEmpty()) {
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
        return combo;
    }

    private void loadAccounts() {
        try {
            String json = ApiClient.get("/client/recherche?q=");
            List<Client> clients = ApiClient.getMapper().readValue(json, new TypeReference<List<Client>>(){});
            cmbSource.removeAllItems();
            cmbDest.removeAllItems();
            for (Client client : clients) {
                cmbSource.addItem(client.getNumeroCompte());
                cmbDest.addItem(client.getNumeroCompte());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Impossible de charger les comptes : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedSource() {
        Object selected = cmbSource.getSelectedItem();
        return selected == null ? "" : selected.toString().trim();
    }

    private String getSelectedDestination() {
        Object selected = cmbDest.getSelectedItem();
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
        data.put("compteSource", getSelectedSource());
        data.put("compteDestination", getSelectedDestination());
        try {
            data.put("montant", Integer.parseInt(txtMontant.getText()));
        } catch (NumberFormatException e) {
            data.put("montant", 0);
        }
        return data;
    }
}
