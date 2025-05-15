package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entities.Partnership;

public class PartnershipService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/eventuras";
    private static final String USER = "root";
    private static final String PASS = "";

    public PartnershipService() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Database driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading database driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Database connection established successfully");
            return conn;
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            throw e;
        }
    }

    public Partnership create(Partnership partnership) {
        // Validate required fields
        if (partnership.getContractType() == null || partnership.getContractType().isEmpty()) {
            System.err.println("[ERROR] Contract type cannot be null or empty");
            return null;
        }
        if (partnership.getDescription() == null || partnership.getDescription().isEmpty()) {
            System.err.println("[ERROR] Description cannot be null or empty");
            return null;
        }
        
        // Ensure status length is within limits
        if (partnership.getStatus() != null && partnership.getStatus().length() > 20) {
            System.err.println("[ERROR] Status length exceeds 20 characters");
            return null;
        }

        String sql = "INSERT INTO partnership (partner_id, organizerid, contracttype, description, is_signed, status, created_at, signed_contract_file, signed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            System.out.println("[DEBUG] Attempting to insert partnership:");
            System.out.println("partner_id: " + partnership.getPartnerId());
            System.out.println("organizerid: " + partnership.getOrganizerId());
            System.out.println("contracttype: " + partnership.getContractType());
            System.out.println("description: " + partnership.getDescription());
            System.out.println("is_signed: " + partnership.isSigned());
            System.out.println("status: " + partnership.getStatus());
            System.out.println("created_at: " + partnership.getCreatedAt());
            System.out.println("signed_contract_file: " + partnership.getSignedContractFile());
            System.out.println("signed_at: " + partnership.getSignedAt());

            // Set parameters with proper type checking
            pstmt.setInt(1, partnership.getPartnerId());
            pstmt.setInt(2, partnership.getOrganizerId());
            pstmt.setString(3, partnership.getContractType());
            pstmt.setString(4, partnership.getDescription());
            pstmt.setBoolean(5, partnership.isSigned());
            
            // Handle nullable status
            if (partnership.getStatus() == null || partnership.getStatus().isEmpty()) {
                pstmt.setNull(6, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(6, partnership.getStatus());
            }
            
            // Handle created_at
            if (partnership.getCreatedAt() == null) {
                pstmt.setNull(7, java.sql.Types.TIMESTAMP);
            } else {
                pstmt.setObject(7, partnership.getCreatedAt());
            }
            
            // Handle nullable signed_contract_file
            if (partnership.getSignedContractFile() == null || partnership.getSignedContractFile().isEmpty()) {
                pstmt.setNull(8, java.sql.Types.VARCHAR);
            } else {
                pstmt.setString(8, partnership.getSignedContractFile());
            }
            
            // Handle nullable signed_at
            if (partnership.getSignedAt() == null) {
                pstmt.setNull(9, java.sql.Types.TIMESTAMP);
            } else {
                pstmt.setObject(9, partnership.getSignedAt());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("[ERROR] No rows inserted!");
                return null;
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                partnership.setId(rs.getInt(1));
                System.out.println("[DEBUG] Partnership inserted with ID: " + partnership.getId());
                return partnership;
            } else {
                System.err.println("[ERROR] Failed to get generated ID");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Exception during insert: " + e.getMessage());
            System.err.println("[ERROR] SQL State: " + e.getSQLState());
            System.err.println("[ERROR] Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
    }

    public Partnership update(Partnership partnership) {
        String sql = "UPDATE partnership SET partner_id = ?, organizerid = ?, contracttype = ?, description = ?, is_signed = ?, status = ?, created_at = ?, signed_contract_file = ?, signed_at = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, partnership.getPartnerId());
            pstmt.setInt(2, partnership.getOrganizerId());
            pstmt.setString(3, partnership.getContractType());
            pstmt.setString(4, partnership.getDescription());
            pstmt.setBoolean(5, partnership.isSigned());
            pstmt.setString(6, partnership.getStatus());
            pstmt.setObject(7, partnership.getCreatedAt());
            pstmt.setString(8, partnership.getSignedContractFile());
            pstmt.setObject(9, partnership.getSignedAt());
            pstmt.setInt(10, partnership.getId());
            pstmt.executeUpdate();
            return partnership;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM partnership WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Partnership read(int id) {
        String sql = "SELECT * FROM partnership WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPartnership(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Partnership> readAll() {
        String sql = "SELECT * FROM partnership";
        List<Partnership> partnerships = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                partnerships.add(mapResultSetToPartnership(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partnerships;
    }

    public List<Partnership> readByPartnerId(int partnerId) {
        String sql = "SELECT * FROM partnership WHERE partner_id = ?";
        List<Partnership> partnerships = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, partnerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                partnerships.add(mapResultSetToPartnership(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partnerships;
    }

    public List<Partnership> readByOrganizerId(int organizerId) {
        String sql = "SELECT * FROM partnership WHERE organizerid = ?";
        List<Partnership> partnerships = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, organizerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                partnerships.add(mapResultSetToPartnership(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partnerships;
    }

    private Partnership mapResultSetToPartnership(ResultSet rs) throws SQLException {
        Partnership partnership = new Partnership();
        partnership.setId(rs.getInt("id"));
        partnership.setPartnerId(rs.getInt("partner_id"));
        partnership.setOrganizerId(rs.getInt("organizerid"));
        partnership.setContractType(rs.getString("contracttype"));
        partnership.setDescription(rs.getString("description"));
        partnership.setSigned(rs.getBoolean("is_signed"));
        partnership.setStatus(rs.getString("status"));
        
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        partnership.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
        
        partnership.setSignedContractFile(rs.getString("signed_contract_file"));
        
        java.sql.Timestamp signedAt = rs.getTimestamp("signed_at");
        partnership.setSignedAt(signedAt != null ? signedAt.toLocalDateTime() : null);
        
        return partnership;
    }

    // New method to record signature verification
    public boolean recordSignatureVerified(int partnershipId, String signedContractPath) throws SQLException {
        String sql = "UPDATE partnership SET is_signed = ?, signed_contract_file = ?, signed_at = ?, status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, true);
            pstmt.setString(2, signedContractPath);
            pstmt.setObject(3, LocalDateTime.now()); // Set signed_at to current time
            pstmt.setString(4, "Signed"); // Update status to "Signed"
            pstmt.setInt(5, partnershipId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("[DEBUG] Partnership ID " + partnershipId + " marked as signed. Path: " + signedContractPath);
                return true;
            }
            System.err.println("[ERROR] Failed to update signature status for partnership ID: " + partnershipId);
            return false;
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Exception during signature update for partnership ID " + partnershipId + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by the caller
        }
    }

    // New method to delete all unsigned partnerships
    public int deleteAllUnsignedPartnerships() {
        List<Partnership> allPartnerships = readAll();
        int deletedCount = 0;
        if (allPartnerships == null || allPartnerships.isEmpty()) {
            System.out.println("[INFO] No partnerships found in the database.");
            return 0;
        }

        List<Partnership> toDelete = new ArrayList<>();
        for (Partnership p : allPartnerships) {
            if (!p.isSigned()) {
                toDelete.add(p);
            }
        }

        if (toDelete.isEmpty()) {
            System.out.println("[INFO] No unsigned partnerships found to delete.");
            return 0;
        }

        System.out.println("[INFO] Found " + toDelete.size() + " unsigned partnerships to delete.");
        for (Partnership p : toDelete) {
            System.out.println("[INFO] Deleting unsigned partnership with ID: " + p.getId() + ", Status: " + p.getStatus());
            if (delete(p.getId())) {
                deletedCount++;
            } else {
                System.err.println("[ERROR] Failed to delete partnership with ID: " + p.getId());
            }
        }
        System.out.println("[INFO] Successfully deleted " + deletedCount + " unsigned partnerships.");
        return deletedCount;
    }
}
