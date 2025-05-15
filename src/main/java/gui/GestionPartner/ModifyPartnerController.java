package gui.GestionPartner;

import java.io.IOException;

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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.PartnerService;

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
    private TextArea descriptionField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextArea addressField;

    @FXML
    private TextField websiteField;

    @FXML
    private Spinner<Double> ratingSpinner;

    @FXML
    private Spinner<Integer> ratingCountSpinner;

    @FXML
    private Button btnUpdate;

    @FXML
    private Button cancelButton;

    private Partner currentPartner;
    private final PartnerService partnerService = new PartnerService();

    @FXML
    public void initialize() {
        typeField.setItems(FXCollections.observableArrayList(PartnerType.values()));

        // Initialize spinners
        ratingSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 5, 0, 0.1));
        ratingCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));

        btnUpdate.setOnAction(event -> updatePartner());
    }

    public void setPartner(Partner partner) {
        this.currentPartner = partner;
        populateFields(partner);
    }

    private void populateFields(Partner partner) {
        nameField.setText(partner.getName());
        typeField.setValue(partner.getType());
        descriptionField.setText(partner.getDescription());
        emailField.setText(partner.getEmail());
        phoneField.setText(partner.getPhone());
        addressField.setText(partner.getAddress());
        websiteField.setText(partner.getWebsite());
        ratingSpinner.getValueFactory().setValue(partner.getRating());
        ratingCountSpinner.getValueFactory().setValue(partner.getRatingCount());
    }

    @FXML
    private void updatePartner() {
        if (validateFields()) {
            try {
                currentPartner.setName(nameField.getText());
                currentPartner.setType(typeField.getValue());
                currentPartner.setDescription(descriptionField.getText());
                currentPartner.setEmail(emailField.getText());
                currentPartner.setPhone(phoneField.getText());
                currentPartner.setAddress(addressField.getText());
                currentPartner.setWebsite(websiteField.getText());
                currentPartner.setRating(ratingSpinner.getValue());
                currentPartner.setRatingCount(ratingCountSpinner.getValue());

                partnerService.update(currentPartner);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire modifié avec succès !");
                closeWindow();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du partenaire : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateFields() {
        if (nameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Le nom du partenaire est requis.");
            return false;
        }
        if (typeField.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Le type du partenaire est requis.");
            return false;
        }
        if (emailField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "L'email est requis.");
            return false;
        }
        if (phoneField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Le numéro de téléphone est requis.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamations.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) reclam.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }
}
