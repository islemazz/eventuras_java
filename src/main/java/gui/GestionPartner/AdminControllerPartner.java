package gui.GestionPartner;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import entities.Partner;
import entities.PartnerType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.PartnerService;

public class AdminControllerPartner {

    private final PartnerService ps = new PartnerService();
    
    @FXML private ListView<Partner> partnersList;
    @FXML private TextField nameField;
    @FXML private ComboBox<PartnerType> typeComboBox;
    @FXML private TextArea descriptionField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField websiteField;
    @FXML private Spinner<Double> ratingSpinner;
    @FXML private Spinner<Integer> ratingCountSpinner;
    @FXML private ImageView partnerImage;
    @FXML private ImageView partnerVideo;
    @FXML private Button Ajouter;
    @FXML private Button Modifier;
    @FXML private Button Supprimer;
    @FXML private Button Actualiser;
    @FXML private Button browseImage;
    @FXML private Button browseVideo;

    private Partner selectedPartner;
    private String imagePath;
    private String videoPath;

    @FXML
    void initialize() {
        try {
            // Initialize partner type combo box
            typeComboBox.setItems(FXCollections.observableArrayList(PartnerType.values()));
            
            // Initialize spinners
            ratingSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 5, 0, 0.1));
            ratingCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 0));
            
            // Load partners list
            refreshPartnersList();
            
            // Set up list view selection listener
            partnersList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedPartner = newValue;
                    populateFields(newValue);
                }
            });
            
            // Set up custom cell factory for the list view
            partnersList.setCellFactory(listView -> new PartnerCell());
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation", e.getMessage());
        }
    }

    private void populateFields(Partner partner) {
        nameField.setText(partner.getName());
        typeComboBox.setValue(partner.getType());
        descriptionField.setText(partner.getDescription());
        emailField.setText(partner.getEmail());
        phoneField.setText(partner.getPhone());
        addressField.setText(partner.getAddress());
        websiteField.setText(partner.getWebsite());
        ratingSpinner.getValueFactory().setValue(partner.getRating());
        ratingCountSpinner.getValueFactory().setValue(partner.getRatingCount());
        
        // Handle image
        if (partner.getImagePath() != null && !partner.getImagePath().isEmpty()) {
            try {
                Image image = new Image(new File(partner.getImagePath()).toURI().toString());
                partnerImage.setImage(image);
                imagePath = partner.getImagePath();
            } catch (Exception e) {
                partnerImage.setImage(null);
                imagePath = null;
            }
        } else {
            partnerImage.setImage(null);
            imagePath = null;
        }
        
        // Handle video
        if (partner.getVideoPath() != null && !partner.getVideoPath().isEmpty()) {
            try {
                // For video files, we'll show a generic video icon
                Image videoIcon = new Image(getClass().getResourceAsStream("/images/video-icon.png"));
                partnerVideo.setImage(videoIcon);
                videoPath = partner.getVideoPath();
            } catch (Exception e) {
                partnerVideo.setImage(null);
                videoPath = null;
            }
        } else {
            partnerVideo.setImage(null);
            videoPath = null;
        }
    }

    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(browseImage.getScene().getWindow());
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            Image image = new Image(selectedFile.toURI().toString());
            partnerImage.setImage(image);
        }
    }

    @FXML
    private void browseVideo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une vidéo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mov", "*.wmv")
        );
        
        File selectedFile = fileChooser.showOpenDialog(browseVideo.getScene().getWindow());
        if (selectedFile != null) {
            videoPath = selectedFile.getAbsolutePath();
            // For video files, we'll show a generic video icon
            Image videoIcon = new Image(getClass().getResourceAsStream("/images/video-icon.png"));
            partnerVideo.setImage(videoIcon);
        }
    }

    @FXML
    private void addPartner() {
        if (!validateFields()) return;

        try {
            Partner newPartner = new Partner();
            newPartner.setName(nameField.getText());
            newPartner.setType(typeComboBox.getValue());
            newPartner.setDescription(descriptionField.getText());
            newPartner.setEmail(emailField.getText());
            newPartner.setPhone(phoneField.getText());
            newPartner.setAddress(addressField.getText());
            newPartner.setWebsite(websiteField.getText());
            newPartner.setRating(ratingSpinner.getValue());
            newPartner.setRatingCount(ratingCountSpinner.getValue());
            newPartner.setImagePath(imagePath);
            newPartner.setVideoPath(videoPath);
            newPartner.setCreatedAt(LocalDateTime.now());
            newPartner.setUpdatedAt(LocalDateTime.now());

            ps.create(newPartner);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire ajouté avec succès !");
            clearFields();
            refreshPartnersList();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du partenaire : " + e.getMessage());
        }
    }

    @FXML
    private void setPartner() {
        if (selectedPartner == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner un partenaire à modifier.");
            return;
        }

        if (!validateFields()) return;

        try {
            selectedPartner.setName(nameField.getText());
            selectedPartner.setType(typeComboBox.getValue());
            selectedPartner.setDescription(descriptionField.getText());
            selectedPartner.setEmail(emailField.getText());
            selectedPartner.setPhone(phoneField.getText());
            selectedPartner.setAddress(addressField.getText());
            selectedPartner.setWebsite(websiteField.getText());
            selectedPartner.setRating(ratingSpinner.getValue());
            selectedPartner.setRatingCount(ratingCountSpinner.getValue());
            selectedPartner.setImagePath(imagePath);
            selectedPartner.setVideoPath(videoPath);
            selectedPartner.setUpdatedAt(LocalDateTime.now());

            ps.update(selectedPartner);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire modifié avec succès !");
            refreshPartnersList();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du partenaire : " + e.getMessage());
        }
    }

    @FXML
    private void delPartner() {
        if (selectedPartner == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez sélectionner un partenaire à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le partenaire");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce partenaire ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ps.delete(selectedPartner.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire supprimé avec succès !");
                    clearFields();
                    refreshPartnersList();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du partenaire : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void Refresh() {
        try {
            refreshPartnersList();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Liste actualisée avec succès !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'actualisation : " + e.getMessage());
        }
    }

    @FXML
    private void goToAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement du tableau de bord : " + e.getMessage());
        }
    }

    private void refreshPartnersList() throws SQLException {
        List<Partner> partners = ps.readAll();
        ObservableList<Partner> observableList = FXCollections.observableArrayList(partners);
        partnersList.setItems(observableList);
    }

    private void clearFields() {
        nameField.clear();
        typeComboBox.setValue(null);
        descriptionField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        websiteField.clear();
        ratingSpinner.getValueFactory().setValue(0.0);
        ratingCountSpinner.getValueFactory().setValue(0);
        partnerImage.setImage(null);
        partnerVideo.setImage(null);
        imagePath = null;
        videoPath = null;
        selectedPartner = null;
    }

    private boolean validateFields() {
        if (nameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Le nom du partenaire est requis.");
            return false;
        }
        if (typeComboBox.getValue() == null) {
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

    static class PartnerCell extends ListCell<Partner> {
        private final HBox content;
        private final Label nameLabel;
        private final Label typeLabel;
        private final Label emailLabel;

        public PartnerCell() {
            content = new HBox(10);
            nameLabel = new Label();
            typeLabel = new Label();
            emailLabel = new Label();

            nameLabel.setStyle("-fx-font-weight: bold;");
            content.getChildren().addAll(nameLabel, typeLabel, emailLabel);
        }

        @Override
        protected void updateItem(Partner partner, boolean empty) {
            super.updateItem(partner, empty);
            if (empty || partner == null) {
                setText(null);
                setGraphic(null);
            } else {
                nameLabel.setText(partner.getName());
                typeLabel.setText(partner.getType().toString());
                emailLabel.setText(partner.getEmail());
                setGraphic(content);
            }
        }
    }
}
