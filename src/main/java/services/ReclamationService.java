package services;

import entities.Reclamation;
import entities.ReclamationAttachment;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements IService4<Reclamation> {

    private Connection cnx;

    public ReclamationService(){
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void create(Reclamation reclam) throws SQLException {
        String query = "INSERT INTO reclamations (id_user, id_event, created_at, subject, description,status) VALUES (?, ?, ?, ?, ?,?)";

        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reclam.getId_user());
            pst.setInt(2, reclam.getId_event());
            pst.setString(3, reclam.getCreated_at());
            pst.setString(4, reclam.getSubject());
            pst.setString(5, reclam.getDescription());
            pst.setString(6, reclam.getStatus());


            pst.executeUpdate();

            // Get the generated reclamation ID
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                int reclamationId = rs.getInt(1);
                saveAttachments(reclamationId, reclam.getAttachments());
            }
        }
    }

    // Method to Save Attachments
    private void saveAttachments(int reclamationId, List<ReclamationAttachment> attachments) throws SQLException {
        if (attachments == null || attachments.isEmpty()) return;

        String query = "INSERT INTO reclamation_attachments (reclamation_id, file_path) VALUES (?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            for (ReclamationAttachment attachment : attachments) {
                pst.setInt(1, reclamationId);
                pst.setString(2, attachment.getFilePath());
                pst.executeUpdate();
            }
        }
    }


    public void addAttachmentToReclamation(ReclamationAttachment reclamAttach) throws SQLException {
        String query = "INSERT INTO reclamation_attachments (reclamation_id, file_path) VALUES (?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reclamAttach.getReclamationId());
            pst.setString(2, reclamAttach.getFilePath());
            pst.executeUpdate();

            // Get generated attachment ID
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                reclamAttach.setId(rs.getInt(1));
            }

            System.out.println("✅ Attachment saved in database: " + reclamAttach.getFilePath());
        }
    }


    @Override
    public void update(Reclamation reclam) throws SQLException {
        String query = "update reclamations set  subject = ? , description = ? where id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setString(1, reclam.getSubject());
        ps.setString(2, reclam.getDescription());
        ps.setInt(3, reclam.getId());
        ps.executeUpdate();
    }



    @Override
    public void delete(int id) throws SQLException {
        // Step 1: Delete all attachments related to this reclamation
        deleteAttachmentsByReclamationId(id);

        // Step 2: Delete the reclamation itself
        String query = "DELETE FROM reclamations WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }




    @Override
    public List<Reclamation> readAll() throws SQLException {
        List<Reclamation> reclams = new ArrayList<>();
        String query = "SELECT * FROM reclamations";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            int id = rs.getInt("id");
            int id_user = rs.getInt("id_user");
            int id_event = rs.getInt("id_event");
            String created_at = rs.getString("created_at");
            String subject = rs.getString("subject");
            String description = rs.getString("description");
            String status = rs.getString("status");

            // Fetch attachments as a List<ReclamationAttachment>
            List<ReclamationAttachment> attachments = getAttachmentsByReclamationId(id);

            // Create Reclamation object with multiple attachments
            Reclamation p = new Reclamation(id, id_user, id_event, created_at, subject, description, status, attachments);
            reclams.add(p);
        }

        return reclams;
    }



    // Helper function to get multiple attachments for a Reclamation
    private List<ReclamationAttachment> getAttachmentsByReclamationId(int reclamationId) throws SQLException {
        List<ReclamationAttachment> attachments = new ArrayList<>();
        String query = "SELECT id, file_path FROM reclamation_attachments WHERE reclamation_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, reclamationId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int attachmentId = rs.getInt("id");
                String filePath = rs.getString("file_path");

                // Create a ReclamationAttachment object and add it to the list
                attachments.add(new ReclamationAttachment(attachmentId, reclamationId, filePath));
            }
        }
        return attachments;
    }



    @Override
    public void deleteAttachment(int attachmentId) throws SQLException {
        String query = "DELETE FROM reclamation_attachments WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, attachmentId);
            pst.executeUpdate();
        }
    }



    @Override
    public void deleteAttachmentsByReclamationId(int reclamationId) throws SQLException {
        String query = "DELETE FROM reclamation_attachments WHERE reclamation_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, reclamationId);
            pst.executeUpdate();
        }
    }




}
