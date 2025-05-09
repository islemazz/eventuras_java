package services;

import entities.user;
import org.mindrot.jbcrypt.BCrypt;
import utils.MyConnection;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class userService implements Iuser<user> {

    private Connection cnx;
    public userService() {
        cnx=MyConnection.getInstance().getConnection();
    }
    //Add user methode
    @Override
    public void addUser(user user) throws SQLException, IOException {
        // The error is happening because we're trying to use user.getRole() but we should be using the id_role directly
        int roleId = getRoleIdByName(user.getRole());
    if(roleId==-1){
        throw new SQLException();
    }
        System.out.println("Using role ID: " + roleId);

        // Insert the user into the 'users' table with the role_id
        String query = "INSERT INTO users (user_username, user_email, user_password, user_firstname, user_lastname, " +
                "user_birthday, user_gender, user_picture, user_phonenumber, user_level,role_id,user_role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            //add hash password here
            pstmt.setString(3, BCrypt.hashpw( user.getPassword(), BCrypt.gensalt()));  // Consider encrypting here
            pstmt.setString(4, user.getFirstname());
            pstmt.setString(5, user.getLastname());
            pstmt.setString(6, user.getBirthday());
            pstmt.setString(7, user.getGender());
            pstmt.setString(8, user.getPicture());
            pstmt.setString(9, user.getPhonenumber());
            pstmt.setInt(10, user.getLevel());
            pstmt.setInt(11, roleId);
            pstmt.setString(12, getRoleNameById(roleId));

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
                            getRoleNameById(rs.getInt("role_id")) // Fetch role name using role_id
                    );
                }
            }
        }
        return null; // Return null if user is not found
    }


    // Helper method to fetch the role_id by role_name

    // Update the getRoleIdByName method to match your actual database schema
    private int getRoleIdByName(String roleName) throws SQLException {
        // Check your actual table and column names
        String query = "SELECT role_id FROM role WHERE role_name = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, roleName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("role_id");
                }
            }
        }
        return -1;  // Return -1 if role_name doesn't exist
    }

    public boolean isEmailTaken(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE user_email = ?";
        try (Connection connection = MyConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Returns true if email is taken
            }
        }
        return false;
    }
    public boolean isUsernameTaken(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE user_username = ?";
        try (Connection connection = MyConnection.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Returns true if username is taken
            }
        }
        return false;
    }

    public Map<String, Integer> getRoles() throws SQLException {
        Map<String, Integer> roles = new HashMap<>();
        String query = "SELECT id_role, user_role FROM role";
        try (Connection connection = MyConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String roleName = resultSet.getString("user_role");
                int idRole = resultSet.getInt("id_role");
                roles.put(roleName, idRole);
            }
        }
        return roles;
    }
    public boolean updateUserPasswordByEmail(String email, String newPassword) throws SQLException {
        Connection cnx = MyConnection.getInstance().getConnection(); // Ensure a valid connection
        System.out.println("hedhy " + cnx);

        // Check if the connection is open
        if (cnx == null || cnx.isClosed()) {
            System.out.println("Database connection is closed or null.");
            return false; // Prevent executing a query on a closed connection
        }

        String query = "UPDATE users SET user_password = ? WHERE user_email = ?";

        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            //add hash password here
            pstmt.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt())); // Hash the password
            pstmt.setString(2, email);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }


    public void updateUser2(user user, int id) {
        String query = "UPDATE users " +
                "SET user_username = ?, user_email = ?, user_firstname = ?, user_lastname = ?, " +
                "user_birthday = ?, user_gender = ?, user_picture = ?, user_phonenumber = ?, " +
                "role_id = ? " + // Include role_id in the update
                "WHERE user_id = ?";

        try {
            PreparedStatement preparedStatement = MyConnection.getInstance().getConnection().prepareStatement(query);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getFirstname());
            preparedStatement.setString(4, user.getLastname());
            preparedStatement.setString(5, user.getBirthday());
            preparedStatement.setString(6, user.getGender());
            preparedStatement.setString(7, user.getPicture());
            preparedStatement.setString(8, user.getPhonenumber());
            preparedStatement.setInt(9, user.getId_role()); // Set the role ID
            preparedStatement.setInt(10, id);

            // Debug output
            System.out.println("Executing update with role_id: " + user.getId_role());
            System.out.println("Updating user ID: " + id);

            int rowsUpdated = preparedStatement.executeUpdate();
            System.out.println("User updated! Rows affected: " + rowsUpdated);
        } catch (SQLException e) {
            System.err.println("SQL Error while updating user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //Update User methode
    @Override
    public void updateUser(user user, int id) {

        String query = "UPDATE users " +
                "SET user_username = ?, user_email = ?, user_password = ?, user_firstname = ?, " +
                "user_lastname = ?, user_birthday = ?, user_gender = ?, user_picture = ?, " +
                "user_phonenumber = ?, role_id = ? " +  // Added role_id
                "WHERE user_id = ?";

        try {
            PreparedStatement preparedStatement = MyConnection.getInstance().getConnection().prepareStatement(query);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getFirstname());
            preparedStatement.setString(5, user.getLastname());
            preparedStatement.setString(6, user.getBirthday());
            preparedStatement.setString(7, user.getGender());
            preparedStatement.setString(8, user.getPicture());
            preparedStatement.setString(9, user.getPhonenumber());
            preparedStatement.setInt(10, user.getId_role());  // Set role_id
            preparedStatement.setInt(11, id);

            preparedStatement.executeUpdate();
            System.out.println("User updated!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Delete User methode
    @Override
    public void deleteUser(int id) {
        String query = "DELETE FROM users WHERE user_id = ?";
        try {
            PreparedStatement preparedStatement = MyConnection.getInstance().getConnection().prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            System.out.println("User with the id = "+ id+" is deleted!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Retrieving data from database
    @Override
    public List<user> getallUserdata() {
        List<user> list = new ArrayList<>();
        String query = "SELECT * FROM users";
        try {
            Statement srt = MyConnection.getInstance().getConnection().createStatement();
            ResultSet rs = srt.executeQuery(query);
            while(rs.next()){
                user user = new user();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("user_username"));
                user.setEmail(rs.getString("user_email"));
                user.setPassword(rs.getString("user_password"));
                user.setFirstname(rs.getString("user_firstname"));
                user.setLastname(rs.getString("user_lastname"));
                user.setBirthday(rs.getString("user_birthday"));
                user.setGender(rs.getString("user_gender"));
                user.setPicture(rs.getString("user_picture"));
                user.setPhonenumber(rs.getString("user_phonenumber"));
                user.setLevel(rs.getInt("user_level"));
                user.setRole(rs.getString("user_role"));
                user.setId_role(rs.getInt("role_id"));  // Added role_id to the user object

                list.add(user);
            }
            System.out.println("All users are added to the list!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    //Methode to login
    public user loginUser(String Username, String password){
        String query = "SELECT * FROM users WHERE user_username = ? AND user_password = ?";
        String encryptedPassword = encrypt(password);

        try (PreparedStatement preparedStatement = MyConnection.getInstance().getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, Username);
            preparedStatement.setString(2, encryptedPassword);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // User login successful, create a user object and return it
                    int userId = resultSet.getInt("user_id");
                    String userName = resultSet.getString("user_username");
                    String userEmail = resultSet.getString("user_email");
                    String userPassword = resultSet.getString("user_password");
                    String userFirstname = resultSet.getString("user_firstname");
                    String userLastname = resultSet.getString("user_lastname");
                    String userBirthday = resultSet.getString("user_birthday");
                    String userGender = resultSet.getString("user_gender");
                    String userPicture = resultSet.getString("user_picture");
                    String userPhone = resultSet.getString("user_phonenumber");
                    int userLevel = resultSet.getInt("user_level");
                    String userRole = resultSet.getString("user_role");

                    return new user(userId, userName, userEmail, userPassword, userFirstname, userLastname, userBirthday, userGender, userPicture, userPhone, userLevel, userRole);
                } else {
                    return null;

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    //Methode to encrypt the Username password
    public static String encrypt(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void updateforgottenpassword(String email, String password) {

        String query = "UPDATE users SET user_password = ? WHERE user_email = ?";

        String x =hashPassword(password);
        try {
            PreparedStatement preparedStatement = MyConnection.getInstance().getConnection().prepareStatement(query);;
            preparedStatement.setString(1, x);
            preparedStatement.setString(2, email);
            preparedStatement.executeUpdate();
            System.out.println("Password updated!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Add a method to retrieve gender distribution data
    public Map<String, Long> getGenderDistribution() {
        Map<String, Long> genderDistribution = new HashMap<>();
        String query = "SELECT user_gender, COUNT(*) as count FROM users GROUP BY user_gender";

        try {
            Statement statement = MyConnection.getInstance().getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String gender = resultSet.getString("user_gender");
                long count = resultSet.getLong("count");
                genderDistribution.put(gender, count);
            }

            System.out.println("Gender distribution retrieved!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return genderDistribution;
    }

    public Map<String, Long> getAgeDistribution() {
        Map<String, Long> ageDistribution = new HashMap<>();

        // Example query to get age distribution based on birthdate
        String query = "SELECT FLOOR(DATEDIFF(CURDATE(), user_birthday) / 365) AS age_group, COUNT(*) AS count " +
                "FROM users " +
                "GROUP BY age_group";

        try {
            Statement statement = MyConnection.getInstance().getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String ageGroup = resultSet.getString("age_group");
                long count = resultSet.getLong("count");
                ageDistribution.put(ageGroup, count);
            }

            System.out.println("Age distribution retrieved!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ageDistribution;
    }

    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }


    public user authenticateUser(String email, String password) {

        String query = "SELECT * FROM users WHERE user_email = ? AND user_password = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Create and populate the user object
                    user authenticatedUser = new user();
                    authenticatedUser.setId(rs.getInt("user_id"));
                    authenticatedUser.setUsername(rs.getString("user_username"));
                    authenticatedUser.setEmail(rs.getString("user_email"));
                    authenticatedUser.setPassword(rs.getString("user_password"));
                    authenticatedUser.setFirstname(rs.getString("user_firstname"));
                    authenticatedUser.setLastname(rs.getString("user_lastname"));
                    authenticatedUser.setBirthday(rs.getString("user_birthday"));
                    authenticatedUser.setGender(rs.getString("user_gender"));
                    authenticatedUser.setPicture(rs.getString("user_picture"));
                    authenticatedUser.setPhonenumber(rs.getString("user_phonenumber"));
                    authenticatedUser.setLevel(rs.getInt("user_level"));

                    // Retrieve and set the user's role
                    String roleName = getRoleNameById(rs.getInt("role_id"));
                    authenticatedUser.setRole(roleName);

                    return authenticatedUser;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error during authentication", e);
        }
        return null; // Return null if authentication fails
    }

    // Helper method to fetch role_name by role_id
    private String getRoleNameById(int roleId) throws SQLException {
        String query = "SELECT role_name FROM role WHERE role_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role_name");
                }
            }
        }
        return null; // Return null if role_id is not found
    }
    public user getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        System.out.println(id);
        if (rs.next()) {
            user u = new user();
            u.setId(rs.getInt("user_id"));
            u.setUsername(rs.getString("user_username"));
            u.setEmail(rs.getString("user_email"));
            u.setFirstname(rs.getString("user_firstname"));
            u.setLastname(rs.getString("user_lastname"));
            u.setPicture(rs.getString("user_picture"));
            // … récupérer les autres champs si besoin
            return u;
        }
        return null;
    }

}