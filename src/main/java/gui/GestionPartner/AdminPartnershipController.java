package gui.GestionPartner;

import entities.ContractType;
import entities.Partnership;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import services.PartnershipService;

import java.sql.SQLException;
import java.util.Optional;

public class AdminPartnershipController {

    public GridPane Grid;
    public Sphere modelGroup;
    @FXML
    private ListView<Partnership> partnershipList;

    @FXML
    private Button Ajouter;

    @FXML
    private Button Supprimer;

    @FXML
    private Button Modifier;

    private PartnershipService partnershipService;
    private ObservableList<Partnership> partnerships;


    public void initialize() {

        partnershipService = new PartnershipService();
        partnerships = FXCollections.observableArrayList();
        partnershipList.setItems(partnerships);
        partnershipList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        loadPartnerships();

        // Set custom cell factory
        partnershipList.setCellFactory(listView -> new PartnershipCell());
    }

    private void loadPartnerships() {
        partnerships.clear();
        try {
            partnerships.addAll(partnershipService.readAll());
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des partenariats", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void addPartnership(MouseEvent mouseEvent) {
        Dialog<Partnership> dialog = new Dialog<>();
        dialog.setTitle("Add Partnership");
        dialog.setHeaderText("Enter partnership details");

        // Create a GridPane for the input fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Create text fields for each property
        TextField organizerIdField = new TextField();
        TextField partnerIdField = new TextField();

        // Create a ComboBox for contract type
        ComboBox<ContractType> contractTypeComboBox = new ComboBox<>();
        contractTypeComboBox.getItems().addAll(ContractType.values()); // Add all ContractType values to the ComboBox
        contractTypeComboBox.setPromptText("Selectionner type de contrat");

        TextField descriptionField = new TextField();
        CheckBox isSignedCheckbox = new CheckBox("Is Signed"); // Checkbox for boolean value

        // Add fields to the grid
        grid.addRow(0, new Label("Organizer ID:"), organizerIdField);
        grid.addRow(1, new Label("Partner ID:"), partnerIdField);
        grid.addRow(2, new Label("Contract Type:"), contractTypeComboBox);
        grid.addRow(3, new Label("Description:"), descriptionField);
        grid.addRow(4, new Label("Is Signed:"), isSignedCheckbox);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Convert the result to a Partnership object when the Save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Parse the input values
                try {
                    int organizerId = Integer.parseInt(organizerIdField.getText());
                    int partnerId = Integer.parseInt(partnerIdField.getText());

                    // Get selected contract type from ComboBox
                    ContractType contractType = contractTypeComboBox.getValue();
                    if (contractType == null) {
                        showAlert("Erreur", "Veuillez selectionner un type de contrat", Alert.AlertType.ERROR);
                        return null; // Don't proceed if no contract type is selected
                    }

                    String description = descriptionField.getText();
                    boolean isSigned = isSignedCheckbox.isSelected(); // Get value from checkbox

                    // Create a new partnership object
                    Partnership newPartnership = new Partnership(0, organizerId, partnerId, contractType, description, isSigned);
                    return newPartnership;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Input Invalid", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(newPartnership -> {
            try {
                partnershipService.create(newPartnership);
                loadPartnerships();
            } catch (SQLException e) {
                showAlert("Erreur", "Echec d'ajout de Partnership", Alert.AlertType.ERROR);
            }
        });
    }


    @FXML
    private void delPartnership(MouseEvent mouseEvent) {
        Partnership selected = partnershipList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Etes Vous sur ?");
            confirmationAlert.setContentText("Cette Action est definitive");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    partnershipService.delete(selected);
                    loadPartnerships();
                } catch (SQLException e) {
                    showAlert("Erreur", "Echec de suppression de Partnership", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Alert", "Veuillez selectionner un partenaire", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void setPartnership(MouseEvent mouseEvent) {
        Partnership selected = partnershipList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Create a dialog to modify partnership details
            Dialog<Partnership> dialog = new Dialog<>();
            dialog.setTitle("Edit Partnership");
            dialog.setHeaderText("Modify partnership details");

            // Create a GridPane for the input fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            // Create text fields for each property
            TextField organizerIdField = new TextField(String.valueOf(selected.getOrganizerId()));
            TextField partnerIdField = new TextField(String.valueOf(selected.getPartnerId()));
            TextField contractTypeField = new TextField(selected.getContractType().toString());
            TextField descriptionField = new TextField(selected.getDescription());
            TextField isSignedField = new TextField(selected.isSigned() ? "Yes" : "No"); // Accept "Yes" or "No"

            // Add fields to the grid
            grid.addRow(0, new Label("Organizer ID:"), organizerIdField);
            grid.addRow(1, new Label("Partner ID:"), partnerIdField);
            grid.addRow(2, new Label("Contract Type:"), contractTypeField);
            grid.addRow(3, new Label("Description:"), descriptionField);
            grid.addRow(4, new Label("Is Signed (Yes/No):"), isSignedField);

            dialog.getDialogPane().setContent(grid);

            // Add buttons
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Convert the result to a Partnership object when the Save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Update the selected partnership object with new values
                    selected.setOrganizerId(Integer.parseInt(organizerIdField.getText()));
                    selected.setPartnerId(Integer.parseInt(partnerIdField.getText()));
                    selected.setContractType(ContractType.valueOf(contractTypeField.getText())); // Adjust if necessary
                    selected.setDescription(descriptionField.getText());
                    selected.isSigned();

                    return selected;
                }
                return null;
            });

            // Show the dialog and handle the result
            dialog.showAndWait().ifPresent(updatedPartnership -> {
                try {
                    partnershipService.update(updatedPartnership);
                    loadPartnerships();
                } catch (SQLException e) {
                    showAlert("Error", "Failed to update partnership", Alert.AlertType.ERROR);
                }
            });
        } else {
            showAlert("Warning", "Please select a partnership to edit", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
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
    private void create3DModel(Group root) {
        Sphere bot = new Sphere(1); // Create a sphere with a radius of 1
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.GREEN); // Set the color or texture
        bot.setMaterial(material);
        root.getChildren().add(bot);
    }


}
