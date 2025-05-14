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
import javafx.scene.control.TextArea;
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
    @FXML
    public Button save;
    @FXML
    public Button returnBtn;
    @FXML
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
    private TextArea descriptionField;

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
        
        // Initialize contract type combo box
        partnershipTypeComboBox.getItems().addAll(ContractType.values());
        
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

        // Set up partnership form and partner selection handler
        partnershipForm.setVisible(false);
        partnersList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedPartner = newVal;
            if (newVal != null) {
                partnershipForm.setVisible(true);
                DisplayImage(); // Show partner image
                
                // Pre-fill some fields
                if (typeFilter.getValue() == null) {
                    typeFilter.setValue(newVal.getType());
                }
            } else {
                partnershipForm.setVisible(false);
                if (myImage != null) { // Clear image when no partner is selected
                    myImage.setImage(null);
                }
            }
        });
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

    @FXML
    public void generateContract(ActionEvent event) {
        if (selectedPartner == null) {
            showAlert("Error", "Please select a partner first", Alert.AlertType.ERROR);
            return;
        }

        if (partnershipTypeComboBox.getValue() == null) {
            showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
            return;
        }

        try {
            ContractType contractType = partnershipTypeComboBox.getValue();
            String description = descriptionField.getText();
            
            if (description == null || description.trim().isEmpty()) {
                showAlert("Error", "Please enter a partnership description", Alert.AlertType.ERROR);
                return;
            }
            
            // First create the partnership if it doesn't exist
            Partnership partnership = new Partnership();
            partnership.setPartnerId(selectedPartner.getId());
            partnership.setOrganizerId(1); // You might want to get this from a logged-in user session
            partnership.setContractType(contractType.toString());
            partnership.setDescription(description);
            partnership.setSigned(false);
            partnership.setStatus("Pending");
            partnership.setCreatedAt(LocalDateTime.now());
            
            System.out.println("Creating partnership with details:");
            System.out.println("Partner ID: " + selectedPartner.getId());
            System.out.println("Contract Type: " + contractType);
            System.out.println("Description: " + description);
            
            // Save the partnership first
            Partnership savedPartnership = partnershipService.create(partnership);
            
            if (savedPartnership == null) {
                showAlert("Error", "Failed to save partnership to database. Check database connection.", Alert.AlertType.ERROR);
                return;
            }
            
            System.out.println("Partnership saved successfully with ID: " + savedPartnership.getId());

            // Then generate the contract with partnership details
            try {
                PDFGenerator.generateContract(
                    selectedPartner.getName(),
                    selectedPartner.getEmail(),
                    selectedPartner.getPhone(),
                    selectedPartner.getAddress(),
                    savedPartnership.getId(),
                    contractType.toString(),
                    description,
                    partnership.getOrganizerId()
                );
                
                System.out.println("Contract generated successfully");
                showAlert("Success", "Partnership created and contract generated successfully", Alert.AlertType.INFORMATION);
                clearPartnershipForm();
            } catch (Exception e) {
                System.err.println("Error generating contract: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "Failed to generate contract: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.err.println("Error in generateContract: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to create partnership: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void verifyPartnershipCreation(int partnershipId) {
        try {
            Partnership created = partnershipService.read(partnershipId);
            if (created != null) {
                System.out.println("Verified partnership in database:");
                System.out.println("ID: " + created.getId());
                System.out.println("Partner ID: " + created.getPartnerId());
                System.out.println("Contract Type: " + created.getContractType());
                System.out.println("Description: " + created.getDescription());
                System.out.println("Status: " + created.getStatus());
            } else {
                System.err.println("Failed to verify partnership with ID: " + partnershipId);
            }
        } catch (Exception e) {
            System.err.println("Error verifying partnership: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void savePartnership(ActionEvent event) {
        if (selectedPartner == null) {
            showAlert("Error", "Please select a partner first", Alert.AlertType.ERROR);
            return;
        }

        try {
            ContractType contractType = partnershipTypeComboBox.getValue();
            String description = descriptionField.getText();
            
            if (contractType == null) {
                showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
                return;
            }

            if (description == null || description.trim().isEmpty()) {
                showAlert("Error", "Please enter a partnership description", Alert.AlertType.ERROR);
                return;
            }

            Partnership partnership = new Partnership();
            partnership.setPartnerId(selectedPartner.getId());
            partnership.setOrganizerId(1); // You might want to get this from a logged-in user session
            partnership.setContractType(contractType.toString());
            partnership.setDescription(description);
            partnership.setSigned(false);
            partnership.setStatus("Pending");
            partnership.setCreatedAt(LocalDateTime.now());
            partnership.setSignedContractFile(null);
            partnership.setSignedAt(null);

            System.out.println("Saving partnership with details:");
            System.out.println("Partner ID: " + selectedPartner.getId());
            System.out.println("Contract Type: " + contractType);
            System.out.println("Description: " + description);

            Partnership savedPartnership = partnershipService.create(partnership);
            
            if (savedPartnership == null) {
                showAlert("Error", "Failed to save partnership to database. Check database connection.", Alert.AlertType.ERROR);
                return;
            }
            
            System.out.println("Partnership saved successfully with ID: " + savedPartnership.getId());
            
            // Verify the partnership was actually saved
            verifyPartnershipCreation(savedPartnership.getId());
            
            showAlert("Success", "Partnership created successfully\nYou can now generate the contract.", Alert.AlertType.INFORMATION);
            clearPartnershipForm();
        } catch (Exception e) {
            System.err.println("Error in savePartnership: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to create partnership: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearPartnershipForm() {
        partnershipTypeComboBox.setValue(null);
        descriptionField.clear();
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
            if (myImage != null) {
                myImage.setImage(null);
            }
            return;
        }

        String imagePathFromDb = selectedPartner.getImagePath();
        if (imagePathFromDb == null || imagePathFromDb.trim().isEmpty()) {
            System.out.println("No image path available for partner: " + selectedPartner.getName());
            if (myImage != null) {
                myImage.setImage(null); // Clear image if path is missing
            }
            return;
        }

        // Sanitize the imagePathFromDb
        String cleanImagePath = imagePathFromDb.trim();
        
        // Remove potential "Images/" or "/Images/" prefix to avoid "/Images/Images/..."
        // and ensure it is treated as relative to the root /Images folder.
        if (cleanImagePath.toLowerCase().startsWith("/images/")) {
            cleanImagePath = cleanImagePath.substring("/images/".length());
        } else if (cleanImagePath.toLowerCase().startsWith("images/")) {
            cleanImagePath = cleanImagePath.substring("images/".length());
        }
        
        // Remove any other leading slash to ensure correct concatenation
        if (cleanImagePath.startsWith("/")) {
             cleanImagePath = cleanImagePath.substring(1);
        }

        // Construct the final resource path, assuming images are in "resources/Images/"
        String resourcePath = "/Images/" + cleanImagePath;
        // Normalize path (e.g. replace // with / if cleanImagePath was empty after stripping)
        resourcePath = resourcePath.replaceAll("//+", "/");
        if (resourcePath.equals("/Images/")) { // Handle case where cleanImagePath might have been just "Images/" or "/"
            System.out.println("Cleaned image path resulted in an empty path relative to /Images/. Original: " + imagePathFromDb);
             if (myImage != null) {
                myImage.setImage(null);
            }
            return;
        }


        System.out.println("Attempting to load image from resource path: " + resourcePath);

        URL imageUrl = null;
        try {
            // Try with class's getResource first.
            imageUrl = getClass().getResource(resourcePath);

            if (imageUrl == null) {
                String classLoaderPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                System.out.println("getClass().getResource() failed for '" + resourcePath + "'. Trying with Thread.currentThread().getContextClassLoader().getResource(): " + classLoaderPath);
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    imageUrl = contextClassLoader.getResource(classLoaderPath);
                }
                if (imageUrl == null) {
                    System.out.println("ContextClassLoader.getResource() failed for '" + classLoaderPath + "'. Trying with ClassLoader.getSystemResource(): " + classLoaderPath);
                    imageUrl = ClassLoader.getSystemResource(classLoaderPath);
                }
            }

            if (imageUrl == null) {
                System.err.println("Image resource not found: " + resourcePath + 
                                   " (also tried variants like: " + (resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath) + 
                                   " with different classloaders)");
                showAlert("Image Not Found", "The image for partner '" + selectedPartner.getName() + "' could not be found. Path used: " + imagePathFromDb, Alert.AlertType.WARNING);
                if (myImage != null) {
                    myImage.setImage(null); // Clear image
                }
                return;
            }

            System.out.println("Successfully found image URL: " + imageUrl.toString());
            Image image = new Image(imageUrl.toString());

            if (image.isError()) {
                System.err.println("Error loading image from URL: " + imageUrl.toString());
                String errorMessage = "Unknown error.";
                if (image.getException() != null) {
                    System.err.println("Image loading exception: " + image.getException().getMessage());
                    image.getException().printStackTrace();
                    errorMessage = image.getException().getMessage();
                }
                showAlert("Image Load Error", "Failed to load image for " + selectedPartner.getName() + ". Error: " + errorMessage, Alert.AlertType.ERROR);
                if (myImage != null) {
                    myImage.setImage(null); // Clear image on error
                }
                return;
            }

            if (myImage != null) {
                myImage.setImage(image);
                myImage.setFitWidth(450);
                myImage.setFitHeight(150);
                myImage.setPreserveRatio(true);
            }
            System.out.println("Image loaded and displayed successfully for: " + selectedPartner.getName());

        } catch (IllegalArgumentException iae) {
             System.err.println("Invalid URL for image: " + (imageUrl != null ? imageUrl.toString() : "null URL (constructed from " + resourcePath + ")") + ". Exception: " + iae.getMessage());
             iae.printStackTrace();
             showAlert("Image Load Error", "The path for the image was invalid: " + resourcePath, Alert.AlertType.ERROR);
             if (myImage != null) {
                myImage.setImage(null);
             }
        }
        catch (Exception e) {
            System.err.println("An unexpected error occurred while displaying image " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Image Display Error", "Failed to display image for " + selectedPartner.getName() + ". Error: " + e.getMessage(), Alert.AlertType.ERROR);
            if (myImage != null) {
                myImage.setImage(null); // Clear image on any other exception
            }
        }
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

    @FXML
    public void returnToOrganizer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/organisateurDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) returnBtn.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}
