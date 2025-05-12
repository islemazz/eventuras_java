package entities;

import java.util.Date;

public class Commande {
    private int id;
    private Produit produit;
    private String nom_client;
    private String adresse;
    private String telephone;
    private int quantite;
    private Double total;
    private Date date_commande;
    public Commande() {
        this.id = 0;
        this.produit = new Produit();
        this.nom_client = "";
        this.adresse = "";
        this.telephone = "";
        this.quantite = 0;
        this.total = 0.0;
        this.date_commande = new Date();
    }

    public Commande(Produit produit, String nom_client, String adresse, String telephone, int quantite,Double total ,Date date_commande) {
        this.produit = produit;
        this.nom_client = nom_client;
        this.adresse = adresse;
        this.telephone = telephone;
        this.quantite = quantite;
        this.total = total;
        this.date_commande = date_commande;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public String getNom_client() {
        return nom_client;
    }

    public void setNom_client(String nom_client) {
        this.nom_client = nom_client;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Date getDate_commande() {
        return date_commande;
    }

    public void setDate_commande(Date date_commande) {
        this.date_commande = date_commande;
    }


    @Override
    public String toString() {
        return "Commande{" +
                "adresse='" + adresse + '\'' +
                ", id=" + id +
                ", produit=" + produit +
                ", nom_client='" + nom_client + '\'' +
                ", telephone='" + telephone + '\'' +
                ", quantite=" + quantite +
                ", total=" + total +
                ", date_commande=" + date_commande +
                '}';
    }
}
