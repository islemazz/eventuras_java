package services;

import entities.Like;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceLike {
    private Connection cnx;

    public ServiceLike() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // Ajouter un like
    public void ajouterLike(int userId, int postId) throws SQLException {
        if (aDejaLike(userId, postId)) {
            System.out.println("L'utilisateur a déjà liké ce post.");
            return;
        }

        String sql = "INSERT INTO likes (user_id, post_id) VALUES (?, ?)";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, postId);
        pst.executeUpdate();
        System.out.println("Like ajouté avec succès !");
    }

    // Vérifier si un utilisateur a déjà liké un post
    public boolean aDejaLike(int userId, int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND post_id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, postId);
        ResultSet rs = pst.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    // Supprimer un like
    public void supprimerLike(int userId, int postId) throws SQLException {
        String sql = "DELETE FROM likes WHERE user_id = ? AND post_id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, userId);
        pst.setInt(2, postId);
        pst.executeUpdate();
        System.out.println("Like supprimé avec succès !");
    }

    // Obtenir le nombre de likes d'un post
    public int getNombreLikes(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, postId);
        ResultSet rs = pst.executeQuery();
        rs.next();
        return rs.getInt(1);
    }
}
