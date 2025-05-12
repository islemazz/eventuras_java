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
import services.userService;
import utils.Session;

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

            // Retrieve user from database using email
            user user = userService.authenticateUser(email, password);

            if (user != null) {
                // Start user session
                Session.getInstance().startSession(user);
                System.out.println("User logged in successfully");

                // Store user session
                UserSession.getInstance(
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
                    user.getRole()
                );

                // Redirect based on role
                String role = user.getRole().toUpperCase(); // Convert to uppercase to match DB format
                System.out.println("User role: " + role); // Debug log
                
                switch (role) {
                    case "ROLE_ADMIN":
                        loadPage("/adminDashboard.fxml");
                        break;
                    case "ROLE_PARTICIPANT":
                        loadPage("/participantDashboard.fxml");
                        break;
                    case "ROLE_ORGANISATEUR":
                        loadPage("/organisateurDashboard.fxml");
                        break;
                    default:
                        System.err.println("Unexpected role: " + role); // Debug log
                        showAlert("Error", "Invalid role configuration. Please contact support.");
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
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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