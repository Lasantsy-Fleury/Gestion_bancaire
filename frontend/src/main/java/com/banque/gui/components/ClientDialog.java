package com.banque.gui.components;

import com.banque.models.Client;
import com.banque.utils.UIConstants;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientDialog extends JDialog {
    private JTextField txtNumero;
    private JTextField txtNom;
    private JTextField txtAdresse;
    private JTextField txtSolde;
    private boolean confirmed = false;
    private Client client;

    public ClientDialog(Frame owner, Client clientToEdit) {
        super(owner, clientToEdit == null ? "Ajouter un client" : "Modifier un client", true);
        this.client = clientToEdit;
        
        setLayout(new BorderLayout());
        setSize(450, 400);
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(25, 25, 25, 25));
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        txtNumero = createModernField(client != null ? client.getNumeroCompte() : "", "C-0000X");
        txtNumero.setEnabled(client == null); 
        
        txtNom = createModernField(client != null ? client.getNom() : "", "Nom complet");
        txtAdresse = createModernField(client != null ? client.getAdresse() : "", "Adresse");
        txtSolde = createModernField(client != null ? String.valueOf(client.getSolde()) : "0", "Solde initial");
        txtSolde.setEnabled(client == null);

        addLabelAndField(content, "Numero de compte :", txtNumero, gbc, 0);
        addLabelAndField(content, "Nom complet :", txtNom, gbc, 1);
        addLabelAndField(content, "Adresse :", txtAdresse, gbc, 2);
        addLabelAndField(content, "Solde initial :", txtSolde, gbc, 3);

        add(content, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actions.setBackground(new Color(245, 245, 245));
        
        RoundedButton btnCancel = new RoundedButton("Annuler", UIConstants.COLOR_TEXT_SECONDARY, UIConstants.COLOR_TEXT_PRIMARY, 10);
        btnCancel.addActionListener(e -> dispose());
        
        RoundedButton btnSave = new RoundedButton("Enregistrer", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnSave.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        actions.add(btnCancel);
        actions.add(btnSave);
        add(actions, BorderLayout.SOUTH);
    }

    private JTextField createModernField(String text, String placeholder) {
        JTextField field = new JTextField(text);
        field.setPreferredSize(new Dimension(200, 35));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 0,5,0,5");
        return field;
    }

    private void addLabelAndField(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    public boolean isConfirmed() { return confirmed; }

    public Client getClientData() {
        if (client == null) client = new Client();
        client.setNumeroCompte(txtNumero.getText());
        client.setNom(txtNom.getText());
        client.setAdresse(txtAdresse.getText());
        try {
            client.setSolde(Integer.parseInt(txtSolde.getText()));
        } catch (NumberFormatException e) {
            client.setSolde(0);
        }
        return client;
    }
}
