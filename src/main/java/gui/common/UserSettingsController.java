package gui.common;

import java.net.URL;
import java.util.ResourceBundle;

import gui.GestionUser.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class UserSettingsController implements Initializable {

    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label birthdayLabel;
    @FXML private Label genderLabel;
    @FXML private Label phoneLabel;
    @FXML private Label levelLabel;
    @FXML private Label roleLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserSettings();
    }

    private void loadUserSettings() {
        try {
            UserSession session = UserSession.getInstance();

            // Populate fields, checking for nulls from session
            String firstName = session.getFirstname();
            String lastName = session.getLastname();
            fullNameLabel.setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));

            usernameLabel.setText(session.getUsername() != null ? session.getUsername() : "N/A");
            emailLabel.setText(session.getEmail() != null ? session.getEmail() : "N/A");
            birthdayLabel.setText(session.getBirthday() != null ? session.getBirthday() : "N/A");
            genderLabel.setText(session.getGender() != null ? session.getGender() : "N/A");
            phoneLabel.setText(session.getPhonenumber() != null ? session.getPhonenumber() : "N/A");
            levelLabel.setText(String.valueOf(session.getLevel())); // int, so direct conversion
            
            // For role, you might want to map the role ID to a name
            // For now, just displaying the ID
            roleLabel.setText(String.valueOf(session.getRole())); 

        } catch (IllegalStateException e) {
            // Handle case where session might not be initialized (e.g., direct access to FXML without login)
            System.err.println("User session not available for settings page: " + e.getMessage());
            setAllLabelsToNA();
        } catch (Exception e) {
            // Catch any other unexpected errors during data loading
            System.err.println("Error loading user settings: " + e.getMessage());
            e.printStackTrace();
            setAllLabelsToNA();
        }
    }

    private void setAllLabelsToNA() {
        fullNameLabel.setText("N/A");
        usernameLabel.setText("N/A");
        emailLabel.setText("N/A");
        birthdayLabel.setText("N/A");
        genderLabel.setText("N/A");
        phoneLabel.setText("N/A");
        levelLabel.setText("N/A");
        roleLabel.setText("N/A");
    }
} 