package gui.GestionPartner;

import entities.ContractType;
import entities.Partnership;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import services.PartnershipService;
import gui.GestionUser.UserSession;
import entities.user;
import services.userService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;

public class UserControllerPartnership {

    private final PartnershipService partnershipService = new PartnershipService();

    @FXML
    private ListView<Partnership> partnershipList;

    String currentPartnership;
    @FXML
    private Label PartnershipLabel;

    @FXML
    private Button Ajouter;

    @FXML
    private ComboBox<ContractType> partnershipTypeComboBox;
    @FXML
    private TextField descriptionField;

    @FXML
    private ImageView myImage;

    @FXML
    void initialize() {
        partnershipTypeComboBox.setItems(FXCollections.observableArrayList(ContractType.values()));
        partnershipList.setCellFactory(listView -> new PartnershipCell());

        partnershipList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Partnership>() {
            @Override
            public void changed(ObservableValue<? extends Partnership> observableValue, Partnership oldValue, Partnership newValue) {
                if (newValue != null) {
                    currentPartnership = newValue.getDescription();
                    PartnershipLabel.setText(currentPartnership);
                    DisplayImage();
                } else {
                    PartnershipLabel.setText("");
                    myImage.setImage(null);
                }
            }
        });
        loadOrganizerPartnerships();
    }

    private void loadOrganizerPartnerships() {
        try {
            UserSession session = UserSession.getInstance();
            if (session.getRole() == 3) {
                List<Partnership> partnerships = partnershipService.readByOrganizerId(session.getId());
                ObservableList<Partnership> observableList = FXCollections.observableArrayList(partnerships);
                partnershipList.setItems(observableList);
            } else {
                partnershipList.setItems(FXCollections.observableArrayList(new ArrayList<Partnership>()));
                showAlert("Access Denied", "This view is for organizers only.");
            }
        } catch (IllegalStateException e) {
            showAlert("Error", "User session not found. Please log in.");
            partnershipList.setItems(FXCollections.observableArrayList(new ArrayList<Partnership>()));
        } catch (Exception e) {
            showAlert("Error loading partnerships", e.getMessage());
            partnershipList.setItems(FXCollections.observableArrayList(new ArrayList<Partnership>()));
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleAddPartnership(MouseEvent event) {
        try {
            UserSession session = UserSession.getInstance();
            if (session.getRole() != 3) {
                showAlert("Access Denied", "Only organizers can add partnerships.");
                return;
            }

            TextInputDialog partnerDialog = new TextInputDialog();
            partnerDialog.setTitle("Input Partner ID");
            partnerDialog.setHeaderText("Enter the ID of the partner for this new partnership:");
            partnerDialog.setContentText("Partner ID:");
            Optional<String> partnerIdResult = partnerDialog.showAndWait();

            if (!partnerIdResult.isPresent() || partnerIdResult.get().trim().isEmpty()) {
                showAlert("Input Required", "Partner ID is required.");
                return;
            }

            int partnerId;
            try {
                partnerId = Integer.parseInt(partnerIdResult.get().trim());
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Partner ID must be a number.");
                return;
            }

            ContractType contractType = partnershipTypeComboBox.getValue();
            String description = descriptionField.getText();

            if (contractType == null) {
                showAlert("Input Required", "Please select a contract type.");
                return;
            }
            if (description == null || description.trim().isEmpty()) {
                showAlert("Input Required", "Description cannot be empty.");
                return;
            }

            Partnership newPartnership = new Partnership();
            newPartnership.setOrganizerId(session.getId());
            newPartnership.setPartnerId(partnerId);
            newPartnership.setContractType(contractType.toString());
            newPartnership.setDescription(description);
            newPartnership.setSigned(false);
            newPartnership.setStatus("Pending");
            newPartnership.setCreatedAt(java.time.LocalDateTime.now());

            Partnership created = partnershipService.create(newPartnership);
            if (created != null) {
                showAlert("Success", "Partnership created successfully with ID: " + created.getId());
                loadOrganizerPartnerships();
                descriptionField.clear();
                partnershipTypeComboBox.getSelectionModel().clearSelection();
            } else {
                showAlert("Error", "Failed to create partnership.");
            }

        } catch (IllegalStateException e) {
            showAlert("Error", "User session not found. Please log in.");
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePartnership(MouseEvent mouseEvent) {
        String description = descriptionField.getText();
        ContractType selectedType = partnershipTypeComboBox.getValue();
        Partnership selected = partnershipList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a partnership to update.");
            return;
        }
        selected.setDescription(description);
        selected.setContractType(selectedType != null ? selectedType.toString() : null);
        try {
            partnershipService.update(selected);
            showAlert("Success", "Partnership updated successfully!");
        } catch (Exception e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    static class PartnershipCell extends ListCell<Partnership> {
        @FXML
        private HBox Hbox;
        @FXML
        private Label organizerName;

        @FXML
        private Label partnerName;

        @FXML
        private Label contractType;

        @FXML
        private Label description;

        public PartnershipCell() {
            Hbox = new HBox(20);
            organizerName = new Label();
            partnerName = new Label();
            contractType = new Label();
            description = new Label();
            Hbox.getChildren().addAll(organizerName, partnerName, contractType, description);
        }

        @Override
        protected void updateItem(Partnership partnership, boolean empty) {
            super.updateItem(partnership, empty);
            if (empty || partnership == null) {
                setText(null);
                setGraphic(null);
            } else {
                organizerName.setText("Organizer ID: " + partnership.getOrganizerId());
                partnerName.setText("Partner ID: " + String.valueOf(partnership.getPartnerId()));

                description.setText("Desc: " + partnership.getDescription());
                contractType.setText("Type: " + partnership.getContractType());
                setGraphic(Hbox);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void DisplayImage() {
        Partnership selectedPartnership = partnershipList.getSelectionModel().getSelectedItem();

        if (selectedPartnership == null) {
            showAlert("No Partnership Selected", "Please select a partnership from the list.");
            return;
        }

        String imagePath = "/Images/" + selectedPartnership.getContractType() + ".png";
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl == null) {
            showAlert("Error", "Image not found at: " + imagePath);
            return;
        }

        Image image = new Image(imageUrl.toString());
        myImage.setImage(image);
    }

}
