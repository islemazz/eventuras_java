package entities;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private Double prix;
    private int quantite;
    private String image;
    public Produit() {
        this.id = 0;
        this.nom = "";
        this.description = "";
        this.prix = 0.0;
        this.quantite = 0;
        this.image = "";
    }
    public Produit(int id, String nom, String description, Double prix, int quantite, String image) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantite = quantite;
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }


    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
    @Override
    public String toString() {
        return "Produit{" +
                "description='" + description + '\'' +
                ", id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", quantite=" + quantite +
                ", image='" + image + '\'' +
                '}';
    }

}
