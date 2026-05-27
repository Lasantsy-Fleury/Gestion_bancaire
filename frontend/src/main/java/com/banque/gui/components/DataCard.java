package com.banque.gui.components;

import com.banque.utils.UIConstants;
import javax.swing.*;
import java.awt.*;

public class DataCard extends RoundedPanel {
    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel trendLabel;

    public DataCard(String title, String value, String trend, Color trendColor) {
        super(UIConstants.CORNER_RADIUS, UIConstants.COLOR_CARD_BG);
        setLayout(new BorderLayout());
        setHasShadow(true);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_SMALL);
        titleLabel.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(UIConstants.FONT_H2);
        valueLabel.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        trendLabel = new JLabel(trend);
        trendLabel.setFont(UIConstants.FONT_SMALL);
        trendLabel.setForeground(trendColor);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(valueLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.CENTER);
        add(trendLabel, BorderLayout.SOUTH);
        
        setPreferredSize(new Dimension(200, 120));
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setTrend(String trend, Color trendColor) {
        trendLabel.setText(trend);
        trendLabel.setForeground(trendColor);
    }
}
