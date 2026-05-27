package com.banque.gui.components;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private int radius;
    private Color backgroundColor;
    private boolean hasShadow = false;

    public RoundedPanel(int radius, Color backgroundColor) {
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        setOpaque(false);
    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (hasShadow) {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, radius, radius);
        }

        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - (hasShadow ? 5 : 0), getHeight() - (hasShadow ? 5 : 0), radius, radius);
        g2.dispose();
    }
}
