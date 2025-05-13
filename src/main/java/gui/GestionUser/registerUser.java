package gui.GestionUser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
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
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import services.userService;
import services.Crole;
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
    private Pane imagePane;
    private boolean imageChanged = false;
    private boolean isInitialized = false;
    private final userService userService = new userService();
    private final Crole roleService = new Crole();
    private final ObservableList<Role> rolesList = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        gender_input.setItems(FXCollections.observableArrayList("Male", "Female"));
        level_PMR_input.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
        picture_input.setVisible(false);
        error.setVisible(false);
        loadRoles();

        role_input.setCellFactory(param -> new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getRoleName());
            }
        });

        role_input.setButtonCell(new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Role" : item.getRoleName());
            }
        });
    }


    private void loadRoles() {
        try {
            List<Role> allRoles = roleService.afficherAll();

            // Ne garder que les rôles différents de "Role_ADMIN"
            List<Role> filteredRoles = allRoles.stream()
                    .filter(role -> !role.getRoleName().equalsIgnoreCase("Role_ADMIN"))
                    .toList();

            ObservableList<Role> roleList = FXCollections.observableArrayList(filteredRoles);
            role_input.setItems(roleList);

            // Cell display for dropdown list
            role_input.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getRoleName());
                }
            });

            // Display selected item in ComboBox button
            role_input.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Select Role" : item.getRoleName());
                }
            });

        } catch (Exception e) {
            System.err.println("Error loading roles: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void reset_inputs(ActionEvent event) {
        lastname_input.clear();
        birthday_input.setValue(null);
        email_input.clear();
        firstname_input.clear();
        gender_input.setValue(null);
        password_input.clear();
        passwordconfirmation_input.clear();
        username_input.clear();
        picture_input.clear();
        phonenumber_input.clear();
        level_PMR_input.setValue(null);
        role_input.setValue(null);
        imagePane.getChildren().clear();
        error.setVisible(false);
    }

    @FXML
    public void user_Submit(ActionEvent event) {
        if (!validateForm()) return;

        String picturePath = picture_input.getText();
        Path path = Paths.get(picturePath);
        String fileName = path.getFileName().toString();
        LocalDate selectedDate = birthday_input.getValue();
        String formattedBirthday = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String selectedGender = gender_input.getValue();
        int selectedLevel = level_PMR_input.getValue();
        Role selectedRole = role_input.getValue();
        int roleId = selectedRole.getRoleId();

        try {
            Path destinationDir = Paths.get("src/main/resources/images/Profilepictures");
            if (!Files.exists(destinationDir)) {
                Files.createDirectories(destinationDir);
            }
            Path destinationPath = destinationDir.resolve(fileName);

            user newUser = new user(
                    username_input.getText(),
                    email_input.getText(),
                    BCrypt.hashpw(password_input.getText(), BCrypt.gensalt()),
                    firstname_input.getText(),
                    lastname_input.getText(),
                    formattedBirthday,
                    selectedGender,
                    destinationPath.toString(),
                    phonenumber_input.getText(),
                    selectedLevel,
                    roleId
            );

            userService.addUser(newUser);
            Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            Alert confirmationAlert = new Alert(Alert.AlertType.INFORMATION);
            confirmationAlert.setTitle("Success");
            confirmationAlert.setContentText("User registered successfully.");
            confirmationAlert.showAndWait();

            navigateTo("/login.fxml", event);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void upload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            picture_input.setText(selectedFile.getAbsolutePath());
            try {
                Image image = new Image(selectedFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                imagePane.getChildren().clear();
                imagePane.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        error.setVisible(false);

        if (username_input.getText().isEmpty() ||
                email_input.getText().isEmpty() ||
                password_input.getText().isEmpty() ||
                passwordconfirmation_input.getText().isEmpty() ||
                firstname_input.getText().isEmpty() ||
                lastname_input.getText().isEmpty() ||
                birthday_input.getValue() == null ||
                gender_input.getValue() == null ||
                level_PMR_input.getValue() == null ||
                picture_input.getText().isEmpty() ||
                role_input.getValue() == null ||
                phonenumber_input.getText().isEmpty()) {
            error.setText("All fields must be filled");
            error.setVisible(true);
            return false;
        }

        if (!isEmailValid(email_input.getText())) {
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
            error.setText("Passwords do not match");
            error.setVisible(true);
            return false;
        }

        if (!phonenumber_input.getText().matches("\\d{8}")) {
            error.setText("Phone number must be exactly 8 digits");
            error.setVisible(true);
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!/])(?=\\S+$).{8,}$");
    }

    private boolean isEmailValid(String email) {
        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void opened(ActionEvent event) {}

    public void back_to_login(ActionEvent actionEvent) {}

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
}