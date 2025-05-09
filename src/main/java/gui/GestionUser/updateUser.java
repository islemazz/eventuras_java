package gui.GestionUser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.userService;
import services.Crole;
import javafx.scene.image.ImageView;

public class updateUser {
    userService userService = new userService();
    Crole roleService = new Crole(); // Add RoleService

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private DatePicker birthday_input;
    @FXML
    private TextField email_input;
    @FXML
    private Pane imagePane;
    @FXML
    private TextField firstname_input;
    @FXML
    private ComboBox<String> gender_combobox;
    @FXML
    private TextField lastname_input;
    // Remove password_input field declaration
    @FXML
    private TextField picture_input;
    @FXML
    private TextField username_input;
    @FXML
    private Text error;
    @FXML
    private TextField phonenumber_input;
    @FXML
    private ComboBox<String> role_input; // Changed to String type

    private user user;
    private boolean imageChanged = false;
    private String originalImagePath;

    public void setUser(user user) {
        this.user = user;
        if (user != null) {
            email_input.setText(user.getEmail());
            firstname_input.setText(user.getFirstname());
            lastname_input.setText(user.getLastname());
            picture_input.setText(user.getPicture());
            username_input.setText(user.getUsername());
            gender_combobox.setValue(user.getGender());
            phonenumber_input.setText(user.getPhonenumber());
            originalImagePath = user.getPicture();

            // Set birthday
            String birthdayString = user.getBirthday();
            try {
                LocalDate date = LocalDate.parse(birthdayString);
                birthday_input.setValue(date);
            } catch (Exception e) {
                System.err.println("Error parsing birthday: " + e.getMessage());
            }

            // Set role
            try {
                String userRole = getRoleNameById(user.getId_role());
                if (userRole != null && !userRole.isEmpty()) {
                    role_input.setValue(userRole);
                    System.out.println("Set user role to: " + userRole);
                } else {
                    System.err.println("Could not find role name for ID: " + user.getId_role());
                }
            } catch (Exception e) {
                System.err.println("Error setting role: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Loading user: " + user);
            System.out.println("Image path: " + picture_input.getText());

            if (picture_input.getText() != null && !picture_input.getText().isEmpty()) {
                displayImage(picture_input.getText());
            } else {
                System.out.println("Image path is null or empty");
                imagePane.getChildren().clear();
            }
        } else {
            System.out.println("User is null!");
        }
    }

    /**
     * Get role name by ID
     */
    private String getRoleNameById(int roleId) {
        try {
            List<Role> roles = roleService.afficherAll();
            for (Role role : roles) {
                if (role.getRoleId() == roleId) {
                    return role.getRoleName();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting role name: " + e.getMessage());
        }
        return null;
    }

    @FXML
    void back_to_list(ActionEvent event) throws IOException {
        navigateTo("/listUser.fxml", event);
    }

    @FXML
    void reset_input(ActionEvent event) {
        // Reset to original values
        setUser(user);
        error.setVisible(false);
        imageChanged = false;
    }

    @FXML
    void submit_user(ActionEvent event) throws IOException {
        if(validateForm()) {
            String picturePath = picture_input.getText();

            LocalDate selectedDate = birthday_input.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedBirthday = selectedDate.format(formatter);
            String selectedGender = gender_combobox.getValue();

            // Get selected role ID
            String selectedRoleName = role_input.getValue();
            int roleId;
            try {
                roleId = roleService.getRoleIdByName(selectedRoleName);
                if (roleId == -1) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Selected role does not exist in the database");
                    return;
                }
                System.out.println("Selected role ID: " + roleId + " for role name: " + selectedRoleName);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error retrieving role ID: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Create updated user object - keep original password
            user updatedUser = new user(
                    user.getId(),
                    username_input.getText(),
                    email_input.getText(),
                    user.getPassword(), // Keep original password
                    firstname_input.getText(),
                    lastname_input.getText(),
                    formattedBirthday,
                    selectedGender,
                    picturePath,
                    phonenumber_input.getText()
            );

            // Set role ID
            updatedUser.setId_role(roleId);
            updatedUser.setRole(selectedRoleName);

            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Update User");
            alert.setContentText("Are you sure you want to update this user: " + updatedUser.getUsername() +
                    "\nRole: " + selectedRoleName + " (ID: " + roleId + ")?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
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

                        // Debug info before update
                        System.out.println("Updating user with ID: " + updatedUser.getId());
                        System.out.println("Role ID: " + updatedUser.getId_role());
                        System.out.println("Role Name: " + updatedUser.getRole());

                        // Update the user in the database
                        userService.updateUser2(updatedUser, updatedUser.getId());

                        // Show success message
                        showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");

                        // Close current window and go back to list
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();

                        // Load the list user view
                        navigateTo("/listUser.fxml", event);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
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

    /**
     * Display an image from path
     */
    private void displayImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                String imageUrl = imageFile.toURI().toURL().toString();
                Image image = new Image(imageUrl);

                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);

                imagePane.getChildren().clear();
                imagePane.getChildren().add(imageView);
            } else {
                System.err.println("Image file does not exist: " + imagePath);
                imagePane.getChildren().clear();
            }
        } catch (Exception e) {
            System.err.println("Error displaying image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        ObservableList<String> genderOptions = FXCollections.observableArrayList("Male", "Female");
        gender_combobox.setItems(genderOptions);
        picture_input.setVisible(false);
        error.setVisible(false);

        // Load roles for the combobox
        loadRoles();
    }

    /**
     * Load all roles from database
     */
    private void loadRoles() {
        try {
            List<Role> roles = roleService.afficherAll();

            ObservableList<String> roleNames = FXCollections.observableArrayList();
            for (Role role : roles) {
                roleNames.add(role.getRoleName());
            }

            role_input.setItems(roleNames);

            // Debug info
            System.out.println("Loaded " + roles.size() + " roles");
            for (Role role : roles) {
                System.out.println("Role ID: " + role.getRoleId() + ", Name: " + role.getRoleName());
            }
        } catch (Exception e) {
            System.err.println("Error loading roles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void upload(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Set the image path to the imageTF text field
            picture_input.setText(selectedFile.getAbsolutePath());
            imageChanged = true;

            // Load the selected image into the imagePane
            try {
                Image image = new Image(selectedFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                imagePane.getChildren().clear(); // Clear existing content
                imagePane.getChildren().add(imageView); // Add the image to the pane
            } catch (Exception e) {
                System.err.println("Error loading selected image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        if (username_input.getText().isEmpty() ||
                email_input.getText().isEmpty() ||
                firstname_input.getText().isEmpty() ||
                lastname_input.getText().isEmpty() ||
                birthday_input.getValue() == null ||
                gender_combobox.getValue() == null ||
                picture_input.getText().isEmpty() ||
                role_input.getValue() == null ||
                phonenumber_input.getText().isEmpty()) {

            error.setText("All fields must be filled");
            error.setVisible(true);
            return false;
        }

        String email = email_input.getText();
        if (!isEmailValid(email)) {
            error.setText("Invalid email address");
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

    //Email validation method
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