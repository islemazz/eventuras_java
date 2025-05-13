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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

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
    private TextField organizerIdField;
    @FXML
    private CheckBox signedCheckBox;

    private final PartnershipService partnershipService = new PartnershipService();
    private final ObservableList<Partnership> partnerships = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize contract type combo box
        contractTypeComboBox.setItems(FXCollections.observableArrayList(ContractType.values()));
        contractTypeComboBox.setConverter(new StringConverter<ContractType>() {
            @Override
            public String toString(ContractType type) {
                return type != null ? type.getDisplayName() : "";
            }

            @Override
            public ContractType fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                for (ContractType type : ContractType.values()) {
                    if (type.getDisplayName().equals(string)) {
                        return type;
                    }
                }
                return null;
            }
        });

        // Load partnerships into ListView
        refreshPartnerships();
        partnershipList.setCellFactory(listView -> new PartnershipCell());
    }

    @FXML
    private void handleAdd() {
        try {
            int partnerId = Integer.parseInt(partnerIdField.getText());
            int organizerId = Integer.parseInt(organizerIdField.getText());
            ContractType contractType = contractTypeComboBox.getValue();
            String description = descriptionArea.getText();
            boolean isSigned = signedCheckBox.isSelected();

            if (contractType == null) {
                showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
                return;
            }

            Partnership partnership = new Partnership();
            partnership.setPartnerId(partnerId);
            partnership.setOrganizerId(organizerId);
            partnership.setContractType(contractType.toString());
            partnership.setDescription(description);
            partnership.setSigned(isSigned);
            partnership.setStatus("Pending");

            partnershipService.create(partnership);
            refreshPartnerships();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid IDs", Alert.AlertType.ERROR);
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
                int organizerId = Integer.parseInt(organizerIdField.getText());
                ContractType contractType = contractTypeComboBox.getValue();
                String description = descriptionArea.getText();
                boolean isSigned = signedCheckBox.isSelected();

                if (contractType == null) {
                    showAlert("Error", "Please select a contract type", Alert.AlertType.ERROR);
                    return;
                }

                selectedPartnership.setPartnerId(partnerId);
                selectedPartnership.setOrganizerId(organizerId);
                selectedPartnership.setContractType(contractType.toString());
                selectedPartnership.setDescription(description);
                selectedPartnership.setSigned(isSigned);

                partnershipService.update(selectedPartnership);
                refreshPartnerships();
                clearFields();
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter valid IDs", Alert.AlertType.ERROR);
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
        organizerIdField.clear();
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

    // Custom ListCell Class


    static class PartnershipCell extends ListCell<Partnership> {
        private final HBox hBox;
        private final Label organizerLabel;
        private final Label partnerLabel;
        private final Label contractTypeLabel;
        private final Label descriptionLabel;
        private final Label isSignedLabel;

        public PartnershipCell() {
            hBox = new HBox();
            organizerLabel = new Label();
            partnerLabel = new Label();
            contractTypeLabel = new Label();
            descriptionLabel = new Label();
            isSignedLabel = new Label();

            VBox vBox = new VBox();
            vBox.getChildren().addAll(organizerLabel, partnerLabel, contractTypeLabel, descriptionLabel, isSignedLabel);
            hBox.getChildren().add(vBox);
        }

        @Override
        protected void updateItem(Partnership partnership, boolean empty) {
            super.updateItem(partnership, empty);
            if (empty || partnership == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Set the text for each label based on the partnership object
                organizerLabel.setText("Organizer ID: " + partnership.getOrganizerId()); // Adjust this if you have a method to get organizer name
                partnerLabel.setText("Partner ID: " + partnership.getPartnerId()); // Adjust this if you have a method to get partner name
                contractTypeLabel.setText("Contract Type: " + partnership.getContractType());
                descriptionLabel.setText("Description: " + partnership.getDescription());
                isSignedLabel.setText("Is Signed: " + (partnership.isSigned() ? "Yes" : "No")); // Assuming isSigned returns boolean

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) partnershipList.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
