package gui.GestionPartner;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import entities.ContractType;
import entities.Partner;
import entities.PartnerType;
import entities.Partnership;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.PartnerService;
import services.PartnershipService;
import utils.PDFGenerator;

public class UserControllerPartner implements Initializable {

    private final PartnerService ps = new PartnerService();
    public Button save;
    public AnchorPane SelecPartner;

    @FXML
    private ListView<Partner> partnersList;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<PartnerType> typeFilter;

    @FXML
    private Button searchButton;

    @FXML
    private Button refreshButton;

    String currentPartner;
    @FXML
    private Label PartnerLabel;

    @FXML
    private Button Ajouter;

    @FXML
    private ComboBox<ContractType> partnershipTypeComboBox;
    @FXML
    private TextField descriptionField;

    @FXML
    private ImageView myImage;

    @FXML
    private VBox partnershipForm;
    @FXML
    private TextField startDateField;
    @FXML
    private TextField endDateField;
    @FXML
    private TextField termsField;
    @FXML
    private ComboBox<ContractType> contractTypeComboBox;
    @FXML
    private Button savePartnershipButton;
    @FXML
    private Button generateContractButton;

    private PartnershipService partnershipService;
    private Partner selectedPartner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        partnershipService = new PartnershipService();

        // Initialize partner type filter
        typeFilter.getItems().addAll(PartnerType.values());
        
        // Load partners list
        try {
            refreshPartnersList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load partners: " + e.getMessage());
        }
        
        // Set up custom cell factory for the list view
        partnersList.setCellFactory(listView -> new PartnerCell());

        // Set up search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                handleSearch();
            } catch (SQLException e) {
                showAlert("Error", "Failed to search partners: " + e.getMessage());
            }
        });

        // Set up partnership form
        partnershipForm.setVisible(false);
        partnersList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedPartner = newVal;
            partnershipForm.setVisible(newVal != null);
        });

        // Set up save partnership button
        savePartnershipButton.setOnAction(e -> {
            try {
                savePartnership();
            } catch (SQLException ex) {
                showAlert("Error", "Failed to save partnership: " + ex.getMessage());
            }
        });

        // Set up generate contract button
        generateContractButton.setOnAction(e -> generateContract());
    }

    @FXML
    private void handleSearch() throws SQLException {
        String searchText = searchField.getText().trim();
        PartnerType selectedType = typeFilter.getValue();

        List<Partner> partners = ps.readAll();
        partners.removeIf(partner -> {
            boolean matchesSearch = searchText.isEmpty() || 
                partner.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                partner.getDescription().toLowerCase().contains(searchText.toLowerCase()) ||
                partner.getEmail().toLowerCase().contains(searchText.toLowerCase());
            
            boolean matchesType = selectedType == null || partner.getType() == selectedType;
            
            return !(matchesSearch && matchesType);
        });

        partnersList.getItems().setAll(partners);
    }

    @FXML
    private void handleRefresh() {
        try {
            refreshPartnersList();
        } catch (SQLException e) {
            showAlert("Error", "Failed to refresh partners: " + e.getMessage());
        }
    }

    private void refreshPartnersList() throws SQLException {
        List<Partner> partners = ps.readAll();
        partnersList.getItems().setAll(partners);
    }

    private void savePartnership() throws SQLException {
        if (selectedPartner == null) {
            showAlert("Error", "Please select a partner first", Alert.AlertType.ERROR);
            return;
        }

        try {
            ContractType contractType = contractTypeComboBox.getValue();
            String description = descriptionField.getText();
            if (contractType == null) {
                showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
                return;
            }

            Partnership partnership = new Partnership();
            partnership.setPartnerId(selectedPartner.getId());
            partnership.setOrganizerId(1); // Set organizerId as needed
            partnership.setContractType(contractType.toString());
            partnership.setDescription(description);
            partnership.setSigned(false);
            partnership.setStatus("Pending");
            partnership.setCreatedAt(LocalDateTime.now());
            partnership.setSignedContractFile(null);
            partnership.setSignedAt(null);

            partnershipService.create(partnership);
            showAlert("Success", "Partnership created successfully", Alert.AlertType.INFORMATION);
            clearPartnershipForm();
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearPartnershipForm() {
        startDateField.clear();
        endDateField.clear();
        termsField.clear();
        contractTypeComboBox.setValue(null);
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void generateContract() {
        if (selectedPartner == null) {
            showAlert("Error", "Please select a partner first", Alert.AlertType.ERROR);
            return;
        }

        try {
            PDFGenerator.generateContract(
                selectedPartner.getName(),
                selectedPartner.getEmail(),
                selectedPartner.getPhone(),
                selectedPartner.getAddress()
            );
            showAlert("Success", "Contract generated successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to generate contract: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Custom ListCell Class to Display Multiple Columns
    static class PartnerCell extends ListCell<Partner> {
        private final HBox content;
        private final Label nameLabel;
        private final Label typeLabel;
        private final Label emailLabel;
        private final Label ratingLabel;

        public PartnerCell() {
            content = new HBox(10);
            nameLabel = new Label();
            typeLabel = new Label();
            emailLabel = new Label();
            ratingLabel = new Label();

            nameLabel.setStyle("-fx-font-weight: bold;");
            ratingLabel.setStyle("-fx-text-fill: #FF9800;");
            content.getChildren().addAll(nameLabel, typeLabel, emailLabel, ratingLabel);
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
                ratingLabel.setText(String.format("★ %.1f (%d)", partner.getRating(), partner.getRatingCount()));
                setGraphic(content);
            }
        }
    }

    @FXML
    private void DisplayImage() {
        Partner selectedPartner = partnersList.getSelectionModel().getSelectedItem();

        if (selectedPartner == null) {
            showAlert("No Partner Selected", "Please select a partner from the list.", Alert.AlertType.ERROR);
            return;
        }

        String imagePath = "/Images/" + selectedPartner.getImagePath(); // Ensure only filename is stored
        System.out.println("Trying to load image from: " + imagePath);

        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl == null) {
            showAlert("Error", "Image not found at: " + imagePath, Alert.AlertType.ERROR);
            return;
        }

        Image image = new Image(imageUrl.toString());
        myImage.setImage(image);
    }

    @FXML
    private void goToAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load admin dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
