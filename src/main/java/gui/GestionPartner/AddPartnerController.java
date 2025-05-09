package gui.GestionPartner;

import entities.Partner;
import entities.PartnerType;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import services.PartnerService;

import java.sql.SQLException;

public class AddPartnerController {

    @FXML
    private TextField nameField;

    @FXML
    private ChoiceBox<PartnerType> typeField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField videoField;

    @FXML
    private Button btnSubmit;

    private Partner currentPartner;

    private final PartnerService partnerService = new PartnerService();

    @FXML
    public void initialize() {
        // Remplir le ChoiceBox avec les valeurs de PartnerType
        typeField.setItems(FXCollections.observableArrayList(PartnerType.values()));

        // Définir l'action du bouton
        btnSubmit.setOnAction(event -> addPartner());
    }

    @FXML
    private void addPartner() {
        try {
            // Récupérer les valeurs du formulaire
            String name = nameField.getText().trim();
            PartnerType type = typeField.getValue();
            String contactInfo = contactField.getText().trim();
            String videoPath = videoField.getText().trim();

            // Vérifier que tous les champs sont remplis
            if (name.isEmpty() || type == null || contactInfo.isEmpty() || videoPath.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Erreur de saisie", "Veuillez remplir tous les champs.");
                return;
            }

            // Créer un nouvel objet Partner
            Partner newPartner = new Partner(0, name, type, contactInfo, videoPath);

            // Enregistrer dans la base de données
            PartnerService pa = new PartnerService();
            partnerService.create(newPartner);

            // Afficher un message de succès
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire ajouté avec succès !");

            // Effacer les champs après l'ajout
            clearForm();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Erreur lors de l'enregistrement du partenaire : " + e.getMessage());
        }
    }

    public void setPartner(Partner partner) {
        this.currentPartner = partner;
        nameField.setText(partner.getName());
        typeField.setValue(partner.getType());
        contactField.setText(partner.getContactInfo());
        videoField.setText(partner.getImagePath());
    }

    private void clearForm() {
        nameField.clear();
        typeField.setValue(null);
        contactField.clear();
        videoField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showEvents(ActionEvent event) {
    }

    public void showAcceuil(ActionEvent event) {

    }
}
