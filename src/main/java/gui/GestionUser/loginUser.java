package gui.GestionUser;

import java.io.IOException;
import java.sql.SQLException;

import entities.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

public class loginUser {

    @FXML
    private TextField email_input;

    @FXML
    private PasswordField password_input;

    @FXML
    private Text error;

    @FXML
    private Button submitButton;

    private userService userService = new userService();

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    @FXML
    void login() throws SQLException {
        try {
            String email = email_input.getText().trim();
            String password = password_input.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Email and password cannot be empty.");
                return;
            }

            if (!isValidEmail(email)) {
                showAlert("Error", "Invalid email format.");
                return;
            }

            // Retrieve user from database
            user user = userService.getUserByEmail(email);

            if (user != null) {
                String storedHash = user.getPassword();

                // Convert $2y$ → $2a$ for BCrypt compatibility
                if (storedHash != null && storedHash.startsWith("$2y$")) {
                    storedHash = storedHash.replaceFirst("^\\$2y\\$", "\\$2a\\$");
                }

                // Verify password
                if (BCrypt.checkpw(password, storedHash)) {
                    UserSession.createSession(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getPassword(),
                            user.getFirstname(),
                            user.getLastname(),
                            user.getBirthday(),
                            user.getGender(),
                            user.getPicture(),
                            user.getPhonenumber(),
                            user.getLevel(),
                            user.getId_role()
                    );
                    UserSession session = UserSession.getInstance();

                    // Redirect based on role
                    switch (user.getId_role()) {
                        case 1:
                            loadPage("/adminDashboard.fxml");
                            break;
                        case 2:
                            loadPage("/participantDashboard.fxml");
                            break;
                        case 3:
                            loadPage("/organisateurDashboard.fxml");
                            break;
                        default:
                            showAlert("Error", "Unknown role. Please contact support.");
                    }
                } else {
                    showAlert("Error", "Incorrect email or password. Please try again.");
                }
            } else {
                showAlert("Error", "Incorrect email or password. Please try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred. Please try again.");
        }
    }


    @FXML
    void register_page(ActionEvent event) throws IOException {
        loadPage("/registerUser.fxml");
    }

    @FXML
    void redirect_passwordpage(ActionEvent event) throws IOException {
        loadPage("/forgotPassword.fxml");
    }

    private void loadPage(String fxmlPath) throws IOException {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlPath.substring(1)));
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void initialize() {
        error.setVisible(false);
    }
}