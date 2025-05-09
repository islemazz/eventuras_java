package services;

import entities.Partner;
import entities.PartnerType;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerService implements IService2<Partner> {

    private Connection cnx;

    public PartnerService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void create(Partner partner) throws SQLException {
        String query = "INSERT INTO partner (name, type, contactInfo,VideoPath) VALUES (?, ?, ?,?)";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setString(1, partner.getName());
        ps.setString(2, partner.getType().name()); // Convert Enum to String
        ps.setString(3, partner.getContactInfo());
        ps.setString(4, partner.getImagePath());
        ps.executeUpdate();
    }

    @Override
    public void update(Partner partner) throws SQLException {
        String query = "UPDATE partner SET name = ?, type = ?, contactInfo = ?, videoPath = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setString(1, partner.getName());
        ps.setString(2, partner.getType().name()); // Convert Enum to String
        ps.setString(3, partner.getContactInfo());
        ps.setString(4, partner.getImagePath()); // Set the imagePath
        ps.setInt(5, partner.getId());
        ps.executeUpdate();
    }


    @Override
    public void delete(Partner partner) throws SQLException {
        String query = "DELETE FROM partner WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setInt(1, partner.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Partner> readAll() throws SQLException {
        List<Partner> partners = new ArrayList<>();
        String query = "SELECT * FROM partner";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            PartnerType type = PartnerType.valueOf(rs.getString("type")); // Convert String to Enum
            String contactInfo = rs.getString("contactInfo");
            String videoPath = rs.getString("VideoPath");
            Partner partner = new Partner(id, name, type, contactInfo,videoPath);
            partners.add(partner);
        }

        return partners;
    }
}
