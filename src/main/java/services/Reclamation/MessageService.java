package services.Reclamation;


import entities.ConversationMessage;
import entities.ConversationMessageAttachment;
import utils.MyConnection;
import utils.Session;
import gui.GestionUser.UserSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private Connection cnx;



    public MessageService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    // Create a new message
//    public void sendMessage(int conversationId, int senderId, String message) throws SQLException {
//        String query = "INSERT INTO ConversationMessage (conversation_id, sender_id, message, created_at) VALUES (?, ?, ?, NOW())";
//
//        try (PreparedStatement pst = cnx.prepareStatement(query)) {
//            pst.setInt(1, conversationId);
//            pst.setInt(2, senderId);
//            pst.setString(3, message);
//            pst.executeUpdate();
//        }
//    }
//
//    public void sendMessage(int conversationId, String message) throws SQLException {
//        String query = "INSERT INTO conversation_messages (conversation_id, sender_id, message, created_at) VALUES (?, 1, ?, NOW())";
//
//        try (PreparedStatement pst = cnx.prepareStatement(query)) {
//            pst.setInt(1, conversationId);
//            pst.setString(2, message);
//            pst.executeUpdate();
//        }
//    }


    public void sendMessage(ConversationMessage msg) throws SQLException {
        String query = "INSERT INTO conversation_messages (conversation_id, sender_id, message, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, msg.getConversationId());
            pst.setInt(2, msg.getSenderId()); // ✅ Now sender_id is passed as a parameter
            pst.setString(3, msg.getMessage());
            pst.setString(4, msg.getCreatedAt());

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                int messageId = rs.getInt(1);
                saveAttachments(messageId, msg.getAttachments()); // ✅ Save attachments
            }
        }
    }

    // ✅ Save attachments when a message is created
    public void saveAttachments(int messageId, List<ConversationMessageAttachment> attachments) throws SQLException {
        if (attachments == null || attachments.isEmpty()) return;

        String query = "INSERT INTO message_attachments (message_id, file_path, uploaded_at) VALUES (?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            for (ConversationMessageAttachment attachment : attachments) {
                pst.setInt(1, messageId);
                pst.setString(2, attachment.getFilePath());
                pst.setString(3, attachment.getUploaded_at());

                pst.executeUpdate();
            }
        }
    }


    // Fetch all messages in a conversation
    public List<ConversationMessage> getMessages(int conversationId) throws SQLException {
        List<ConversationMessage> messages = new ArrayList<>();
        String query = "SELECT id, conversation_id, sender_id, message, created_at FROM conversation_messages WHERE conversation_id = ? ORDER BY created_at ASC";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, conversationId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int messageId = rs.getInt("id");

                // ✅ Fetch attachments for this message
                List<ConversationMessageAttachment> attachments = getAttachmentsByMessageId(messageId);

                ConversationMessage message = new ConversationMessage(
                        messageId,
                        rs.getInt("conversation_id"),
                        rs.getInt("sender_id"),
                        rs.getString("message"),
                        rs.getString("created_at"),
                        attachments // ✅ Now passing attachments
                );
                messages.add(message);
            }
        }
        return messages;
    }

    // ✅ Retrieve all attachments for a specific message
    public List<ConversationMessageAttachment> getAttachmentsByMessageId(int messageId) throws SQLException {
        List<ConversationMessageAttachment> attachments = new ArrayList<>();
        String query = "SELECT id, file_path, uploaded_at FROM message_attachments WHERE message_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, messageId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String filePath = rs.getString("file_path");
                String uploaded_at = rs.getString("file_path");

                attachments.add(new ConversationMessageAttachment(id, messageId, filePath, uploaded_at));
            }
        }
        return attachments;
    }


    // Delete a message by ID
    public void deleteMessage(int messageId) throws SQLException {
        String query = "DELETE FROM conversation_messages WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, messageId);
            pst.executeUpdate();
        }
    }

    // Update a message (useful if you want an edit feature)
    public void updateMessage(int messageId, String newMessage) throws SQLException {
        String query = "UPDATE conversation_messages SET message = ? WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, newMessage);
            pst.setInt(2, messageId);
            pst.executeUpdate();
        }
    }









    // ✅ Delete a specific attachment
    public void deleteAttachment(int attachmentId) throws SQLException {
        String query = "DELETE FROM message_attachments WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, attachmentId);
            pst.executeUpdate();
        }
    }

    // ✅ Delete all attachments for a message
    public void deleteAttachmentsByMessageId(int messageId) throws SQLException {
        String query = "DELETE FROM message_attachments WHERE message_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, messageId);
            pst.executeUpdate();
        }
    }




}
