package services;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import entities.user;
import utils.MyConnection;

public class userService implements Iuser<user> {

    private final Connection cnx;

    public userService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void addUser(user user) throws SQLException, IOException {
        String query = "INSERT INTO users (user_username, user_email, user_password, user_firstname, user_lastname, " +
                "user_birthday, user_gender, user_picture, user_phonenumber, user_level, role_id, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            String salt = BCrypt.gensalt(10);
            String hashedPassword = BCrypt.hashpw(user.getPassword(), salt);
            System.out.println("New user hashed password: " + hashedPassword);
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, user.getFirstname());
            pstmt.setString(5, user.getLastname());
            pstmt.setString(6, user.getBirthday());
            pstmt.setString(7, user.getGender());
            pstmt.setString(8, user.getPicture());
            pstmt.setString(9, user.getPhonenumber());
            pstmt.setInt(10, user.getLevel());
            pstmt.setInt(11, user.getId_role());
            pstmt.setInt(12, 1); // Default statut value as integer 1

            pstmt.executeUpdate();
        }
    }

    public user getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE user_email = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new user(
                            rs.getInt("user_id"),
                            rs.getString("user_username"),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_firstname"),
                            rs.getString("user_lastname"),
                            rs.getString("user_birthday"),
                            rs.getString("user_gender"),
                            rs.getString("user_picture"),
                            rs.getString("user_phonenumber"),
                            rs.getInt("user_level"),
                            rs.getInt("role_id")
                    );
                }
            }
        }
        return null;
    }

    public boolean isEmailTaken(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE user_email = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE user_username = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    @Override
    public void updateUser(user user, int id) {
        String query = "UPDATE users SET user_username = ?, user_email = ?, user_password = ?, user_firstname = ?, " +
                "user_lastname = ?, user_birthday = ?, user_gender = ?, user_picture = ?, " +
                "user_phonenumber = ?, role_id = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getFirstname());
            pstmt.setString(5, user.getLastname());
            pstmt.setString(6, user.getBirthday());
            pstmt.setString(7, user.getGender());
            pstmt.setString(8, user.getPicture());
            pstmt.setString(9, user.getPhonenumber());
            pstmt.setInt(10, user.getId_role());
            pstmt.setInt(11, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(int id) {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<user> getallUserdata() {
        List<user> list = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                user u = new user(
                        rs.getInt("user_id"),
                        rs.getString("user_username"),
                        rs.getString("user_email"),
                        rs.getString("user_password"),
                        rs.getString("user_firstname"),
                        rs.getString("user_lastname"),
                        rs.getString("user_birthday"),
                        rs.getString("user_gender"),
                        rs.getString("user_picture"),
                        rs.getString("user_phonenumber"),
                        rs.getInt("user_level"),
                        rs.getInt("role_id")
                );
                list.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public boolean updateUserPasswordByEmail(String email, String newPassword) throws SQLException {
        String query = "UPDATE users SET user_password = ? WHERE user_email = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        }
    }

    public void updateforgottenpassword(String email, String password) {
        String query = "UPDATE users SET user_password = ? WHERE user_email = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, BCrypt.hashpw(password, BCrypt.gensalt()));
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public user authenticateUser(String email, String password) {
        String query = "SELECT * FROM users WHERE user_email = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("user_password");
                    int roleId = rs.getInt("role_id");
                    
                    // Debug print to check values
                    System.out.println("=== Authentication Debug ===");
                    System.out.println("Attempting login for email: " + email);
                    System.out.println("Role ID: " + roleId);
                    System.out.println("Is Organizer: " + (roleId == 3));
                    System.out.println("Stored hashed password: " + hashedPassword);
                    System.out.println("Provided password: " + password);
                    
                    try {
                        boolean passwordMatch = BCrypt.checkpw(password, hashedPassword);
                        System.out.println("Password match result: " + passwordMatch);
                        
                        if (passwordMatch) {
                            user authenticatedUser = new user(
                                    rs.getInt("user_id"),
                                    rs.getString("user_username"),
                                    rs.getString("user_email"),
                                    hashedPassword,
                                    rs.getString("user_firstname"),
                                    rs.getString("user_lastname"),
                                    rs.getString("user_birthday"),
                                    rs.getString("user_gender"),
                                    rs.getString("user_picture"),
                                    rs.getString("user_phonenumber"),
                                    rs.getInt("user_level"),
                                    roleId
                            );
                            System.out.println("Authentication successful for user: " + authenticatedUser.getUsername());
                            System.out.println("User role: " + (roleId == 3 ? "Organizer" : "Other"));
                            return authenticatedUser;
                        } else {
                            System.out.println("Password verification failed");
                            // For organizer accounts, let's check if the password might be stored differently
                            if (roleId == 3) {
                                System.out.println("Organizer account - checking alternative password format");
                                // Try with trimmed password
                                String trimmedPassword = password.trim();
                                if (!trimmedPassword.equals(password)) {
                                    System.out.println("Trying with trimmed password");
                                    if (BCrypt.checkpw(trimmedPassword, hashedPassword)) {
                                        System.out.println("Password match with trimmed password");
                                        return new user(
                                            rs.getInt("user_id"),
                                            rs.getString("user_username"),
                                            rs.getString("user_email"),
                                            hashedPassword,
                                            rs.getString("user_firstname"),
                                            rs.getString("user_lastname"),
                                            rs.getString("user_birthday"),
                                            rs.getString("user_gender"),
                                            rs.getString("user_picture"),
                                            rs.getString("user_phonenumber"),
                                            rs.getInt("user_level"),
                                            roleId
                                        );
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error during password verification: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("No user found with email: " + email);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception during authentication: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public Map<String, Long> getGenderDistribution() {
        Map<String, Long> genderDistribution = new HashMap<>();
        String query = "SELECT user_gender, COUNT(*) as count FROM users GROUP BY user_gender";

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                genderDistribution.put(rs.getString("user_gender"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return genderDistribution;
    }

    public Map<String, Long> getAgeDistribution() {
        Map<String, Long> ageDistribution = new HashMap<>();
        String query = "SELECT FLOOR(DATEDIFF(CURDATE(), user_birthday) / 365) AS age_group, COUNT(*) AS count " +
                "FROM users GROUP BY age_group";

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                ageDistribution.put(rs.getString("age_group"), rs.getLong("count"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ageDistribution;
    }

    public user getUserById(int id) throws SQLException {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new user(
                            rs.getInt("user_id"),
                            rs.getString("user_username"),
                            rs.getString("user_email"),
                            rs.getString("user_password"),
                            rs.getString("user_firstname"),
                            rs.getString("user_lastname"),
                            rs.getString("user_birthday"),
                            rs.getString("user_gender"),
                            rs.getString("user_picture"),
                            rs.getString("user_phonenumber"),
                            rs.getInt("user_level"),
                            rs.getInt("role_id")
                    );
                }
            }
        }
        return null;
    }

    public void resetOrganizerPassword(String email, String newPassword) throws SQLException {
        String query = "UPDATE users SET user_password = ? WHERE user_email = ? AND role_id = 3";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            // Use a consistent salt for password hashing
            String salt = BCrypt.gensalt(10);
            String hashedPassword = BCrypt.hashpw(newPassword, salt);
            System.out.println("New hashed password: " + hashedPassword);
            
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, email);
            
            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Password reset successful for organizer: " + email);
            } else {
                System.out.println("No organizer found with email: " + email);
            }
        }
    }
}