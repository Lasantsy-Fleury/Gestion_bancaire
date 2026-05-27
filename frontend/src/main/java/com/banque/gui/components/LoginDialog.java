package com.banque.gui.components;

import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private boolean authenticated = false;

    public LoginDialog(Frame owner) {
        super(owner, "Connexion", true);
        setUndecorated(true);
        setLayout(new BorderLayout());
        setSize(460, 320);
        setLocationRelativeTo(owner);

        JPanel background = new GradientBackgroundPanel();
        background.setLayout(new GridBagLayout());
        background.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel glassCard = createGlassCard();
        background.add(glassCard, new GridBagConstraints());

        add(background, BorderLayout.CENTER);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez renseigner tous les champs.");
            return;
        }

        try {
            ApiClient.login(username, password);
            authenticated = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Echec de connexion : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(260, 50));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 0,6,0,6; padding: 10,18,10,18; background: #FFFFFF; borderWidth: 1");
        return field;
    }

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(260, 50));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10; margin: 0,6,0,6; padding: 10,18,10,18; background: #FFFFFF; borderWidth: 1");
        return field;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        return lbl;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    private JPanel createGlassCard() {
        JPanel card = new GlassPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel title = new JLabel("Bienvenue");
        title.setFont(UIConstants.FONT_H1);
        title.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Connectez-vous pour continuer");
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        txtUsername = createField("Nom d'utilisateur");
        txtPassword = createPasswordField("Mot de passe");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.setOpaque(false);

        RoundedButton btnCancel = new RoundedButton("Annuler", UIConstants.COLOR_TEXT_SECONDARY, UIConstants.COLOR_TEXT_PRIMARY, 10);
        btnCancel.addActionListener(e -> dispose());

        RoundedButton btnLogin = new RoundedButton("Se connecter", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnLogin.addActionListener(e -> handleLogin());

        actions.add(btnCancel);
        actions.add(btnLogin);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(10));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(14));
        card.add(actions);

        return card;
    }

    private static class GradientBackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            Color c1 = new Color(236, 244, 255);
            Color c2 = new Color(248, 239, 255);
            GradientPaint paint = new GradientPaint(0, 0, c1, w, h, c2);
            g2.setPaint(paint);
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    private static class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 18;

            g2.setColor(new Color(255, 255, 255, 185));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(new Color(255, 255, 255, 220));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
