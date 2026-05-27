package com.banque;

import com.banque.gui.components.LoginDialog;
import com.banque.utils.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientBancaireSwing extends JFrame {
    private static final String API_URL = "http://localhost:5000";
    private static final ObjectMapper mapper = new ObjectMapper();

    public ClientBancaireSwing() {
        setTitle("Gestion bancaire - client bureautique");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab("1. Gestion des clients", createCrudClientPanel());
        tabbedPane.addTab("2. Versements et retraits", createTransactionsPanel());
        tabbedPane.addTab("3. Transferts", createTransfertPanel());
        tabbedPane.addTab("4. Recherche clients", createRecherchePanel());
        tabbedPane.addTab("5. Situation des clients", createEtatClientsPanel());
        tabbedPane.addTab("6. Mouvements", createMouvementsPanel());

        add(tabbedPane);
    }

    // --- Onglet 1 : CRUD Client ---
    private JPanel createCrudClientPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField txtCompte = new JTextField();
        JTextField txtNom = new JTextField();
        JTextField txtAdresse = new JTextField();
        
        form.add(new JLabel("Numero de compte :")); form.add(txtCompte);
        form.add(new JLabel("Nom complet :")); form.add(txtNom);
        form.add(new JLabel("Adresse :")); form.add(txtAdresse);
        
        JPanel buttons = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton("Ajouter");
        JButton btnUpdate = new JButton("Mettre a jour");
        JButton btnDelete = new JButton("Supprimer");
        buttons.add(btnAdd); buttons.add(btnUpdate); buttons.add(btnDelete);
        
        form.add(new JLabel("Actions :")); form.add(buttons);
        panel.add(form, BorderLayout.NORTH);

        btnAdd.addActionListener(e -> {
            btnAdd.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    Map<String, Object> data = new HashMap<>();
                    data.put("numeroCompte", txtCompte.getText());
                    data.put("nom", txtNom.getText());
                    data.put("adresse", txtAdresse.getText());
                    return HttpUtil.post(API_URL + "/client/ajout", mapper.writeValueAsString(data));
                }
                @Override protected void done() {
                    btnAdd.setEnabled(true);
                    try { get(); JOptionPane.showMessageDialog(panel, "Client enregistre avec succes."); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });

        btnUpdate.addActionListener(e -> {
            btnUpdate.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    Map<String, Object> data = new HashMap<>();
                    data.put("numeroCompte", txtCompte.getText());
                    data.put("nom", txtNom.getText());
                    data.put("adresse", txtAdresse.getText());
                    return HttpUtil.post(API_URL + "/client/modifier", mapper.writeValueAsString(data));
                }
                @Override protected void done() {
                    btnUpdate.setEnabled(true);
                    try { get(); JOptionPane.showMessageDialog(panel, "Client mis a jour avec succes."); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });

        btnDelete.addActionListener(e -> {
            btnDelete.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    return HttpUtil.post(API_URL + "/client/supprimer?compte=" + txtCompte.getText(), null);
                }
                @Override protected void done() {
                    btnDelete.setEnabled(true);
                    try { get(); JOptionPane.showMessageDialog(panel, "Client supprime."); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });
        
        return panel;
    }

    // --- Onglet 2 : Transactions ---
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Versement
        JPanel pVers = new JPanel(new GridLayout(3, 2, 5, 5));
        pVers.setBorder(BorderFactory.createTitledBorder("Versement"));
        JTextField txtVCompte = new JTextField();
        JTextField txtVMontant = new JTextField();
        JButton btnVerser = new JButton("Enregistrer un versement");
        pVers.add(new JLabel("Numero de compte :")); pVers.add(txtVCompte);
        pVers.add(new JLabel("Montant :")); pVers.add(txtVMontant);
        pVers.add(new JLabel("")); pVers.add(btnVerser);
        
        btnVerser.addActionListener(e -> {
            int montant;
            try {
                montant = Integer.parseInt(txtVMontant.getText().trim());
                if (montant <= 0) {
                    JOptionPane.showMessageDialog(panel, "Le montant doit etre superieur a zero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Montant invalide.");
                return;
            }
            btnVerser.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    Map<String, Object> data = new HashMap<>();
                    data.put("numCompte", txtVCompte.getText());
                    data.put("montant", montant);
                    return HttpUtil.post(API_URL + "/versement/ajouter", mapper.writeValueAsString(data));
                }
                @Override protected void done() {
                    btnVerser.setEnabled(true);
                    try { get(); JOptionPane.showMessageDialog(panel, "Versement enregistre."); }
                    catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });

        // Retrait
        JPanel pRet = new JPanel(new GridLayout(4, 2, 5, 5));
        pRet.setBorder(BorderFactory.createTitledBorder("Retrait"));
        JTextField txtRCompte = new JTextField();
        JTextField txtRCheque = new JTextField();
        JTextField txtRMontant = new JTextField();
        JButton btnRetirer = new JButton("Enregistrer un retrait");
        pRet.add(new JLabel("Numero de compte :")); pRet.add(txtRCompte);
        pRet.add(new JLabel("Numero de cheque :")); pRet.add(txtRCheque);
        pRet.add(new JLabel("Montant :")); pRet.add(txtRMontant);
        pRet.add(new JLabel("")); pRet.add(btnRetirer);
        
        btnRetirer.addActionListener(e -> {
            int montant;
            try {
                montant = Integer.parseInt(txtRMontant.getText().trim());
                if (montant <= 0) {
                    JOptionPane.showMessageDialog(panel, "Le montant doit etre superieur a zero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Montant invalide.");
                return;
            }
            btnRetirer.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    Map<String, Object> data = new HashMap<>();
                    data.put("numCompte", txtRCompte.getText());
                    data.put("numCheque", txtRCheque.getText());
                    data.put("montant", montant);
                    return HttpUtil.post(API_URL + "/retrait/ajouter", mapper.writeValueAsString(data));
                }
                @Override protected void done() {
                    btnRetirer.setEnabled(true);
                    try { get(); JOptionPane.showMessageDialog(panel, "Retrait enregistre."); }
                    catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });

        panel.add(pVers);
        panel.add(pRet);
        return panel;
    }

    // --- Onglet 3 : Transfert ---
    private JPanel createTransfertPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 300, 20));
        
        JTextField txtSource = new JTextField();
        JTextField txtDest = new JTextField();
        JTextField txtMontant = new JTextField();
        JButton btnTransferer = new JButton("Executer le transfert");

        panel.add(new JLabel("Compte source :")); panel.add(txtSource);
        panel.add(new JLabel("Compte destination :")); panel.add(txtDest);
        panel.add(new JLabel("Montant :")); panel.add(txtMontant);
        panel.add(new JLabel("")); panel.add(btnTransferer);

        btnTransferer.addActionListener(e -> {
            int montant;
            try {
                montant = Integer.parseInt(txtMontant.getText().trim());
                if (montant <= 0) {
                    JOptionPane.showMessageDialog(panel, "Le montant doit etre superieur a zero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Montant invalide.");
                return;
            }
            btnTransferer.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    Map<String, Object> data = new HashMap<>();
                    data.put("compteSource", txtSource.getText());
                    data.put("compteDestination", txtDest.getText());
                    data.put("montant", montant);
                    return HttpUtil.post(API_URL + "/transfert", mapper.writeValueAsString(data));
                }
                @Override protected void done() {
                    btnTransferer.setEnabled(true);
                    try { get(); JOptionPane.showMessageDialog(panel, "Transfert confirme."); }
                    catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });

        return panel;
    }

    // --- Onglet 4 : Recherche Client ---
    private JPanel createRecherchePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout());
        JTextField txtQuery = new JTextField(20);
        JButton btnChercher = new JButton("Rechercher");
        top.add(new JLabel("Mot cle (compte, nom, adresse) :")); top.add(txtQuery); top.add(btnChercher);
        
        DefaultTableModel model = new DefaultTableModel(new String[]{"Numero de compte", "Nom", "Adresse", "Solde"}, 0);
        JTable table = new JTable(model);
        
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnChercher.addActionListener(e -> {
            btnChercher.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    return HttpUtil.get(API_URL + "/client/recherche?q=" + txtQuery.getText());
                }
                @Override protected void done() {
                    btnChercher.setEnabled(true);
                    try {
                        String resp = get();
                        List<Map<String, Object>> clients = mapper.readValue(resp, new TypeReference<List<Map<String, Object>>>(){});
                        model.setRowCount(0);
                        for(Map<String, Object> c : clients) {
                            model.addRow(new Object[]{c.get("numeroCompte"), c.get("nom"), c.get("adresse"), c.get("solde")});
                        }
                    } catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });
        
        return panel;
    }

    // --- Onglet 5 : État des Clients ---
    private JPanel createEtatClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton btnLoad = new JButton("Actualiser la situation des clients");
        panel.add(btnLoad, BorderLayout.NORTH);
        
        DefaultTableModel model = new DefaultTableModel(new String[]{"Numero de compte", "Nom", "Solde"}, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnLoad.addActionListener(e -> {
            btnLoad.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    // L'appel réseau est effectué ici (Background Thread)
                    return HttpUtil.get(API_URL + "/etat/clients");
                }
                @Override
                protected void done() {
                    btnLoad.setEnabled(true);
                    try {
                        // Le retour du serveur est traité ici (Event Dispatch Thread -> Pas de gel de l'UI)
                        String resp = get();
                        List<Map<String, Object>> etats = mapper.readValue(resp, new TypeReference<List<Map<String, Object>>>(){});
                        
                        model.setRowCount(0);
                        for(Map<String, Object> c : etats) {
                            model.addRow(new Object[]{c.get("numCompte"), c.get("nom"), c.get("soldeCalcule")});
                        }
                    } catch(Exception ex) { 
                        JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); 
                    }
                }
            }.execute();
        });

        return panel;
    }

    // --- Onglet 6 : Mouvements ---
    private JPanel createMouvementsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout());
        JTextField txtCompte = new JTextField(15);
        JButton btnLoad = new JButton("Charger l'historique");
        top.add(new JLabel("Numero de compte :")); top.add(txtCompte); top.add(btnLoad);
        panel.add(top, BorderLayout.NORTH);
        
        DefaultTableModel model = new DefaultTableModel(new String[]{"Type", "Numero de cheque", "Montant", "Date"}, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        btnLoad.addActionListener(e -> {
            btnLoad.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override protected String doInBackground() throws Exception {
                    return HttpUtil.get(API_URL + "/mouvements?compte=" + txtCompte.getText());
                }
                @Override protected void done() {
                    btnLoad.setEnabled(true);
                    try {
                        String resp = get();
                        List<Map<String, Object>> mvt = mapper.readValue(resp, new TypeReference<List<Map<String, Object>>>(){});
                        model.setRowCount(0);
                        for(Map<String, Object> m : mvt) {
                            model.addRow(new Object[]{m.get("type"), m.get("numCheque"), m.get("montant"), m.get("date")});
                        }
                    } catch(Exception ex) { JOptionPane.showMessageDialog(panel, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE); }
                }
            }.execute();
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (!login.isAuthenticated()) {
                return;
            }
            new ClientBancaireSwing().setVisible(true);
        });
    }
}
