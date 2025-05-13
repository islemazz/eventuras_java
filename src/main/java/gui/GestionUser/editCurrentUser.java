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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class editCurrentUser {

    @FXML private DatePicker birthday_input;
    @FXML private TextField email_input;
    @FXML private Text error;
    @FXML private TextField firstname_input;
    @FXML private ComboBox<String> gender_combobox;
    @FXML private TextField lastname_input;
    @FXML private TextField password_input;
    @FXML private TextField phonenumber_input;
    @FXML private TextField picture_input;
    @FXML private TextField username_input;
    @FXML private Pane imagePane;

    private boolean imageChanged = false;
    private String originalImagePath;
    private final userService userService = new userService();
    private final Crole croleService = new Crole();
    private final UserSession userSession = UserSession.getInstance();

    @FXML
    void initialize() {
        gender_combobox.setItems(FXCollections.observableArrayList("Male", "Female"));
        picture_input.setVisible(false);
        error.setVisible(false);
        originalImagePath = userSession.getPicture();
        resetForm();
    }

    private void resetForm() {
        firstname_input.setText(userSession.getFirstname());
        lastname_input.setText(userSession.getLastname());
        phonenumber_input.setText(userSession.getPhonenumber());
        username_input.setText(userSession.getUsername());
        email_input.setText(userSession.getEmail());
        picture_input.setText(userSession.getPicture());
        gender_combobox.setValue(userSession.getGender());
        try {
            birthday_input.setValue(LocalDate.parse(userSession.getBirthday()));
        } catch (Exception e) {
            System.err.println("Error parsing birthday: " + e.getMessage());
        }
        displayProfileImage(userSession.getPicture());
    }

    @FXML
    void reset_input(ActionEvent event) {
        resetForm();
        error.setVisible(false);
        imageChanged = false;
    }

    @FXML
    void submit_user(ActionEvent event) {
        if (!validateForm()) return;

        String picturePath = picture_input.getText();
        String formattedBirthday = birthday_input.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String selectedGender = gender_combobox.getValue();
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
                phonenumber_input.getText(),
                userSession.getLevel(),     // par exemple 0 si tu ne le changes pas
                userSession.getRole()       // ou le rôle ID à jour
        );

        updatedUser.setId_role(userSession.getRole());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Update Profile");
        alert.setContentText("Are you sure you want to update your profile?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (imageChanged) {
                    File sourceFile = new File(picturePath);
                    Path destinationDir = Paths.get("src/main/resources/images/Profilepictures");
                    if (!Files.exists(destinationDir)) Files.createDirectories(destinationDir);
                    Path destinationPath = destinationDir.resolve(sourceFile.getName());
                    Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    updatedUser.setPicture(destinationPath.toString());
                }
                userService.updateUser(updatedUser, updatedUser.getId());
                updateUserSession(updatedUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Your profile has been updated successfully!");

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();

                if (userSession.getRole() == 1) {
                    navigateTo("/listUser.fxml", event);
                } else {
                    navigateTo("/adminDashboard.fxml", event);
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            picture_input.setText(selectedFile.getAbsolutePath());
            imageChanged = true;
            try {
                Image image = new Image(selectedFile.toURI().toString());
                displayImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void displayImage(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imagePane.getChildren().clear();
        imagePane.getChildren().add(imageView);
    }

    private void displayProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    displayImage(image);
                } else {
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

    private boolean validateForm() {
        error.setVisible(false);
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
        if (!phonenumber_input.getText().matches("\\d{8,}")) {
            error.setText("Phone number must have at least 8 digits");
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void back_to_list(ActionEvent actionEvent) {
    }

    public void goto_shop(ActionEvent actionEvent) {
    }

    public void goto_blog(ActionEvent actionEvent) {
    }

    public void goto_dashboard(ActionEvent actionEvent) {
    }

    public void goto_edit(ActionEvent actionEvent) {
    }

    public void disconnect(ActionEvent actionEvent) {
    }

    public void goto_forum(ActionEvent actionEvent) {
    }

    public void goto_event(ActionEvent actionEvent) {
    }

    public void goto_user(ActionEvent actionEvent) {

    }

    @FXML
    void goto_dashboard(ActionEvent event) throws IOException {
        navigateTo("/adminDashboard.fxml", event);
    }

    @FXML
    void goto_user(ActionEvent event) throws IOException {
        navigateTo("/listUser.fxml", event);
    }

    @FXML
    void goto_event(ActionEvent event) throws IOException {
        navigateTo("/listColis.fxml", event);
    }

    @FXML
    void goto_forum(ActionEvent event) throws IOException {
        navigateTo("/listReponses.fxml", event);
    }

    @FXML
    void goto_shop(ActionEvent event) throws IOException {
        navigateTo("/listFactures.fxml", event);
    }

    @FXML
    void goto_blog(ActionEvent event) throws IOException {
        navigateTo("/listTrajets.fxml", event);
    }

    @FXML
    void goto_edit(ActionEvent event) throws IOException {
        navigateTo("/editCurrentuser.fxml", event);
    }

    @FXML
    void disconnect(ActionEvent event) throws IOException {
        // Clear user session
        UserSession.getInstance().cleanUserSession();
        // Navigate to login page
        navigateTo("/login.fxml", event);
    }
}