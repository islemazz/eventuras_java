package gui.GestionUser;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.userService;
import services.Crole;
import javafx.scene.text.Text;

public class addUser {
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
    private ComboBox<Integer> gender_combobox1;
    @FXML
    private TextField firstname_input;
    @FXML
    private ComboBox<String> gender_combobox;
    @FXML
    private TextField lastname_input;
    @FXML
    private TextField password_input;
    @FXML
    private TextField username_input;
    @FXML
    private TextField picture_input;
    @FXML
    private TextField phonenumber_input;
    @FXML
    private Text error;
    @FXML
    private Pane imagePane;
    @FXML
    private ComboBox<Role> role_combobox; // Added role combobox

    private ObservableList<Role> rolesList = FXCollections.observableArrayList();

    @FXML
    void reset_input(ActionEvent event) {
        lastname_input.clear();
        birthday_input.setValue(null);
        email_input.clear();
        firstname_input.clear();
        gender_combobox.setValue(null);
        gender_combobox1.setValue(null);
        role_combobox.setValue(null); // Reset role selection
        password_input.clear();
        username_input.clear();
        picture_input.clear();
        phonenumber_input.clear();

        // Clear image display
        imagePane.getChildren().clear();

        // Hide error message
        error.setVisible(false);
    }

    @FXML
    void submit_user(ActionEvent event) throws IOException, SQLException {
        // Validate the form inputs
        if(validateForm()) {
            // Get file path and prepare new file name
            String picturePath = picture_input.getText();
            Path path = Paths.get(picturePath);
            String fileName = path.getFileName().toString();

            // Format the date
            LocalDate selectedDate = birthday_input.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedBirthday = selectedDate.format(formatter);

            // Get selected values from comboboxes
            String selectedGender = gender_combobox.getValue();
            Integer pmrLevel = gender_combobox1.getValue();

            // Get selected role
            Role selectedRole = role_combobox.getValue();
            int roleId = selectedRole.getRoleId();

            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Add New User");
            alert.setContentText("Are you sure you want to add this user with role: " +
                    selectedRole.getRoleName() + "?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Prepare the destination path for the image
                        Path destinationDir = Paths.get("src/main/resources/images/Profilepictures");

                        // Create the directory if it doesn't exist
                        if (!Files.exists(destinationDir)) {
                            Files.createDirectories(destinationDir);
                        }

                        Path destinationPath = destinationDir.resolve(fileName);

                        // Create new user object with role ID
                        user newUser = new user(
                                username_input.getText(),
                                email_input.getText(),
                                password_input.getText(),
                                firstname_input.getText(),
                                lastname_input.getText(),
                                formattedBirthday,
                                selectedGender,
                                destinationPath.toString(), // Store the full path
                                phonenumber_input.getText(),
                                roleId  // Pass the role ID to the constructor
                        );

                        // Set the role and level
                        newUser.setId_role(roleId);
                        newUser.setRole(selectedRole.getRoleName());
                        newUser.setLevel(pmrLevel != null ? pmrLevel : 0);

                        // Add the user to the database
                        userService.addUser(newUser);

                        // Copy the image file
                        try {
                            Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Image copied successfully to: " + destinationPath);
                        } catch (IOException e) {
                            System.err.println("Error copying image: " + e.getMessage());
                            e.printStackTrace();
                        }

                        // Close the current window and return to list
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();

                        // Navigate back to user list
                        navigateTo("/listUser.fxml", event);

                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user: " + e.getMessage());
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
    @FXML
    void initialize() {
        // Initialize gender options
        ObservableList<String> genderOptions = FXCollections.observableArrayList("Male", "Female");
        gender_combobox.setItems(genderOptions);

        // Initialize PMR level options
        ObservableList<Integer> pmrLevelOptions = FXCollections.observableArrayList(1, 2, 3, 4);
        gender_combobox1.setItems(pmrLevelOptions);

        // Load roles from database and populate combobox
        loadRoles();

        // Hide the picture input field and initialize error text
        picture_input.setVisible(false);
        error.setVisible(false);

        // Configure role combobox cell factory to display role names
        role_combobox.setCellFactory(param -> new javafx.scene.control.ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getRoleName());
                }
            }
        });

        // Configure role combobox button cell to display selected role name
        role_combobox.setButtonCell(new javafx.scene.control.ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Role");
                } else {
                    setText(item.getRoleName());
                }
            }
        });
    }

    /**
     * Loads all roles from the database and populates the role combobox
     */
    private void loadRoles() {
        try {
            // Fetch roles from database
            List<Role> roles = roleService.afficherAll();

            // Update observable list
            rolesList.clear();
            rolesList.addAll(roles);

            // Set items in combobox
            role_combobox.setItems(rolesList);

            // Pre-select "user" role if it exists
            for (Role role : rolesList) {
                if (role.getRoleName().equalsIgnoreCase("user")) {
                    role_combobox.setValue(role);
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading roles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void back_to_list(ActionEvent event) throws IOException {
        navigateTo("/listUser.fxml", event);
    }

    @FXML
    void upload_img(ActionEvent actionEvent) {
        // Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload your profile picture");

        // Set file extension filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Get the stage from the event source
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

        // Show dialog and get selected file
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Store the file path
            picture_input.setText(selectedFile.getAbsolutePath());

            // Load and display the image
            try {
                // Create image from file
                Image image = new Image(selectedFile.toURI().toString());

                // Create image view
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);

                // Clear existing content in imagePane
                imagePane.getChildren().clear();

                // Add the image view to the pane
                imagePane.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected");
        }
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
                gender_combobox1.getValue() == null ||
                picture_input.getText().isEmpty() ||
                role_combobox.getValue() == null ||  // Validate role selection
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
        String phone = phonenumber_input.getText();
        if (!phone.matches("\\d{8,}")) {
            error.setText("Phone number must have at least 8 digits");
            error.setVisible(true);
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!/])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void goto_event(ActionEvent event) {
    }
}