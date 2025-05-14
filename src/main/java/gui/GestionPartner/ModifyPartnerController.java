package gui.GestionPartner;

import entities.Partner;
import entities.PartnerType;
import gui.GestionEvents.AfficherEventHOME;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.PartnerService;

import java.io.IOException;
import java.sql.SQLException;

public class ModifyPartnerController {

    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    @FXML
    private TextField nameField;

    @FXML
    private ChoiceBox<PartnerType> typeField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField videoField;

    @FXML
    private Button btnUpdate;

    private Partner currentPartner;
    private final PartnerService partnerService = new PartnerService();

    @FXML
    public void initialize() {
        typeField.setItems(FXCollections.observableArrayList(PartnerType.values()));

        btnUpdate.setOnAction(event -> updatePartner());
    }

    public void setPartner(Partner partner) {
        this.currentPartner = partner;
        nameField.setText(partner.getName());
        typeField.setValue(partner.getType());
        contactField.setText(partner.getContactInfo());
        videoField.setText(partner.getImagePath());
    }

    @FXML
    private void updatePartner() {
        try {
            String name = nameField.getText().trim();
            PartnerType type = typeField.getValue();
            String contactInfo = contactField.getText().trim();
            String imagePath = videoField.getText().trim(); // Change variable name to imagePath

            if (name.isEmpty() || type == null || contactInfo.isEmpty() || imagePath.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all fields.");
                return;
            }

            currentPartner.setName(name);
            currentPartner.setType(type);
            currentPartner.setContactInfo(contactInfo);
            currentPartner.setImagePath(imagePath); // Set imagePath instead of videoPath

            partnerService.update(currentPartner);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Partner updated successfully!");

            Stage stage = (Stage) btnUpdate.getScene().getWindow();
            stage.close(); // Close the modification window

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while updating partner: " + e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    Scene scene;
    Stage stage;

    public void showEvents(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents(); // Call the method to display all events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) GoToEvents.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }

    //display last 3 events in the home section
    public void showAcceuil(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) Acceuil.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);

    }

    public void goToCollabs(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipPartner.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) Collaborations.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }

    public void goToTickets(ActionEvent event) throws IOException {

    }

    public void goToReclams(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AfficherReclamations.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) reclam.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }
}
