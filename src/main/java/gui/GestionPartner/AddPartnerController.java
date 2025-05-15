package gui.GestionPartner;

import java.sql.SQLException;
import java.time.LocalDateTime;

import entities.Partner;
import entities.PartnerType;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.PartnerService;

public class AddPartnerController {

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
    private Button btnSubmit;

    @FXML
    private Button cancelButton;

    @FXML
    private Button btnReturnToDashboard;

    private Partner currentPartner;
    private final PartnerService partnerService = new PartnerService();

    @FXML
    public void initialize() {
        // Remplir le ChoiceBox avec les valeurs de PartnerType
        typeField.setItems(FXCollections.observableArrayList(PartnerType.values()));

        // Initialize spinners
        ratingSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 5, 0, 0.1));
        ratingCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));

        // Définir l'action du bouton
        btnSubmit.setOnAction(event -> addPartner());
        btnReturnToDashboard.setOnAction(event -> showAcceuil(event));
    }

    @FXML
    private void addPartner() {
        if (validateFields()) {
            try {
                Partner newPartner = new Partner();
                newPartner.setName(nameField.getText());
                newPartner.setType(typeField.getValue());
                newPartner.setDescription(descriptionField.getText());
                newPartner.setEmail(emailField.getText());
                newPartner.setPhone(phoneField.getText());
                newPartner.setAddress(addressField.getText());
                newPartner.setWebsite(websiteField.getText());
                newPartner.setRating(ratingSpinner.getValue());
                newPartner.setRatingCount(ratingCountSpinner.getValue());
                newPartner.setCreatedAt(LocalDateTime.now());
                newPartner.setUpdatedAt(LocalDateTime.now());

                partnerService.create(newPartner);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire ajouté avec succès !");
                closeWindow();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de base de données", 
                    "Erreur lors de l'ajout du partenaire : " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur inattendue s'est produite : " + e.getMessage());
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

    public void setPartner(Partner partner) {
        this.currentPartner = partner;
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

    public void showEvents(ActionEvent event) {
    }

    public void showAcceuil(ActionEvent event) {
    }
}
