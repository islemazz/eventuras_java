package gui.GestionUser;

import entities.user;
import org.mindrot.jbcrypt.BCrypt;
import services.userService;
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
            user user = userService.getUserByEmail(email); // Implement this method in your service

            // Check if the password matches
            if (user!=null) {
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
                UserSession session = UserSession.getInstance(); // ✅ Maintenant l'instance existe

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
                // Password is incorrect
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