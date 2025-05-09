package gui.GestionUser;


import entities.Role;
import entities.user;
import gui.mainController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import services.Crole;
import services.userService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class editCurrentUser {

    @FXML
    private DatePicker birthday_input;
    @FXML
    private TextField email_input;
    @FXML
    private Text error;

    @FXML
    private TextField firstname_input;
    @FXML
    private ComboBox<String> gender_combobox;
    @FXML
    private TextField lastname_input;
    @FXML
    private TextField password_input;
    @FXML
    private TextField phonenumber_input;
    @FXML
    private TextField picture_input;
    @FXML
    private TextField username_input;
    @FXML
    private Pane imagePane; // Changed from ImageView to Pane
    private boolean isInitialized = false;

    private UserSession userSession = UserSession.getInstance();
    private userService userService = new userService();
    private Crole croleService = new Crole();
    private boolean imageChanged = false;
    private String originalImagePath;

    @FXML
    void back_to_list(ActionEvent event) throws IOException {
        navigateTo("/listUser.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get the current stage using the event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    void reset_input(ActionEvent event) {
        // Reset form to original user data
        resetForm();
        error.setVisible(false);
        imageChanged = false;
    }

    /**
     * Reset the form fields to the current user's data
     */
    private void resetForm() {
        firstname_input.setText(userSession.getFirstname());
        lastname_input.setText(userSession.getLastname());
        phonenumber_input.setText(userSession.getPhonenumber());
        username_input.setText(userSession.getUsername());
        email_input.setText(userSession.getEmail());
        picture_input.setText(userSession.getPicture());
        gender_combobox.setValue(userSession.getGender());

        // Parse and set birthday
        String birthdayString = userSession.getBirthday();
        try {
            LocalDate date = LocalDate.parse(birthdayString);
            birthday_input.setValue(date);
        } catch (Exception e) {
            System.err.println("Error parsing birthday: " + e.getMessage());
        }

        // Display current profile image
        displayProfileImage(userSession.getPicture());
    }

    @FXML
    void submit_user(ActionEvent event) {
        if (validateForm()) {
            String picturePath = picture_input.getText();

            // Format the date
            LocalDate selectedDate = birthday_input.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedBirthday = selectedDate.format(formatter);

            // Get selected gender
            String selectedGender = gender_combobox.getValue();





            // Create updated user object with current session user ID
            user updatedUser = new user(
                    userSession.getId(),
                    username_input.getText(),
                    email_input.getText(),
                    hashPassword(password_input.getText()),
                    firstname_input.getText(),
                    lastname_input.getText(),
                    formattedBirthday,
                    selectedGender,
                    picturePath,
                    phonenumber_input.getText()
            );

            // Set role ID
            updatedUser.setId_role(1);
            updatedUser.setRole("Admin");

            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Update Profile");
            alert.setContentText("Are you sure you want to update your profile?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Handle image if changed
                    if (imageChanged) {
                        // Get the source file
                        File sourceFile = new File(picturePath);

                        // Prepare destination path
                        Path destinationDir = Paths.get("src/main/resources/images/Profilepictures");
                        if (!Files.exists(destinationDir)) {
                            Files.createDirectories(destinationDir);
                        }

                        String fileName = sourceFile.getName();
                        Path destinationPath = destinationDir.resolve(fileName);

                        // Copy the new image
                        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                        // Update the path in the user object
                        updatedUser.setPicture(destinationPath.toString());
                    }

                    // Update the user in the database
                    userService.updateUser(updatedUser, updatedUser.getId());

                    // Update the user session with the new values
                    updateUserSession(updatedUser);

                    // Show success message
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile has been updated successfully!");

                    // Close current window and navigate to appropriate page
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();

                    // Navigate to appropriate view based on user role
                    if (userSession.getRole().equalsIgnoreCase("Admin")) {
                        navigateTo("/listUser.fxml", event);
                    } else {
                        // Load user dashboard or home page for non-admins
                        navigateTo("/adminDashboard.fxml", event);
                    }

                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Update the user session with the updated user data
     */
    private void updateUserSession(user updatedUser) {
        userSession.setFirstname(updatedUser.getFirstname());
        userSession.setLastname(updatedUser.getLastname());
        userSession.setEmail(updatedUser.getEmail());
        userSession.setUsername(updatedUser.getUsername());
        userSession.setPassword(updatedUser.getPassword());
        userSession.setPicture(updatedUser.getPicture());
        userSession.setBirthday(updatedUser.getBirthday());
        userSession.setGender(updatedUser.getGender());
        userSession.setPhonenumber(updatedUser.getPhonenumber());
        userSession.setRole(updatedUser.getRole());
        // Update other fields as needed
    }


    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }



    @FXML
    void upload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Get the stage from the event source for better dialog positioning
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Set the image path to the text field
            picture_input.setText(selectedFile.getAbsolutePath());
            imageChanged = true;

            // Load and display the selected image
            try {
                Image image = new Image(selectedFile.toURI().toString());
                displayImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Display an image in the imagePane
     */
    private void displayImage(Image image) {
        // Create an ImageView to display the image
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Clear existing content in imagePane
        imagePane.getChildren().clear();

        // Add the image view to the pane
        imagePane.getChildren().add(imageView);
    }

    /**
     * Display profile image from path
     */
    private void displayProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    // Image exists locally, load from file
                    Image image = new Image(imageFile.toURI().toString());
                    displayImage(image);
                } else {
                    // Try loading from resources
                    URL resourceUrl = getClass().getResource("/images/" + imagePath);
                    if (resourceUrl != null) {
                        Image image = new Image(resourceUrl.toExternalForm());
                        displayImage(image);
                    } else {
                        System.err.println("Image not found: " + imagePath);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error displaying profile image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @FXML
    void initialize() {
        // Initialize gender options
        ObservableList<String> genderOptions = FXCollections.observableArrayList("Male", "Female");
        gender_combobox.setItems(genderOptions);

        // Hide picture input field and error message
        picture_input.setVisible(false);
        error.setVisible(false);

        // Store original image path
        originalImagePath = userSession.getPicture();

        // Fill form with current user data
        resetForm();


    }

    private boolean validateForm() {
        // Reset error visibility
        error.setVisible(false);

        // Check if any required fields are empty
        if (username_input.getText().isEmpty() ||
                email_input.getText().isEmpty() ||
                password_input.getText().isEmpty() ||
                firstname_input.getText().isEmpty() ||
                lastname_input.getText().isEmpty() ||
                birthday_input.getValue() == null ||
                gender_combobox.getValue() == null ||
                phonenumber_input.getText().isEmpty()) {

            error.setText("All fields must be filled");
            error.setVisible(true);
            return false;
        }

        // Validate email
        String email = email_input.getText();
        if (!isEmailValid(email)) {
            error.setText("Invalid email address");
            error.setVisible(true);
            return false;
        }

        // Validate password
        if (!isValidPassword(password_input.getText())) {
            error.setText("Password must contain at least one uppercase letter, one number, and one special character");
            error.setVisible(true);
            return false;
        }

        // Validate phone number
        if (!phonenumber_input.getText().matches("\\d{8,}")) {
            error.setText("Phone number must have at least 8 digits");
            error.setVisible(true);
            return false;
        }

        return true;
    }

    // Password validation method
    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!/])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // Email validation method
    private boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}