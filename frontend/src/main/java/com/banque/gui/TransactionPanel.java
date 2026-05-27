package com.banque.gui;

import com.banque.services.ApiClient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionPanel extends JPanel {
    public TransactionPanel() {
        setLayout(new GridLayout(2, 1, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Versement Panel
        JPanel versPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        versPanel.setBorder(BorderFactory.createTitledBorder("Enregistrer un versement"));
        
        JTextField txtVersCompte = new JTextField();
        JTextField txtVersMontant = new JTextField();
        JButton btnVerser = new JButton("Enregistrer");

        versPanel.add(new JLabel("Numero de compte :"));
        versPanel.add(txtVersCompte);
        versPanel.add(new JLabel("Montant :"));
        versPanel.add(txtVersMontant);
        versPanel.add(new JLabel(""));
        versPanel.add(btnVerser);

        // Retrait Panel
        JPanel retPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        retPanel.setBorder(BorderFactory.createTitledBorder("Enregistrer un retrait"));
        
        JTextField txtRetCompte = new JTextField();
        JTextField txtRetCheque = new JTextField();
        JTextField txtRetMontant = new JTextField();
        JButton btnRetrait = new JButton("Enregistrer");

        retPanel.add(new JLabel("Numero de compte :"));
        retPanel.add(txtRetCompte);
        retPanel.add(new JLabel("Numero de cheque (optionnel) :"));
        retPanel.add(txtRetCheque);
        retPanel.add(new JLabel("Montant :"));
        retPanel.add(txtRetMontant);
        retPanel.add(new JLabel(""));
        retPanel.add(btnRetrait);

        add(versPanel);
        add(retPanel);

        // Actions
        btnVerser.addActionListener(e -> {
            try {
                int montant = Integer.parseInt(txtVersMontant.getText().trim());
                if (montant <= 0) {
                    JOptionPane.showMessageDialog(this, "Le montant doit etre superieur a zero.");
                    return;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("numCompte", txtVersCompte.getText());
                data.put("montant", montant);
                data.put("date", getCurrentDate());

                ApiClient.post("/versement/ajouter", data);
                JOptionPane.showMessageDialog(this, "Versement enregistre.");
                txtVersCompte.setText("");
                txtVersMontant.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Impossible d'enregistrer le versement : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRetrait.addActionListener(e -> {
            try {
                int montant = Integer.parseInt(txtRetMontant.getText().trim());
                if (montant <= 0) {
                    JOptionPane.showMessageDialog(this, "Le montant doit etre superieur a zero.");
                    return;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("numCompte", txtRetCompte.getText());
                data.put("numCheque", txtRetCheque.getText());
                data.put("montant", montant);
                data.put("date", getCurrentDate());

                ApiClient.post("/retrait/ajouter", data);
                JOptionPane.showMessageDialog(this, "Retrait enregistre.");
                txtRetCompte.setText("");
                txtRetCheque.setText("");
                txtRetMontant.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Impossible d'enregistrer le retrait : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
