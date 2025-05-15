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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.userService;
import services.Crole;
import javafx.scene.text.Text;
import org.mindrot.jbcrypt.BCrypt;

public class addUser {
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
    private ComboBox<Role> role_combobox;

    private ObservableList<Role> rolesList = FXCollections.observableArrayList();

    @FXML
    void reset_input(ActionEvent event) {
        lastname_input.clear();
        birthday_input.setValue(null);
        email_input.clear();
        firstname_input.clear();
        gender_combobox.setValue(null);
        gender_combobox1.setValue(null);
        role_combobox.setValue(null);
        password_input.clear();
        username_input.clear();
        picture_input.clear();
        phonenumber_input.clear();
        imagePane.getChildren().clear();
        error.setVisible(false);
    }

    @FXML
    void submit_user(ActionEvent event) throws IOException, SQLException {
        if (validateForm()) {
            String picturePath = picture_input.getText();
            Path path = Paths.get(picturePath);
            String fileName = path.getFileName().toString();

            LocalDate selectedDate = birthday_input.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedBirthday = selectedDate.format(formatter);

            String selectedGender = gender_combobox.getValue();
            Integer pmrLevel = gender_combobox1.getValue();
            Role selectedRole = role_combobox.getValue();
            int roleId = selectedRole.getRoleId();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Add New User");
            alert.setContentText("Are you sure you want to add this user with role: " + selectedRole.getRoleName() + "?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
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
                                pmrLevel != null ? pmrLevel : 0,
                                roleId
                        );

                        userService.addUser(newUser);

                        try {
                            Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Image copied successfully to: " + destinationPath);
                        } catch (IOException e) {
                            System.err.println("Error copying image: " + e.getMessage());
                            e.printStackTrace();
                        }

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();

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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    void initialize() {
        gender_combobox.setItems(FXCollections.observableArrayList("Male", "Female"));
        gender_combobox1.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
        loadRoles();
        picture_input.setVisible(false);
        error.setVisible(false);

        role_combobox.setCellFactory(param -> new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getRoleName());
            }
        });

        role_combobox.setButtonCell(new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Role" : item.getRoleName());
            }
        });
    }

    private void loadRoles() {
        try {
            List<Role> roles = roleService.afficherAll();
            rolesList.clear();
            rolesList.addAll(roles);
            role_combobox.setItems(rolesList);

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload your profile picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
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
                e.printStackTrace();
            }
        } else {
            System.out.println("No file selected");
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
                gender_combobox1.getValue() == null ||
                picture_input.getText().isEmpty() ||
                role_combobox.getValue() == null ||
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void goto_event(ActionEvent event) {}
}