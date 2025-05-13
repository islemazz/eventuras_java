package gui.GestionUser;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.Crole;
import services.userService;

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

public class updateUser {
    userService userService = new userService();
    Crole roleService = new Crole();

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
    @FXML
    private TextField picture_input;
    @FXML
    private TextField username_input;
    @FXML
    private Text error;
    @FXML
    private TextField phonenumber_input;
    @FXML
    private ComboBox<String> role_input;

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

            try {
                LocalDate date = LocalDate.parse(user.getBirthday());
                birthday_input.setValue(date);
            } catch (Exception e) {
                System.err.println("Error parsing birthday: " + e.getMessage());
            }

            try {
                String userRole = getRoleNameById(user.getId_role());
                if (userRole != null && !userRole.isEmpty()) {
                    role_input.setValue(userRole);
                }
            } catch (Exception e) {
                System.err.println("Error setting role: " + e.getMessage());
            }

            if (picture_input.getText() != null && !picture_input.getText().isEmpty()) {
                displayImage(picture_input.getText());
            } else {
                imagePane.getChildren().clear();
            }
        }
    }

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
        setUser(user);
        error.setVisible(false);
        imageChanged = false;
    }

    @FXML
    void submit_user(ActionEvent event) throws IOException {
        if (validateForm()) {
            String picturePath = picture_input.getText();
            LocalDate selectedDate = birthday_input.getValue();
            String formattedBirthday = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String selectedGender = gender_combobox.getValue();

            String selectedRoleName = role_input.getValue();
            int roleId;
            try {
                roleId = roleService.getRoleIdByName(selectedRoleName);
                if (roleId == -1) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Selected role does not exist in the database");
                    return;
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error retrieving role ID: " + e.getMessage());
                return;
            }

            user updatedUser = new user(
                    user.getId(),
                    username_input.getText(),
                    email_input.getText(),
                    user.getPassword(),
                    firstname_input.getText(),
                    lastname_input.getText(),
                    formattedBirthday,
                    selectedGender,
                    picturePath,
                    phonenumber_input.getText(),
                    user.getLevel(),
                    roleId
            );

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Update User");
            alert.setContentText("Are you sure you want to update this user: " + updatedUser.getUsername());

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        if (imageChanged) {
                            File sourceFile = new File(picturePath);
                            Path destinationDir = Paths.get("src/main/resources/images/Profilepictures");
                            if (!Files.exists(destinationDir)) {
                                Files.createDirectories(destinationDir);
                            }
                            String fileName = sourceFile.getName();
                            Path destinationPath = destinationDir.resolve(fileName);
                            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            updatedUser.setPicture(destinationPath.toString());
                        }
                        userService.updateUser(updatedUser, updatedUser.getId());
                        showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();
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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void displayImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imagePane.getChildren().clear();
                imagePane.getChildren().add(imageView);
            } else {
                imagePane.getChildren().clear();
            }
        } catch (Exception e) {
            System.err.println("Error displaying image: " + e.getMessage());
        }
    }

    @FXML
    void initialize() {
        gender_combobox.setItems(FXCollections.observableArrayList("Male", "Female"));
        picture_input.setVisible(false);
        error.setVisible(false);
        loadRoles();
    }

    private void loadRoles() {
        try {
            List<Role> roles = roleService.afficherAll();
            ObservableList<String> roleNames = FXCollections.observableArrayList();
            for (Role role : roles) {
                roleNames.add(role.getRoleName());
            }
            role_input.setItems(roleNames);
        } catch (Exception e) {
            System.err.println("Error loading roles: " + e.getMessage());
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
            picture_input.setText(selectedFile.getAbsolutePath());
            imageChanged = true;
            try {
                Image image = new Image(selectedFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
                imagePane.getChildren().clear();
                imagePane.getChildren().add(imageView);
            } catch (Exception e) {
                System.err.println("Error loading selected image: " + e.getMessage());
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
        if (!isEmailValid(email_input.getText())) {
            error.setText("Invalid email address");
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