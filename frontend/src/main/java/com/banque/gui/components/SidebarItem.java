package com.banque.gui.components;

import com.banque.utils.UIConstants;
import org.kordamp.ikonli.swing.FontIcon;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarItem extends JPanel {
    private JLabel label;
    private FontIcon fontIcon;
    private boolean active = false;
    private String name;

    public SidebarItem(String name, FontIcon icon, boolean active) {
        this.name = name;
        this.active = active;
        this.fontIcon = icon;
        
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 18));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        fontIcon.setIconColor(active ? Color.WHITE : UIConstants.COLOR_TEXT_LIGHT);
        fontIcon.setIconSize(20);

        label = new JLabel(name);
        label.setFont(UIConstants.FONT_BODY);
        label.setForeground(active ? Color.WHITE : UIConstants.COLOR_TEXT_LIGHT);
        
        add(new JLabel(fontIcon));
        add(label);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    label.setForeground(Color.WHITE);
                    fontIcon.setIconColor(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    label.setForeground(UIConstants.COLOR_TEXT_LIGHT);
                    fontIcon.setIconColor(UIConstants.COLOR_TEXT_LIGHT);
                }
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        label.setForeground(active ? Color.WHITE : UIConstants.COLOR_TEXT_LIGHT);
        fontIcon.setIconColor(active ? Color.WHITE : UIConstants.COLOR_TEXT_LIGHT);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (active) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UIConstants.COLOR_PRIMARY);
            g2.fillRoundRect(10, 5, getWidth() - 20, getHeight() - 10, 10, 10);
            g2.dispose();
        }
        super.paintComponent(g);
    }
    
    public String getItemName() {
        return name;
    }
}
