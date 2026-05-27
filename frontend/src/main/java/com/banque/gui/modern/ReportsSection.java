package com.banque.gui.modern;

import com.banque.gui.components.RoundedPanel;
import com.banque.gui.components.RoundedButton;
import com.banque.services.ApiClient;
import com.banque.utils.UIConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportsSection extends JPanel {
    private JTable globalTable;
    private DefaultTableModel globalModel;
    private JTable movementsTable;
    private DefaultTableModel movementsModel;
    private JComboBox<String> accountSelector;
    private JLabel accountValueLabel;
    private JLabel nameValueLabel;
    private Map<String, String> accountNames = new LinkedHashMap<>();

    public ReportsSection() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Rapports bancaires");
        title.setFont(UIConstants.FONT_H1);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BODY_BOLD);

        tabs.addTab("Etat des situations des clients", createGlobalSituationsPanel());
        tabs.addTab("Etat des mouvements bancaires", createMovementsReportPanel());
        
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createGlobalSituationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        RoundedButton btnRefresh = new RoundedButton("Actualiser les situations", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnRefresh.addActionListener(e -> loadGlobalData());

        RoundedButton btnExport = new RoundedButton("Exporter PDF", UIConstants.COLOR_SIDEBAR_BG, Color.BLACK, 10);
        btnExport.addActionListener(e -> exportGlobalReportPdf());
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.add(btnRefresh);
        top.add(btnExport);
        panel.add(top, BorderLayout.NORTH);

        RoundedPanel container = new RoundedPanel(UIConstants.CORNER_RADIUS, Color.WHITE);
        container.setHasShadow(true);
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"Numero de compte", "Titulaire", "Solde"};
        globalModel = new DefaultTableModel(columns, 0);
        globalTable = new JTable(globalModel);
        globalTable.setRowHeight(40);
        
        JScrollPane scroll = new JScrollPane(globalTable);
        scroll.setBorder(null);
        container.add(scroll, BorderLayout.CENTER);
        panel.add(container, BorderLayout.CENTER);

        loadGlobalData();
        return panel;
    }

    private JPanel createMovementsReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);

        accountSelector = new JComboBox<>();
        accountSelector.setPreferredSize(new Dimension(220, 36));
        accountSelector.putClientProperty("JComponent.sizeVariant", "regular");

        RoundedButton btnView = new RoundedButton("Voir", UIConstants.COLOR_PRIMARY, UIConstants.COLOR_PRIMARY_DARK, 10);
        btnView.addActionListener(e -> loadMovementsData());

        RoundedButton btnExport = new RoundedButton("Exporter PDF", UIConstants.COLOR_SIDEBAR_BG, Color.BLACK, 10);
        btnExport.addActionListener(e -> exportMovementsReportPdf());

        top.add(new JLabel("Compte :"));
        top.add(accountSelector);
        top.add(btnView);
        top.add(btnExport);
        panel.add(top, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 4));
        infoPanel.setOpaque(false);
        JLabel accountLabel = new JLabel("N.COMPTE :");
        accountLabel.setFont(UIConstants.FONT_BODY_BOLD);
        accountValueLabel = new JLabel("-");
        accountValueLabel.setFont(UIConstants.FONT_BODY);
        JLabel nameLabel = new JLabel("NOM :");
        nameLabel.setFont(UIConstants.FONT_BODY_BOLD);
        nameValueLabel = new JLabel("-");
        nameValueLabel.setFont(UIConstants.FONT_BODY);
        infoPanel.add(accountLabel);
        infoPanel.add(accountValueLabel);
        infoPanel.add(nameLabel);
        infoPanel.add(nameValueLabel);

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.setBorder(new EmptyBorder(0, 0, 5, 0));
        infoWrapper.add(infoPanel, BorderLayout.WEST);
        panel.add(infoWrapper, BorderLayout.CENTER);

        RoundedPanel container = new RoundedPanel(UIConstants.CORNER_RADIUS, Color.WHITE);
        container.setHasShadow(true);
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"N.CHEQUE", "VERSEMENT", "RETRAIT", "DATE"};
        movementsModel = new DefaultTableModel(columns, 0);
        movementsTable = new JTable(movementsModel);
        movementsTable.setRowHeight(40);

        JScrollPane scroll = new JScrollPane(movementsTable);
        scroll.setBorder(null);
        container.add(scroll, BorderLayout.CENTER);
        panel.add(container, BorderLayout.SOUTH);

        loadAccounts();
        return panel;
    }

    private void loadGlobalData() {
        try {
            String json = ApiClient.get("/etat/clients");
            List<Map<String, Object>> etats = ApiClient.getMapper().readValue(json, new TypeReference<List<Map<String, Object>>>(){});
            globalModel.setRowCount(0);
            for (Map<String, Object> e : etats) {
                globalModel.addRow(new Object[]{
                    e.get("numCompte"),
                    e.get("nom"),
                    e.get("soldeCalcule") + " Ar"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAccounts() {
        try {
            String json = ApiClient.get("/client/recherche?q=");
            List<Map<String, Object>> clients = ApiClient.getMapper().readValue(json, new TypeReference<List<Map<String, Object>>>(){});
            accountSelector.removeAllItems();
            accountNames.clear();
            for (Map<String, Object> client : clients) {
                String account = String.valueOf(client.get("numeroCompte"));
                String name = String.valueOf(client.get("nom"));
                accountSelector.addItem(account);
                accountNames.put(account, name);
            }
            accountSelector.addActionListener(e -> updateSelectedAccountLabels());
            updateSelectedAccountLabels();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Impossible de charger les comptes : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedAccountLabels() {
        Object selected = accountSelector.getSelectedItem();
        if (selected == null) {
            accountValueLabel.setText("-");
            nameValueLabel.setText("-");
            return;
        }
        String account = selected.toString();
        accountValueLabel.setText(account);
        String name = accountNames.getOrDefault(account, "-");
        nameValueLabel.setText(name);
    }

    private void loadMovementsData() {
        Object selected = accountSelector.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Veuillez selectionner un compte.");
            return;
        }
        try {
            updateSelectedAccountLabels();
            String json = ApiClient.get("/mouvements?compte=" + selected.toString());
            List<Map<String, Object>> movements = ApiClient.getMapper().readValue(json, new TypeReference<List<Map<String, Object>>>(){});
            movementsModel.setRowCount(0);
            for (Map<String, Object> movement : movements) {
                String type = String.valueOf(movement.get("type"));
                String typeLower = type == null ? "" : type.toLowerCase();
                Object amount = movement.get("montant");
                String versement = "";
                String retrait = "";
                if (typeLower.contains("verse")) {
                    versement = amount + " Ar";
                } else if (typeLower.contains("retrait")) {
                    retrait = amount + " Ar";
                }
                movementsModel.addRow(new Object[]{
                    movement.get("numCheque") == null ? "-" : movement.get("numCheque"),
                    versement,
                    retrait,
                    movement.get("date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportGlobalReportPdf() {
        if (globalModel == null || globalModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Aucune donnee a exporter.");
            return;
        }
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < globalModel.getRowCount(); i++) {
            rows.add(new String[]{
                String.valueOf(globalModel.getValueAt(i, 0)),
                String.valueOf(globalModel.getValueAt(i, 1)),
                String.valueOf(globalModel.getValueAt(i, 2))
            });
        }
        savePdfTable(
            "Etat des situations des clients",
            new ArrayList<>(),
            new String[]{"N.COMPTE", "NOM", "SOLDE"},
            rows,
            "etat_situations_clients.pdf"
        );
    }

    private void exportMovementsReportPdf() {
        if (movementsModel == null || movementsModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Aucune donnee a exporter.");
            return;
        }
        String account = accountValueLabel == null ? "-" : accountValueLabel.getText();
        String name = nameValueLabel == null ? "-" : nameValueLabel.getText();
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < movementsModel.getRowCount(); i++) {
            rows.add(new String[]{
                String.valueOf(movementsModel.getValueAt(i, 0)),
                String.valueOf(movementsModel.getValueAt(i, 1)),
                String.valueOf(movementsModel.getValueAt(i, 2)),
                String.valueOf(movementsModel.getValueAt(i, 3))
            });
        }
        List<String> meta = new ArrayList<>();
        meta.add("N.COMPTE : " + account);
        meta.add("NOM : " + name);
        savePdfTable(
            "Etat des mouvements bancaires",
            meta,
            new String[]{"N.CHEQUE", "VERSEMENT", "RETRAIT", "DATE"},
            rows,
            "etat_mouvements_bancaires.pdf"
        );
    }

    private void savePdfTable(String title, List<String> metaLines, String[] headers, List<String[]> rows, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultFileName));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".pdf")) {
            target = new File(target.getParentFile(), target.getName() + ".pdf");
        }

        try (PDDocument document = new PDDocument()) {
            PdfTableContext ctx = startNewPage(document, title, metaLines);
            float[] colWidths = computeColumnWidths(headers, rows, ctx.page, ctx.margin, ctx.fontSize);
            drawHeaderRow(ctx, headers, colWidths);
            for (String[] row : rows) {
                if (ctx.y - ctx.rowHeight < ctx.margin) {
                    ctx.close();
                    ctx = startNewPage(document, title, metaLines);
                    drawHeaderRow(ctx, headers, colWidths);
                }
                drawDataRow(ctx, row, colWidths);
            }
            ctx.close();
            document.save(target);
            JOptionPane.showMessageDialog(this, "Export PDF termine : " + target.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur export PDF : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private PdfTableContext startNewPage(PDDocument document, String title, List<String> metaLines) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);
        PdfTableContext ctx = new PdfTableContext(document, page, content);

        drawText(content, title, ctx.margin, ctx.y, PDType1Font.HELVETICA_BOLD, 14);
        ctx.y -= 22;
        for (String line : metaLines) {
            drawText(content, line, ctx.margin, ctx.y, PDType1Font.HELVETICA, 11);
            ctx.y -= 16;
        }
        ctx.y -= 6;
        return ctx;
    }

    private float[] computeColumnWidths(String[] headers, List<String[]> rows, PDPage page, float margin, float fontSize) {
        float[] widths = new float[headers.length];
        float charWidth = fontSize * 0.6f;
        for (int i = 0; i < headers.length; i++) {
            int maxLen = headers[i] == null ? 0 : headers[i].length();
            for (String[] row : rows) {
                if (row != null && i < row.length && row[i] != null) {
                    maxLen = Math.max(maxLen, row[i].length());
                }
            }
            widths[i] = maxLen * charWidth + 12;
        }

        float available = page.getMediaBox().getWidth() - (margin * 2);
        float total = 0f;
        for (float w : widths) {
            total += w;
        }
        if (total > available && total > 0) {
            float scale = available / total;
            for (int i = 0; i < widths.length; i++) {
                widths[i] = widths[i] * scale;
            }
        }
        return widths;
    }

    private void drawHeaderRow(PdfTableContext ctx, String[] headers, float[] colWidths) throws Exception {
        drawRowBorder(ctx, colWidths);
        float x = ctx.margin + 4;
        float textY = ctx.y - 12;
        for (int i = 0; i < headers.length; i++) {
            drawText(ctx.content, headers[i], x, textY, PDType1Font.HELVETICA_BOLD, ctx.fontSize);
            x += colWidths[i];
        }
        ctx.y -= ctx.rowHeight;
    }

    private void drawDataRow(PdfTableContext ctx, String[] row, float[] colWidths) throws Exception {
        drawRowBorder(ctx, colWidths);
        float x = ctx.margin + 4;
        float textY = ctx.y - 12;
        for (int i = 0; i < colWidths.length; i++) {
            String value = (row != null && i < row.length && row[i] != null) ? row[i] : "";
            drawText(ctx.content, value, x, textY, PDType1Font.HELVETICA, ctx.fontSize);
            x += colWidths[i];
        }
        ctx.y -= ctx.rowHeight;
    }

    private void drawRowBorder(PdfTableContext ctx, float[] colWidths) throws Exception {
        float x = ctx.margin;
        float top = ctx.y;
        float bottom = ctx.y - ctx.rowHeight;

        ctx.content.setLineWidth(0.5f);
        ctx.content.moveTo(x, top);
        ctx.content.lineTo(x + ctx.tableWidth(colWidths), top);
        ctx.content.moveTo(x, bottom);
        ctx.content.lineTo(x + ctx.tableWidth(colWidths), bottom);

        float cursor = x;
        for (float w : colWidths) {
            ctx.content.moveTo(cursor, top);
            ctx.content.lineTo(cursor, bottom);
            cursor += w;
        }
        ctx.content.moveTo(cursor, top);
        ctx.content.lineTo(cursor, bottom);
        ctx.content.stroke();
    }

    private void drawText(PDPageContentStream content, String text, float x, float y, PDType1Font font, float size) throws Exception {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(text == null ? "" : text);
        content.endText();
    }

    private static class PdfTableContext {
        private final PDDocument document;
        private final PDPage page;
        private final PDPageContentStream content;
        private float y;
        private final float margin = 40f;
        private final float rowHeight = 18f;
        private final float fontSize = 10f;

        private PdfTableContext(PDDocument document, PDPage page, PDPageContentStream content) {
            this.document = document;
            this.page = page;
            this.content = content;
            this.y = page.getMediaBox().getHeight() - margin;
        }

        private void close() throws Exception {
            content.close();
        }

        private float tableWidth(float[] colWidths) {
            float total = 0f;
            for (float w : colWidths) {
                total += w;
            }
            return total;
        }
    }
}
