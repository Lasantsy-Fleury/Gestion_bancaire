package com.banque.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Client {
    @JsonProperty("numeroCompte")
    private String numeroCompte;
    
    @JsonProperty("nom")
    private String nom;
    
    @JsonProperty("adresse")
    private String adresse;
    
    @JsonProperty("solde")
    private int solde;

    public String getNumeroCompte() { return numeroCompte; }
    public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public int getSolde() { return solde; }
    public void setSolde(int solde) { this.solde = solde; }
}
