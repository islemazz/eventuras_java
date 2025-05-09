package services;

import entities.ContractType;
import entities.Partnership;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnershipService implements IService2<Partnership> {

    private Connection cnx;

    public PartnershipService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void create(Partnership partnership) throws SQLException {
        // Updated query to exclude organizerSignature and partnerSignature
        String query = "INSERT INTO partnership (organizerId, partnerId, contractType, description, isSigned) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, partnership.getOrganizerId());
        ps.setInt(2, partnership.getPartnerId());
        ps.setString(3, partnership.getContractType().name()); // Convert Enum to String
        ps.setString(4, partnership.getDescription());
        ps.setBoolean(5, partnership.isSigned()); // Fixed index for isSigned
        ps.executeUpdate();
    }

    @Override
    public void update(Partnership partnership) throws SQLException {
        // Updated query to exclude organizerSignature and partnerSignature
        String query = "UPDATE partnership SET organizerId = ?, partnerId = ?, contractType = ?, description = ?, isSigned = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, partnership.getOrganizerId());
        ps.setInt(2, partnership.getPartnerId());
        ps.setString(3, partnership.getContractType().name()); // Convert Enum to String
        ps.setString(4, partnership.getDescription());
        ps.setBoolean(5, partnership.isSigned()); // Fixed index for isSigned
        ps.setInt(6, partnership.getId()); // Fixed index for id
        ps.executeUpdate();
    }

    @Override
    public void delete(Partnership partnership) throws SQLException {
        String query = "DELETE FROM partnership WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, partnership.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Partnership> readAll() throws SQLException {
        List<Partnership> partnerships = new ArrayList<>();
        String query = "SELECT * FROM partnership";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            int id = rs.getInt("id");
            int organizerId = rs.getInt("organizerId");
            int partnerId = rs.getInt("partnerId");
            ContractType contractType = ContractType.valueOf(rs.getString("contractType")); // Convert String to Enum
            String description = rs.getString("description");
            boolean isSigned = rs.getBoolean("isSigned"); // Removed organizerSignature and partnerSignature

            Partnership partnership = new Partnership(id, organizerId, partnerId, contractType, description, isSigned); // Updated constructor call
            partnerships.add(partnership);
        }

        return partnerships;
    }
}
