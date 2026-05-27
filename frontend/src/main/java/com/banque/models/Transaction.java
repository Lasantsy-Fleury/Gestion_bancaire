package com.banque.models;

import java.time.LocalDateTime;

public class Transaction {
    private String type;
    private double amount;
    private String date;
    private String status;

    public Transaction(String type, double amount, String date, String status) {
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
}
