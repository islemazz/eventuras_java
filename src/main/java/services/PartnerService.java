package services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import entities.Partner;
import entities.PartnerType;
import utils.MyConnection;

public class PartnerService {
    private final MyConnection connection = MyConnection.getInstance();

    public void create(Partner partner) throws SQLException {
        if (!connection.isConnected()) {
            throw new SQLException("Database connection is not available");
        }

        String query = "INSERT INTO partner (name, type, description, email, phone, address, website, rating, rating_count, image_path, video_path, created_at, updated_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, partner.getName());
            statement.setString(2, partner.getType().toString());
            statement.setString(3, partner.getDescription());
            statement.setString(4, partner.getEmail());
            statement.setString(5, partner.getPhone());
            statement.setString(6, partner.getAddress());
            statement.setString(7, partner.getWebsite());
            statement.setDouble(8, partner.getRating());
            statement.setInt(9, partner.getRatingCount());
            statement.setString(10, partner.getImagePath());
            statement.setString(11, partner.getVideoPath());
            statement.setTimestamp(12, Timestamp.valueOf(partner.getCreatedAt()));
            statement.setTimestamp(13, Timestamp.valueOf(partner.getUpdatedAt()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating partner failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    partner.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating partner failed, no ID obtained.");
                }
            }
        }
    }

    public List<Partner> readAll() throws SQLException {
        if (!connection.isConnected()) {
            throw new SQLException("Database connection is not available");
        }

        List<Partner> partners = new ArrayList<>();
        String query = "SELECT * FROM partner";

        try (Statement statement = connection.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Partner partner = new Partner();
                partner.setId(resultSet.getInt("id"));
                partner.setName(resultSet.getString("name"));
                partner.setType(PartnerType.valueOf(resultSet.getString("type")));
                partner.setDescription(resultSet.getString("description"));
                partner.setEmail(resultSet.getString("email"));
                partner.setPhone(resultSet.getString("phone"));
                partner.setAddress(resultSet.getString("address"));
                partner.setWebsite(resultSet.getString("website"));
                partner.setRating(resultSet.getDouble("rating"));
                partner.setRatingCount(resultSet.getInt("rating_count"));
                partner.setImagePath(resultSet.getString("image_path"));
                partner.setVideoPath(resultSet.getString("video_path"));
                partner.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                partner.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                partners.add(partner);
            }
        }
        return partners;
    }

    public void update(Partner partner) throws SQLException {
        if (!connection.isConnected()) {
            throw new SQLException("Database connection is not available");
        }

        String query = "UPDATE partner SET name=?, type=?, description=?, email=?, phone=?, address=?, website=?, " +
                      "rating=?, rating_count=?, image_path=?, video_path=?, updated_at=? WHERE id=?";

        try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
            statement.setString(1, partner.getName());
            statement.setString(2, partner.getType().toString());
            statement.setString(3, partner.getDescription());
            statement.setString(4, partner.getEmail());
            statement.setString(5, partner.getPhone());
            statement.setString(6, partner.getAddress());
            statement.setString(7, partner.getWebsite());
            statement.setDouble(8, partner.getRating());
            statement.setInt(9, partner.getRatingCount());
            statement.setString(10, partner.getImagePath());
            statement.setString(11, partner.getVideoPath());
            statement.setTimestamp(12, Timestamp.valueOf(partner.getUpdatedAt()));
            statement.setInt(13, partner.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating partner failed, no rows affected.");
            }
        }
    }

    public void delete(int id) throws SQLException {
        if (!connection.isConnected()) {
            throw new SQLException("Database connection is not available");
        }

        String query = "DELETE FROM partner WHERE id=?";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
            statement.setInt(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting partner failed, no rows affected.");
            }
        }
    }

    public Partner findById(int id) throws SQLException {
        if (!connection.isConnected()) {
            throw new SQLException("Database connection is not available");
        }

        String query = "SELECT * FROM partner WHERE id=?";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Partner partner = new Partner();
                    partner.setId(resultSet.getInt("id"));
                    partner.setName(resultSet.getString("name"));
                    partner.setType(PartnerType.valueOf(resultSet.getString("type")));
                    partner.setDescription(resultSet.getString("description"));
                    partner.setEmail(resultSet.getString("email"));
                    partner.setPhone(resultSet.getString("phone"));
                    partner.setAddress(resultSet.getString("address"));
                    partner.setWebsite(resultSet.getString("website"));
                    partner.setRating(resultSet.getDouble("rating"));
                    partner.setRatingCount(resultSet.getInt("rating_count"));
                    partner.setImagePath(resultSet.getString("image_path"));
                    partner.setVideoPath(resultSet.getString("video_path"));
                    partner.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    partner.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                    return partner;
                }
            }
        }
        return null;
    }

    public List<Partner> findByType(PartnerType type) throws SQLException {
        if (!connection.isConnected()) {
            throw new SQLException("Database connection is not available");
        }

        List<Partner> partners = new ArrayList<>();
        String query = "SELECT * FROM partner WHERE type=?";
        try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
            statement.setString(1, type.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Partner partner = new Partner();
                    partner.setId(resultSet.getInt("id"));
                    partner.setName(resultSet.getString("name"));
                    partner.setType(PartnerType.valueOf(resultSet.getString("type")));
                    partner.setDescription(resultSet.getString("description"));
                    partner.setEmail(resultSet.getString("email"));
                    partner.setPhone(resultSet.getString("phone"));
                    partner.setAddress(resultSet.getString("address"));
                    partner.setWebsite(resultSet.getString("website"));
                    partner.setRating(resultSet.getDouble("rating"));
                    partner.setRatingCount(resultSet.getInt("rating_count"));
                    partner.setImagePath(resultSet.getString("image_path"));
                    partner.setVideoPath(resultSet.getString("video_path"));
                    partner.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    partner.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                    partners.add(partner);
                }
            }
        }
        return partners;
    }
}
