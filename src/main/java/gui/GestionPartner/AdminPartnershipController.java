package gui.GestionPartner;

import entities.ContractType;
import entities.Partnership;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.StringConverter;
import services.PartnershipService;
import services.userService;
import entities.user;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminPartnershipController implements Initializable {

    public GridPane Grid;
    public Sphere modelGroup;
    @FXML
    private ListView<Partnership> partnershipList;
    @FXML
    private ComboBox<ContractType> contractTypeComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField partnerIdField;
    @FXML
    private ComboBox<user> organizerComboBox;
    @FXML
    private CheckBox signedCheckBox;

    private final PartnershipService partnershipService = new PartnershipService();
    private userService uService;
    private final ObservableList<Partnership> partnerships = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        uService = new userService();

        // Initialize contract type combo box
        contractTypeComboBox.setItems(FXCollections.observableArrayList(ContractType.values()));
        contractTypeComboBox.setConverter(new StringConverter<ContractType>() {
            @Override
            public String toString(ContractType type) {
                return type != null ? type.toString() : "";
            }

            @Override
            public ContractType fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                try {
                    return ContractType.valueOf(string);
                } catch (IllegalArgumentException e) {
                    System.err.println("AdminPartnershipController: No ContractType enum constant for string '" + string + "'");
                    return null;
                }
            }
        });

        // Initialize organizer combo box
        populateOrganizerComboBox();
        organizerComboBox.setConverter(new StringConverter<user>() {
            @Override
            public String toString(user organizer) {
                if (organizer == null) {
                    return null;
                }
                // Display format: "FirstName LastName (Username)"
                return String.format("%s %s (%s)", organizer.getFirstname(), organizer.getLastname(), organizer.getUsername());
            }

            @Override
            public user fromString(String string) {
                // Not strictly needed if the ComboBox is not editable or if selection is always from the list
                return organizerComboBox.getItems().stream()
                        .filter(organizer -> toString(organizer).equals(string))
                        .findFirst().orElse(null);
            }
        });

        // Load partnerships into ListView
        refreshPartnerships();
        partnershipList.setCellFactory(listView -> new PartnershipCell(this));
    }

    @FXML
    private void handleAdd() {
        try {
            int partnerId = Integer.parseInt(partnerIdField.getText());
            user selectedOrganizer = organizerComboBox.getSelectionModel().getSelectedItem();
            ContractType contractType = contractTypeComboBox.getValue();
            String description = descriptionArea.getText();
            boolean isSigned = signedCheckBox.isSelected();

            if (selectedOrganizer == null) {
                showAlert("Error", "Please select an organizer", Alert.AlertType.ERROR);
                return;
            }
            if (contractType == null) {
                showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
                return;
            }

            Partnership partnership = new Partnership();
            partnership.setPartnerId(partnerId);
            partnership.setOrganizerId(selectedOrganizer.getId());
            partnership.setContractType(contractType.toString());
            partnership.setDescription(description);
            partnership.setSigned(isSigned);
            partnership.setStatus("Pending");
            partnership.setCreatedAt(LocalDateTime.now());

            partnershipService.create(partnership);
            refreshPartnerships();
            clearFields();
            showAlert("Success", "Partnership created successfully", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid Partner ID", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        Partnership selectedPartnership = partnershipList.getSelectionModel().getSelectedItem();
        if (selectedPartnership != null) {
            partnershipService.delete(selectedPartnership.getId());
            refreshPartnerships();
        } else {
            showAlert("Error", "Please select a partnership to delete", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdate() {
        Partnership selectedPartnership = partnershipList.getSelectionModel().getSelectedItem();
        if (selectedPartnership != null) {
            try {
                int partnerId = Integer.parseInt(partnerIdField.getText());
                user selectedOrganizer = organizerComboBox.getSelectionModel().getSelectedItem();
                ContractType contractType = contractTypeComboBox.getValue();
                String description = descriptionArea.getText();
                boolean isSigned = signedCheckBox.isSelected();

                if (selectedOrganizer == null) {
                    showAlert("Error", "Please select an organizer", Alert.AlertType.ERROR);
                    return;
                }
                if (contractType == null) {
                    showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
                    return;
                }

                selectedPartnership.setPartnerId(partnerId);
                selectedPartnership.setOrganizerId(selectedOrganizer.getId());
                selectedPartnership.setContractType(contractType.toString());
                selectedPartnership.setDescription(description);
                selectedPartnership.setSigned(isSigned);

                partnershipService.update(selectedPartnership);
                refreshPartnerships();
                clearFields();
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid Partner ID", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "Please select a partnership to update", Alert.AlertType.ERROR);
        }
    }

    private void refreshPartnerships() {
        partnerships.clear();
        partnerships.addAll(partnershipService.readAll());
        partnershipList.setItems(partnerships);
    }

    private void clearFields() {
        partnerIdField.clear();
        organizerComboBox.getSelectionModel().clearSelection();
        contractTypeComboBox.setValue(null);
        descriptionArea.clear();
        signedCheckBox.setSelected(false);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to populate organizer ComboBox
    private void populateOrganizerComboBox() {
        try {
            List<user> allUsers = uService.getallUserdata();
            List<user> organizers = allUsers.stream()
                                            .filter(u -> u.getId_role() == 3) // Assuming role_id 3 is for Organizers
                                            .collect(Collectors.toList());
            organizerComboBox.setItems(FXCollections.observableArrayList(organizers));
        } catch (Exception e) {
            // Handle exceptions, e.g., show an alert or log
            System.err.println("Error populating organizer ComboBox: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not load organizers.", Alert.AlertType.ERROR);
        }
    }

    // Method to handle AI signature verification
    private void handleVerifySignature(Partnership partnership) {
        if (partnership.getSignedContractFile() == null || partnership.getSignedContractFile().isEmpty()) {
            showAlert("Verification Info", "No contract file specified for this partnership.", Alert.AlertType.INFORMATION);
            return;
        }

        // --- AI VERIFICATION PLACEHOLDER ---
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("AI Signature Verification (Mock)");
        confirmationDialog.setHeaderText("Simulating AI check for: " + partnership.getSignedContractFile());
        confirmationDialog.setContentText("Is the contract document signed?");

        ButtonType buttonTypeYes = new ButtonType("Yes (Signed)");
        ButtonType buttonTypeNo = new ButtonType("No (Not Signed)");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmationDialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        // --- END AI VERIFICATION PLACEHOLDER ---

        if (result.isPresent() && result.get() == buttonTypeYes) {
            try {
                partnership.setSigned(true);
                partnership.setSignedAt(LocalDateTime.now());
                partnership.setStatus("Signed (Verified)"); // Optional: Update status
                partnershipService.update(partnership);
                refreshPartnerships(); // Refresh the entire list to show changes
                showAlert("Success", "Contract marked as signed!", Alert.AlertType.INFORMATION);
            } catch (Exception e) { // Catch potential SQL exceptions from service
                showAlert("Error", "Failed to update partnership: " + e.getMessage(), Alert.AlertType.ERROR);
                // Optionally revert UI changes if DB update fails
                partnership.setSigned(false); // Revert in-memory change
                partnership.setSignedAt(null);
                partnership.setStatus("Pending"); // Revert status
            }
        } else if (result.isPresent() && result.get() == buttonTypeNo) {
            showAlert("Verification Result", "Contract marked as NOT signed by AI (Mock).", Alert.AlertType.INFORMATION);
        }
    }

    // Custom ListCell Class - made non-static
    class PartnershipCell extends ListCell<Partnership> {
        private final HBox hBox;
        private final Label organizerLabel;
        private final Label partnerLabel;
        private final Label contractTypeLabel;
        private final Label descriptionLabel;
        private final Label isSignedLabel;
        private final Label contractFileLabel; // Added to show contract file
        private final Button verifyButton;
        private final AdminPartnershipController controller; // Reference to outer class

        public PartnershipCell(AdminPartnershipController controller) {
            this.controller = controller; // Store reference to controller
            hBox = new HBox(10); // Add spacing to HBox
            hBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            organizerLabel = new Label();
            partnerLabel = new Label();
            contractTypeLabel = new Label();
            descriptionLabel = new Label();
            isSignedLabel = new Label();
            contractFileLabel = new Label(); // Initialize new label

            VBox detailsVBox = new VBox(5); // VBox for text details
            detailsVBox.getChildren().addAll(
                    organizerLabel, partnerLabel, contractTypeLabel,
                    descriptionLabel, contractFileLabel, isSignedLabel
            );

            verifyButton = new Button("Verify Signature");
            verifyButton.getStyleClass().add("action-button-small"); // Optional: for styling

            // Add details and button to HBox
            // Make detailsVBox take up available horizontal space
            HBox.setHgrow(detailsVBox, javafx.scene.layout.Priority.ALWAYS);
            hBox.getChildren().addAll(detailsVBox, verifyButton);
        }

        @Override
        protected void updateItem(Partnership partnership, boolean empty) {
            super.updateItem(partnership, empty);
            if (empty || partnership == null) {
                setText(null);
                setGraphic(null);
            } else {
                organizerLabel.setText("Organizer ID: " + partnership.getOrganizerId());
                partnerLabel.setText("Partner ID: " + partnership.getPartnerId());
                contractTypeLabel.setText("Contract Type: " + partnership.getContractType());
                descriptionLabel.setText("Description: " + partnership.getDescription());
                descriptionLabel.setWrapText(true); // Allow description to wrap

                String contractPath = partnership.getSignedContractFile();
                if (contractPath != null && !contractPath.isEmpty() && !contractPath.equalsIgnoreCase("null")) {
                    contractFileLabel.setText("Contract File: " + contractPath);
                    contractFileLabel.setVisible(true);
                } else {
                    contractFileLabel.setText("Contract File: Not available");
                    contractFileLabel.setVisible(true); // Or false if you prefer to hide
                }
                
                isSignedLabel.setText("Status: " + (partnership.isSigned() ? "Signed on " + partnership.getSignedAt() : "Not Signed (Status: " + partnership.getStatus() + ")"));
                isSignedLabel.setTextFill(partnership.isSigned() ? Color.GREEN : Color.RED);

                // Configure verify button
                if (!partnership.isSigned() && contractPath != null && !contractPath.isEmpty() && !contractPath.equalsIgnoreCase("null")) {
                    verifyButton.setVisible(true);
                    verifyButton.setOnAction(event -> controller.handleVerifySignature(partnership));
                } else {
                    verifyButton.setVisible(false);
                }
                setGraphic(hBox);
            }
        }
    }

    @FXML
    private void addPartnership() {
        handleAdd();
    }

    @FXML
    private void delPartnership() {
        handleDelete();
    }

    @FXML
    private void setPartnership() {
        handleUpdate();
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) partnershipList.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

}
