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

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

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
        try {
            List<Partnership> partnerships = partnershipService.readAll();
            ObservableList<Partnership> observableList = FXCollections.observableArrayList(partnerships);
            partnershipList.setItems(observableList);
            partnershipTypeComboBox.setItems(FXCollections.observableArrayList(ContractType.values()));

            partnershipList.setCellFactory(listView -> new PartnershipCell());
            partnershipList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Partnership>(){
                @Override
                public void changed(ObservableValue<? extends Partnership> observableValue, Partnership oldValue, Partnership newValue) {
                    if (newValue != null) {
                        currentPartnership = newValue.getDescription();
                        PartnershipLabel.setText(currentPartnership);
                        DisplayImage();
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
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
                organizerName.setText("User");
                partnerName.setText(String.valueOf(partnership.getPartnerId()));

                description.setText(partnership.getDescription());
                contractType.setText(partnership.getContractType());
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
