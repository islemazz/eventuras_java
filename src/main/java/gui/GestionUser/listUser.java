package gui.GestionUser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import entities.Role;
import entities.user;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import services.Crole;
import services.userService;
import javafx.event.ActionEvent;

public class listUser {
    public Button events;
    public Button dash;
    public Button edit;
    public Button disc;

    @FXML private TextField searchbar_id;
    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private GridPane grid;
    @FXML private ScrollPane scroll;

    private final Crole crole = new Crole();
    private final userService userService = new userService();

    @FXML
    void initialize() {
        searchbar_id.textProperty().addListener((observable, oldValue, newValue) -> populateGrid(newValue.toLowerCase()));
        populateGrid("");
    }

    private void populateGrid(String searchText) {
        grid.getChildren().clear();
        List<user> users = userService.getallUserdata();

        int column = 0;
        int row = 1;
        boolean foundResults = false;

        for (user currentUser : users) {
            if (!searchText.isEmpty() &&
                    !currentUser.getFirstname().toLowerCase().contains(searchText) &&
                    !currentUser.getLastname().toLowerCase().contains(searchText) &&
                    !currentUser.getUsername().toLowerCase().contains(searchText) &&
                    !currentUser.getEmail().toLowerCase().contains(searchText)) {
                continue;
            }

            foundResults = true;
            String imagePath = currentUser.getPicture();

            if (imagePath != null && !imagePath.trim().isEmpty()) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    try {
                        Image userImage = new Image(imageFile.toURI().toString());
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/item.fxml"));
                        AnchorPane userCard = loader.load();

                        ItemController controller = loader.getController();
                        Role role = crole.getRoleById(currentUser.getId_role());

                        controller.setLocation(currentUser.getFirstname(), role.getRoleName(), userImage, currentUser);

                        GridPane.setMargin(userCard, new Insets(10));
                        grid.add(userCard, column++, row);
                        if (column == 3) {
                            column = 0;
                            row++;
                        }
                    } catch (IOException | SQLException e) {
                        System.err.println("Error loading user card: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Image file not found for user: " + currentUser.getUsername());
                }
            } else {
                System.out.println("No image path for user: " + currentUser.getUsername());
            }
        }

        if (!foundResults && !searchText.isEmpty()) {
            Label noResults = new Label("No users found matching: " + searchText);
            noResults.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
            grid.add(noResults, 0, 0, 3, 1);
        }

        grid.setPrefHeight((row + 1) * 250);
    }

    public void deleteUser(user u) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete user " + u.getUsername() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userService.deleteUser(u.getId());

                String imagePath = u.getPicture();
                if (imagePath != null && !imagePath.trim().isEmpty()) {
                    try {
                        Files.deleteIfExists(Paths.get(imagePath));
                        System.out.println("Deleted image for user: " + u.getUsername());
                    } catch (IOException e) {
                        System.err.println("Error deleting image: " + e.getMessage());
                    }
                }

                populateGrid(searchbar_id.getText().toLowerCase());
            }
        });
    }

    private void navigateTo(String path, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void add_user(ActionEvent e) throws IOException { navigateTo("/addUser.fxml", e); }
    public void display_charts(ActionEvent e) throws IOException { navigateTo("/Crudrole.fxml", e); }
    public void goto_dashboard(ActionEvent e) throws IOException { navigateTo("/adminDashboard.fxml", e); }
    public void goto_event(ActionEvent e) throws IOException { navigateTo("/AfficherEventBack.fxml", e); }
    public void goto_edit(ActionEvent e) throws IOException { navigateTo("/editCurrentuser.fxml", e); }
    public void disconnect(ActionEvent e) throws IOException { navigateTo("/login.fxml", e); }
    public void back_to_list(ActionEvent e) throws IOException { navigateTo("/adminDashboard.fxml", e); }
    public void goto_user(ActionEvent e) {}
}
