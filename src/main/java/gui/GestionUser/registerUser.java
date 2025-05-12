package gui.GestionUser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import entities.Role;
import entities.user;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.Crole;
import services.userService;
import utils.MyConnection;

public class registerUser {

    @FXML
    public Text error;
    @FXML
    public ComboBox<Role> role_input;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private DatePicker birthday_input;
    @FXML
    private TextField email_input;
    @FXML
    private TextField firstname_input;
    @FXML
    private ComboBox<String> gender_input;
    @FXML
    private ComboBox<Integer> level_PMR_input;
    @FXML
    private TextField lastname_input;
    @FXML
    private PasswordField password_input;
    @FXML
    private PasswordField passwordconfirmation_input;
    @FXML
    private TextField picture_input;
    @FXML
    private TextField username_input;
    @FXML
    private TextField phonenumber_input;
    @FXML
    private Pane imagePane; // Changed from ImageView to Pane
    private boolean imageChanged = false;
    @FXML
    private ComboBox<Role> role_combobox; // Added role combobox
    private boolean isInitialized = false;
    private final userService userService = new userService();
    private final Crole croleService = new Crole();

    private ObservableList<Role> rolesList = FXCollections.observableArrayList();


    private Connection cnx;
    public registerUser() {
        cnx=MyConnection.getInstance().getConnection();
    }

    @FXML
    void initialize() {
        // Set up gender options
        ObservableList<String> genderOptions = FXCollections.observableArrayList("Male", "Female");
        gender_input.setItems(genderOptions);

        // Set up PMR level options
        ObservableList<Integer> levels = FXCollections.observableArrayList(1, 2, 3, 4);
        level_PMR_input.setItems(levels);

        // Hide error message initially
        error.setVisible(false);

        // Hide picture input (if needed)
        picture_input.setVisible(false);

        // Initialize the ComboBox with roles
        refreshRoleComboBox();
    }

    // Refresh the roles ComboBox
    private void refreshRoleComboBox() {
        try {
            // Fetch roles from the database
            List<Role> roles = croleService.afficherAll();
            rolesList.clear();
            
            // Add roles to the list, except for "admin"
            for (Role role : roles) {
                if (!role.getRoleName().equalsIgnoreCase("admin")) {
                    rolesList.add(role);
                }
            }

            // Set the items in the ComboBox
            role_input.setItems(rolesList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void back_to_login(ActionEvent event) throws IOException {
        navigateTo("/login.fxml", event);
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
    void reset_inputs(ActionEvent event) {
        lastname_input.clear();
        birthday_input.setValue(null);
        email_input.clear();
        firstname_input.clear();
        gender_input.setValue(null);
        password_input.clear();
        username_input.clear();
        picture_input.clear();
        phonenumber_input.clear();
    }

    @FXML
    public void user_Submit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        // Validate form inputs
        if (!validateForm()) {
            alert.setTitle("Erreur");
            alert.setContentText("Veuillez remplir tous les champs");
            alert.showAndWait();
            return;
        }

        // Get the selected role
        Role selectedRole = role_input.getValue();
        if (selectedRole == null) {
            alert.setTitle("Error");
            alert.setContentText("Please select a role.");
            alert.showAndWait();
            return;
        }
        String roleName = selectedRole.getRoleName();
        int roleId = selectedRole.getRoleId();
        System.out.println("Selected role: " + roleName);
        System.out.println("Retrieved role ID: " + roleId);

        // Retrieve and validate picture path
        String picturePath = picture_input.getText();
        if (picturePath.isEmpty()) {
            alert.setTitle("Erreur");
            alert.setContentText("Veuillez sélectionnez une photo de profil");
            alert.showAndWait();
            return;
        }

        // Retrieve and validate birthday
        LocalDate selectedDate = birthday_input.getValue();
        if (selectedDate == null) {
            alert.setTitle("Erreur");
            alert.setContentText("Veuillez sélectionnez un date");
            alert.showAndWait();
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedBirthday = selectedDate.format(formatter);

        // Retrieve other inputs
        String selectedGender = gender_input.getValue();
        int selectedLevel = level_PMR_input.getValue();

        // Create the user object
        user user = new user(
                roleId,
                username_input.getText(),
                email_input.getText(),
                password_input.getText(),
                firstname_input.getText(),
                lastname_input.getText(),
                formattedBirthday,
                selectedGender,
                picturePath,
                phonenumber_input.getText(),
                selectedLevel,
                roleName
        );

        System.out.println("User object: " + user);

        // Add the user to the database
        try {
            userService.addUser(user);
        } catch (SQLException e) {
            alert.setTitle("Error");
            alert.setContentText("Error adding user: " + e.getMessage());
            alert.showAndWait();
            return;
        } catch (IOException e) {
            alert.setTitle("Error");
            alert.setContentText("Error handling files: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        // Show confirmation message
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setContentText("User added successfully. Please log in.");
        confirmationAlert.show();
        try {
            navigateTo("/login.fxml", event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * Loads all roles from the database and populates the role combobox
     */
    private void loadRoles() {
        try {
            // Fetch roles from database
            List<Role> roles = croleService.afficherAll();

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

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Profile picture upload method
    public void upload_pfp(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload your profile picture");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String fileName = selectedFile.getName().toLowerCase();
            if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                picture_input.setText(selectedFile.getPath());
            } else {
                System.out.println("Invalid file format. Please select a PNG or JPG file.");
            }
        } else {
            System.out.println("No file selected");
        }
    }

    // Validation form for the register page
    private boolean validateForm() {
        if (username_input.getText().isEmpty() || email_input.getText().isEmpty() || password_input.getText().isEmpty()
                || firstname_input.getText().isEmpty() || lastname_input.getText().isEmpty() ||
                birthday_input.getValue() == null || gender_input.getValue() == null || picture_input.getText().isEmpty() || role_input.getValue() == null) {
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

        if (!isValidPassword(password_input.getText())) {
            error.setText("Password must contain at least one uppercase letter, one number, and one special character");
            error.setVisible(true);
            return false;
        }

        if (!password_input.getText().equals(passwordconfirmation_input.getText())) {
            error.setText("Your password is not the same as the confirmation");
            error.setVisible(true);
            return false;
        }
        // Validate birthday (must be at least 18 years old and not in the future)
        LocalDate birthday = birthday_input.getValue();
        LocalDate today = LocalDate.now();
        if (birthday.isAfter(today)) {
            error.setText("Birthday cannot be in the future");
            error.setVisible(true);
            return false;
        }

        if (Period.between(birthday, today).getYears() < 18) {
            error.setText("You must be at least 18 years old");
            error.setVisible(true);
            return false;
        }

        // Validate phone number (must be exactly 8 digits)
        String phone = phonenumber_input.getText();
        if (!phone.matches("\\d{8}")) {
            error.setText("Phone number must be exactly 8 digits");
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


    // Method to generate user password
    public static String generatePassword() {
        String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String specialCharacters = "@$!%*?&";
        String allCharacters = uppercaseLetters + digits + specialCharacters;
        int passwordLength = 12;
        StringBuilder password = new StringBuilder(passwordLength);
        SecureRandom random = new SecureRandom();
        password.append(uppercaseLetters.charAt(random.nextInt(uppercaseLetters.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));
        for (int i = 3; i < passwordLength; i++) {
            password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
        }
        return password.toString();
    }


    public void opened(ActionEvent event) {
    }
}