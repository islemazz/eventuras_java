package services.Reclamation;


import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConversationService {

    private Connection cnx;

    public ConversationService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // Create conversation when admin accepts a reclamation
    public void createConversation(int reclamationId) throws SQLException {
        String query = "INSERT INTO reclamation_conversations (reclamation_id, created_at, status) VALUES (?, NOW(), 'Active')";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, reclamationId);
            pst.executeUpdate();
        }
    }

    // Change conversation status (lock/unlock/close)
    public void updateConversationStatus(int conversationId, String newStatus) throws SQLException {
        String query = "UPDATE reclamation_conversations SET status = ? WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, newStatus);
            pst.setInt(2, conversationId);
            pst.executeUpdate();
        }
    }

    // Fetch conversation status
    public String getConversationStatus(int conversationId) throws SQLException {
        String query = "SELECT status FROM reclamation_conversations WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, conversationId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        }
        return null;
    }

    public int getConversationIdForReclamation(int reclamationId) {
        int convoId = -1;
        String query = "SELECT id FROM reclamation_conversations WHERE reclamation_id = ? LIMIT 1";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, reclamationId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                convoId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return convoId;
    }



}