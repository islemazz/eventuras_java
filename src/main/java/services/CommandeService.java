package services;

import entities.Commande;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CommandeService {
    private Connection cnx;

    // Constructeur pour la connexion à la base de données
    public CommandeService() {
        cnx = MyConnection.getInstance().getConnection();
        if (cnx != null) {
            System.out.println("Connexion à la base de données réussie !");
        } else {
            System.err.println("Erreur de connexion à la base de données !");
        }
    }

    // Méthode pour ajouter une commande à la base de données
    public void ajouterCommande(Commande commande) {
        // Vérification que le produit associé à la commande existe dans la base de données
        if (commande.getProduit() == null || commande.getProduit().getId() == 0) {
            throw new IllegalArgumentException("Produit invalide. Assurez-vous que le produit a un ID valide.");
        }

        String sql = "INSERT INTO commande (produit_id, nom_client, adresse, telephone, quantite, total, date_commande) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            // Préparer la requête SQL
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, commande.getProduit().getId()); // Récupère l'ID du produit
            stmt.setString(2, commande.getNom_client());
            stmt.setString(3, commande.getAdresse());
            stmt.setString(4, commande.getTelephone());
            stmt.setInt(5, commande.getQuantite());
            stmt.setDouble(6, commande.getTotal());
            stmt.setDate(7, new java.sql.Date(commande.getDate_commande().getTime())); // Convertir en SQL Date

            // Exécuter la requête SQL
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Commande ajoutée avec succès.");
            } else {
                System.out.println("❌ Aucun enregistrement ajouté.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de la commande : " + e.getMessage());
        }
    }
}
