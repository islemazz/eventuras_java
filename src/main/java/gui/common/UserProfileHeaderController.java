package gui.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import gui.GestionUser.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UserProfileHeaderController implements Initializable {

    @FXML
    private HBox userProfileHeader;

    @FXML
    private Label userNameLabel;

    @FXML
    private ImageView userProfileImageView;

    @FXML
    private MenuButton userActionsMenuButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
        // Optional: Set an icon for the MenuButton
        // Example: 
        // try {
        //     ImageView menuIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/icons/gear_icon.png")));
        //     menuIcon.setFitHeight(16);
        //     menuIcon.setFitWidth(16);
        //     userActionsMenuButton.setGraphic(menuIcon);
        // } catch (Exception e) { System.err.println("Menu button icon not found."); }
    }

    private void loadUserData() {
        try {
            UserSession session = UserSession.getInstance();
            // Ensure session and essential data are non-null
            if (session != null && session.getFirstname() != null) { 
                String MFN = session.getFirstname();
                String MLN = session.getLastname();
                userNameLabel.setText(MFN + " " + (MLN != null ? MLN : ""));

                String imagePath = session.getPicture(); // Use direct method

                if (imagePath != null && !imagePath.isEmpty()) {
                    try {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists() && imageFile.isFile()) {
                            userProfileImageView.setImage(new Image(imageFile.toURI().toString()));
                        } else {
                            System.err.println("User profile image not found or is not a file: " + imagePath);
                            setDefaultProfileImage();
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading user profile image: " + imagePath + "; " + e.getMessage());
                        setDefaultProfileImage();
                    }
                } else {
                    System.out.println("User profile image path is null or empty. Using default.");
                    setDefaultProfileImage();
                }
            } else {
                System.out.println("User session or first name is null. Setting to Guest.");
                userNameLabel.setText("Guest");
                setDefaultProfileImage();
                userActionsMenuButton.setVisible(false); 
            }
        } catch (IllegalStateException e) {
            System.out.println("No active user session for header: " + e.getMessage());
            userNameLabel.setText("Guest");
            setDefaultProfileImage();
            userProfileHeader.setVisible(false); 
        }
    }

    private void setDefaultProfileImage() {
        try {
            // Ensure you have '/images/default_profile.png' in your resources
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
            userProfileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Default profile image ('/images/default_profile.png') not found or error loading: " + e.getMessage());
            userProfileImageView.setImage(null); 
        }
    }

    @FXML
    void handleSettingsAction(ActionEvent event) {
        System.out.println("Settings action triggered");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/common/UserSettings.fxml"));
            Parent settingsRoot = loader.load();
            Stage settingsStage = new Stage();
            settingsStage.setTitle("User Settings");
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            if (userProfileHeader.getScene() != null && userProfileHeader.getScene().getWindow() != null) {
                 settingsStage.initOwner(userProfileHeader.getScene().getWindow());
            }
            settingsStage.setScene(new Scene(settingsRoot));
            settingsStage.showAndWait();
        } catch (IOException e) {
            System.err.println("Failed to load UserSettings.fxml: " + e.getMessage());
            e.printStackTrace();
            // Consider showing a user-friendly alert here
        }
    }

    @FXML
    void handleLogoutAction(ActionEvent event) {
        System.out.println("Logout action triggered");
        try {
            UserSession.cleanUserSession();
            // Ensure the path to login.fxml is correct
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/login.fxml"));
            if (userProfileHeader.getScene() != null) {
                userProfileHeader.getScene().setRoot(loginRoot);
            } else {
                System.err.println("Logout: Cannot get current scene to navigate to login.");
                // Fallback: try to create a new stage for login if current scene is somehow null
                Stage primaryStage = (Stage) userActionsMenuButton.getScene().getWindow(); // A bit of a hack to get the stage
                primaryStage.setScene(new Scene(loginRoot));
                primaryStage.show();
            }
        } catch (Exception e) {
            System.err.println("Failed to logout and navigate to login: " + e.getMessage());
            e.printStackTrace();
            // Consider showing a user-friendly alert here
        }
    }
} 