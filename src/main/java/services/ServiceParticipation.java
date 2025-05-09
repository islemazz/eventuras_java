package services;

import entities.Participation;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;

public class ServiceParticipation implements IService<Participation> {
    private Connection cnx;

    public ServiceParticipation() {
        cnx = MyConnection.getInstance().getConnection();
    }
    @Override
    public void ajouter(Participation participation) throws SQLException {
        String sql = "INSERT INTO participation (event_id, user_id, status, activities) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, participation.getEvent_id());
        pst.setInt(2, participation.getUser_id());
        pst.setString(3, participation.getStatus()); // Set the status as a String
        pst.setString(4, participation.getActivities()); // Set the activities
        pst.executeUpdate();
        System.out.println("Participation ajoutée avec succès!");
    }
    @Override
    public void update(Participation participation) throws SQLException {
        String sql = "UPDATE Participation SET status = ? where id_part = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setString(1, participation.getStatus());
        pst.setInt(2, participation.getId_part());
        pst.executeUpdate();
        System.out.println("STATUS CHANGE AVEC SUCCES!");

    }

    @Override
    public void delete(Participation participation) throws SQLException {
        String sql = "DELETE FROM participation WHERE id_part = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, participation.getId_part());
        pst.executeUpdate();
        System.out.println("Participation supprimee avec succes!");
    }

    @Override
    public ArrayList<Participation> afficherAll() throws SQLException {


        return null;
    }
    public int getParticipantCount(int eventId) throws SQLException {
        String sql = "SELECT COUNT(user_id) AS participant_count FROM participation WHERE event_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, eventId);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getInt("participant_count") : 0;
        }
    }
}
