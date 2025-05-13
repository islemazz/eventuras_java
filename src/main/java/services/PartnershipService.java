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
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/eventuras";
    private static final String USER = "root";
    private static final String PASS = "";

    public PartnershipService() {
        // No initialization needed for JDBC
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public Partnership create(Partnership partnership) {
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

            pstmt.setInt(1, partnership.getPartnerId());
            pstmt.setInt(2, partnership.getOrganizerId());
            pstmt.setString(3, partnership.getContractType());
            pstmt.setString(4, partnership.getDescription());
            pstmt.setBoolean(5, partnership.isSigned());
            pstmt.setString(6, partnership.getStatus());
            pstmt.setObject(7, partnership.getCreatedAt());
            pstmt.setString(8, partnership.getSignedContractFile());
            pstmt.setObject(9, partnership.getSignedAt());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("[ERROR] No rows inserted!");
                return null;
            }
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                partnership.setId(rs.getInt(1));
            }
            System.out.println("[DEBUG] Partnership inserted with ID: " + partnership.getId());
            return partnership;
        } catch (SQLException e) {
            System.err.println("[ERROR] SQL Exception during insert: " + e.getMessage());
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
}
