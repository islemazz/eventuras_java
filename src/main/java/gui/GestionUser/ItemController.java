package gui.GestionUser;


import entities.user;
import gui.mainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.userService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ItemController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ImageView img;

    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLable;

    private user evenement;

    @FXML
    private Button validateButton;

    @FXML
    private Button rejectButton;
    @FXML
    private Button sponsorEventButton;



    private user selectedUser;  // Store the user for this item
    private userService userService = new userService ();


    @FXML
    void initialize() {


    }

    // Static method to update button visibility based on role


    // Method to apply rounded corners
    private void applyRoundedCornersToImage() {
        if (img != null) {
            // Create a rectangle with the same dimensions as the ImageView
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(img.getFitWidth(), img.getFitHeight());

            // Set the corner radius
            clip.setArcWidth(20);  // Adjust this value for more or less rounding
            clip.setArcHeight(20);

            // Apply the clip to the ImageView
            img.setClip(clip);
        }
    }
    // Method to set location information in the UI
    public void setLocation(String name, String Role, Image image, user user) {

        this.selectedUser = user;

        nameLabel.setText(name);
        priceLable.setText( Role); // Format price with two decimal places
        img.setImage(image);

    };


    private void refreshScene(Button button) {
        button.getScene().getWindow().hide(); // Hide current window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private ComboBox<String> sponsorComboBox; // Declare the ComboBox in your FXML file



    // Method to get justification input


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void click(MouseEvent mouseEvent) {
    }

    @FXML
    public void update() {
        System.out.println(selectedUser.getFirstname());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/updateUser.fxml"));
            Parent root = loader.load();


            // Get the controller and pass the selected user
            updateUser updateController = loader.getController();
            updateController.setUser(selectedUser);

            // Open the update window
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update User");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void delete(ActionEvent event) {
        // Check if a user is selected
        if (selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user selected for deletion.");
            return;
        }

        // Get current logged-in user (to prevent deleting your own account)
        UserSession currentSession = null;
        try {
            currentSession = UserSession.getInstance();
        } catch (IllegalStateException e) {
            System.out.println("No user session detected.");
        }

        // Prevent users from deleting their own account
        if (currentSession != null && currentSession.getId() == selectedUser.getId()) {
            showAlert(Alert.AlertType.WARNING, "Forbidden Action",
                    "You cannot delete your own account while logged in.");
            return;
        }

        // Create confirmation dialog
        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("Delete User");
        confirmDelete.setHeaderText("Delete User: " + selectedUser.getUsername());
        confirmDelete.setContentText("Are you sure you want to delete this user?\n" +
                "Name: " + selectedUser.getFirstname() + " " + selectedUser.getLastname() + "\n" +
                "Email: " + selectedUser.getEmail() + "\n" +
                "Role: " + selectedUser.getRole() + "\n\n" +
                "This action cannot be undone!");

        // Get the response from the dialog
        Optional<ButtonType> result = confirmDelete.showAndWait();

        // If the user confirms the deletion
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Log the deletion for audit purposes
                System.out.println("Deleting user: " + selectedUser.getId() + " - " + selectedUser.getUsername());
                System.out.println("Deletion performed by: " +
                        (currentSession != null ? currentSession.getUsername() : "Unknown") +
                        " at " + java.time.LocalDateTime.now());

                // Attempt to delete the user
                userService.deleteUser(selectedUser.getId());

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "User '" + selectedUser.getUsername() + "' has been successfully deleted.");

                // Refresh the scene to update the user list
                if (event.getSource() instanceof Button) {
                    refreshScene((Button) event.getSource());
                } else {
                    // Try to refresh the scene through other means if the source is not a button
                    try {
                        navigateTo("/listUser.fxml", event);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                // Handle any errors that occur during deletion
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to delete user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get the current stage using the event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}