package com.banque.gui.modern;

import com.banque.gui.components.DataCard;
import com.banque.gui.components.RoundedPanel;
import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private DataCard totalBalanceCard;
    private DataCard incomeCard;
    private DataCard expenseCard;
    private DataCard savingsRateCard;
    private DefaultTableModel transactionsModel;
    private BarChartPanel balancesChart;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        initHeader();
        initContent();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel(new GridLayout(2, 1));
        titleGroup.setOpaque(false);
        JLabel title = new JLabel("Bonjour, utilisateur");
        title.setFont(UIConstants.FONT_H1);
        JLabel subtitle = new JLabel("Vue d'ensemble des comptes aujourd'hui.");
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.COLOR_TEXT_SECONDARY);
        titleGroup.add(title);
        titleGroup.add(subtitle);

        header.add(titleGroup, BorderLayout.WEST);

        // Search and Profile placeholder
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);
        JTextField search = new JTextField("Rechercher des transactions...", 20);
        search.setPreferredSize(new Dimension(200, 35));
        actions.add(search);
        
        JLabel profile = new JLabel("👤 Administrateur");
        profile.setFont(UIConstants.FONT_BODY_BOLD);
        actions.add(profile);

        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initContent() {
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 0, 20, 0);
        gbc.weightx = 1.0;

        // Metric Cards Row
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        cardsPanel.setOpaque(false);
        totalBalanceCard = new DataCard("Solde total", "0 Ar", "", UIConstants.COLOR_TEXT_SECONDARY);
        incomeCard = new DataCard("Revenus mensuels", "0 Ar", "", UIConstants.COLOR_TEXT_SECONDARY);
        expenseCard = new DataCard("Depenses mensuelles", "0 Ar", "", UIConstants.COLOR_TEXT_SECONDARY);
        savingsRateCard = new DataCard("Taux d'epargne", "0 %", "", UIConstants.COLOR_TEXT_SECONDARY);
        cardsPanel.add(totalBalanceCard);
        cardsPanel.add(incomeCard);
        cardsPanel.add(expenseCard);
        cardsPanel.add(savingsRateCard);

        gbc.gridy = 0;
        mainContent.add(cardsPanel, gbc);

        // Tabs Section
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BODY_BOLD);
        tabs.setBackground(UIConstants.COLOR_BACKGROUND);

        RoundedPanel transactionsCard = new RoundedPanel(UIConstants.CORNER_RADIUS, UIConstants.COLOR_CARD_BG);
        transactionsCard.setHasShadow(true);
        transactionsCard.setLayout(new BorderLayout());
        transactionsCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel transTitle = new JLabel("Dernieres transactions");
        transTitle.setFont(UIConstants.FONT_H3);
        transactionsCard.add(transTitle, BorderLayout.NORTH);

        String[] columns = {"Date", "Compte", "Type", "Montant", "Statut"};
        transactionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(transactionsModel);
        table.setRowHeight(40);
        table.setFont(UIConstants.FONT_BODY);
        table.getTableHeader().setFont(UIConstants.FONT_BODY_BOLD);
        table.getTableHeader().setBackground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if ("Valide".equals(value)) {
                    label.setForeground(UIConstants.COLOR_SUCCESS);
                } else if ("En attente".equals(value)) {
                    label.setForeground(UIConstants.COLOR_PRIMARY);
                }
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        transactionsCard.add(scrollPane, BorderLayout.CENTER);

        RoundedPanel chartCard = new RoundedPanel(UIConstants.CORNER_RADIUS, UIConstants.COLOR_CARD_BG);
        chartCard.setHasShadow(true);
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel chartTitle = new JLabel("Solde par client");
        chartTitle.setFont(UIConstants.FONT_H3);
        chartCard.add(chartTitle, BorderLayout.NORTH);

        balancesChart = new BarChartPanel();
        chartCard.add(balancesChart, BorderLayout.CENTER);

        tabs.addTab("Transactions", transactionsCard);
        tabs.addTab("Histogramme", chartCard);

        gbc.gridy = 1;
        gbc.weighty = 1.0;
        mainContent.add(tabs, gbc);

        add(mainContent, BorderLayout.CENTER);

        loadDashboardData();
    }

    private void loadDashboardData() {
        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData data = new DashboardData();

                String etatJson = ApiClient.get("/etat/clients");
                List<Map<String, Object>> etats = ApiClient.getMapper().readValue(etatJson, new TypeReference<List<Map<String, Object>>>(){});

                for (Map<String, Object> etat : etats) {
                    String account = String.valueOf(etat.get("numCompte"));
                    String name = String.valueOf(etat.get("nom"));
                    long solde = toLong(etat.get("soldeCalcule"));
                    data.totalBalance += solde;
                    data.balanceBars.add(new BarEntry(name + " (" + account + ")", solde));

                    String mvtJson = ApiClient.get("/mouvements?compte=" + account);
                    List<Map<String, Object>> mouvements = ApiClient.getMapper().readValue(mvtJson, new TypeReference<List<Map<String, Object>>>(){});
                    for (Map<String, Object> mvt : mouvements) {
                        String type = String.valueOf(mvt.get("type"));
                        long montant = toLong(mvt.get("montant"));
                        String date = String.valueOf(mvt.get("date"));
                        data.movements.add(new Movement(account, type, montant, date));
                        if ("VERSEMENT".equals(type)) {
                            data.totalIncome += montant;
                        } else if ("RETRAIT".equals(type)) {
                            data.totalExpense += montant;
                        }
                    }
                }

                data.movements.sort(Comparator.comparing(DashboardPanel::parseDate).reversed());
                return data;
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    updateCards(data);
                    updateTransactions(data.movements);
                    balancesChart.setData(data.balanceBars);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardPanel.this, "Impossible de charger le dashboard : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateCards(DashboardData data) {
        totalBalanceCard.setValue(formatAmountAr(data.totalBalance));
        incomeCard.setValue(formatAmountAr(data.totalIncome));
        expenseCard.setValue(formatAmountAr(data.totalExpense));

        long net = data.totalIncome - data.totalExpense;
        long base = data.totalIncome == 0 ? 1 : data.totalIncome;
        long rate = Math.round((double) net * 100 / base);
        savingsRateCard.setValue(rate + " %");
    }

    private void updateTransactions(List<Movement> movements) {
        transactionsModel.setRowCount(0);
        int limit = Math.min(10, movements.size());
        for (int i = 0; i < limit; i++) {
            Movement movement = movements.get(i);
            String sign = "VERSEMENT".equals(movement.type) ? "+" : "-";
            transactionsModel.addRow(new Object[]{
                movement.date,
                movement.account,
                movement.type,
                sign + formatAmountAr(movement.amount),
                "Valide"
            });
        }
    }

    private static LocalDateTime parseDate(Movement movement) {
        try {
            return LocalDateTime.parse(movement.date, DATE_TIME_FORMAT);
        } catch (Exception ex) {
            return LocalDateTime.MIN;
        }
    }

    private static String formatAmountAr(long amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbols);
        format.setGroupingUsed(true);
        return format.format(amount) + " Ar";
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static final class DashboardData {
        private long totalBalance = 0;
        private long totalIncome = 0;
        private long totalExpense = 0;
        private final List<Movement> movements = new ArrayList<>();
        private final List<BarEntry> balanceBars = new ArrayList<>();
    }

    private static final class Movement {
        private final String account;
        private final String type;
        private final long amount;
        private final String date;

        private Movement(String account, String type, long amount, String date) {
            this.account = account;
            this.type = type;
            this.amount = amount;
            this.date = date;
        }
    }

    private static final class BarEntry {
        private final String label;
        private final long value;

        private BarEntry(String label, long value) {
            this.label = label;
            this.value = value;
        }
    }

    private static final class BarChartPanel extends JPanel {
        private final List<BarEntry> data = new ArrayList<>();

        private BarChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(300, 260));
        }

        private void setData(List<BarEntry> entries) {
            data.clear();
            data.addAll(entries);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) {
                drawEmptyState(g);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 10;
            int labelHeight = 30;
            int chartHeight = height - padding * 2 - labelHeight;
            int barCount = data.size();
            int maxBars = Math.min(barCount, 8);
            long maxValue = data.stream().mapToLong(entry -> entry.value).max().orElse(1);

            int barWidth = Math.max(20, (width - padding * 2) / maxBars - 10);
            int gap = 10;

            for (int i = 0; i < maxBars; i++) {
                BarEntry entry = data.get(i);
                int barHeight = (int) Math.round((entry.value / (double) maxValue) * chartHeight);
                int x = padding + i * (barWidth + gap);
                int y = padding + (chartHeight - barHeight);

                g2.setColor(UIConstants.COLOR_PRIMARY);
                g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

                g2.setColor(UIConstants.COLOR_TEXT_SECONDARY);
                g2.setFont(UIConstants.FONT_SMALL);
                String label = entry.label;
                if (label.length() > 10) {
                    label = label.substring(0, 10) + "...";
                }
                int labelY = padding + chartHeight + 18;
                g2.drawString(label, x, labelY);
            }

            g2.dispose();
        }

        private void drawEmptyState(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(UIConstants.COLOR_TEXT_SECONDARY);
            g2.setFont(UIConstants.FONT_BODY);
            String text = "Aucune donnee";
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = getHeight() / 2;
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }
}
