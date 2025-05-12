package services;

import entities.Produit;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService {

    private Connection cnx;

    public ProduitService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // ✅ Créer un produit
    public void create(Produit produit) throws SQLException {
        String query = "INSERT INTO produits (nom, description, prix, quantite, image) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setDouble(3, produit.getPrix());
            ps.setInt(4, produit.getQuantite());
            ps.setString(5, produit.getImage());
            ps.executeUpdate();
        }
    }

    // ✅ Modifier un produit
    public void update(Produit produit) throws SQLException {
        String query = "UPDATE produits SET nom = ?, description = ?, prix = ?, quantite = ?, image = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setDouble(3, produit.getPrix());
            ps.setInt(4, produit.getQuantite());
            ps.setString(5, produit.getImage());
            ps.setInt(6, produit.getId());
            ps.executeUpdate();
        }
    }

    // ✅ Supprimer un produit
    public void delete(Produit produit) throws SQLException {
        String query = "DELETE FROM produits WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, produit.getId());
            ps.executeUpdate();
        }
    }

    // ✅ Lire tous les produits
    public List<Produit> readAll() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setDescription(rs.getString("description"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setQuantite(rs.getInt("quantite"));
                produit.setImage(rs.getString("image"));
                produits.add(produit);
            }
        }
        return produits;
    }

    public List<Produit> getAllProduits() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setDescription(rs.getString("description"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setQuantite(rs.getInt("quantite"));
                produit.setImage(rs.getString("image"));

                produits.add(produit);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }


    // ✅ Lire un produit par son ID
    public Produit getProduitById(int id) {
        Produit produit = null;
        String sql = "SELECT * FROM produits WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setDescription(rs.getString("description"));
                produit.setPrix(rs.getDouble("prix"));
                produit.setQuantite(rs.getInt("quantite"));
                produit.setImage(rs.getString("image"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produit;
    }
}
