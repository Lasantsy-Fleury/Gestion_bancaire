package com.banque.gui.modern;

import com.banque.models.Transaction;
import java.util.ArrayList;
import java.util.List;

public class ModernController {
    private ModernMainFrame view;

    public ModernController(ModernMainFrame view) {
        this.view = view;
    }

    public void start() {
        view.setVisible(true);
    }

    // This is where we would normally fetch data from the backend
    public List<Transaction> getRecentTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Achats", 124.50, "2026-05-01", "Valide"));
        transactions.add(new Transaction("Revenus", 4000.00, "2026-04-30", "Valide"));
        return transactions;
    }
}
