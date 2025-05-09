package gui.GestionPartner;

import entities.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.PartnerService;
import services.PartnershipService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class UserControllerPartner {

    private final PartnerService ps = new PartnerService();
    public Button save;
    public AnchorPane SelecPartner;

    @FXML
    private ListView<Partner> partnersList;

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
    void initialize() {
        try {

            // Fetch data from the database
            List<Partner> partners = ps.readAll();
            ObservableList<Partner> observableList = FXCollections.observableArrayList(partners);
            partnersList.setItems(observableList);
            partnershipTypeComboBox.setItems(FXCollections.observableArrayList(ContractType.values()));

            // Set custom cell factory to display multiple columns
            partnersList.setCellFactory(listView -> new PartnerCell());
            partnersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Partner>(){

                @Override
                public void changed(ObservableValue<? extends Partner> observableValue, Partner partner, Partner t1) {
                    currentPartner = partnersList.getSelectionModel().getSelectedItem().getName();
                    PartnerLabel.setText(currentPartner);
                    DisplayImage();
                }

            });
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void savePartnership(MouseEvent mouseEvent) {
        String description = descriptionField.getText();
        ContractType selectedType = partnershipTypeComboBox.getValue();
        SelectionModel<Partner> selectionModel = partnersList.getSelectionModel();
        selectionModel.select(partnersList.getSelectionModel().getSelectedIndex());
        int id = partnersList.getSelectionModel().getSelectedItem().getId();
        Partnership partnership = new Partnership(1,id,selectedType,description,false);
        PartnershipService partnershipService = new PartnershipService();
        try {
            partnershipService.create(partnership);
            showAlert("Success", "Partnership saved successfully!");
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    // Custom ListCell Class to Display Multiple Columns
    static class PartnerCell extends ListCell<Partner> {
        @FXML
        private HBox Hbox;
        @FXML
        private Label NameLabel;
        @FXML
        private Label TypeLabel;
        @FXML
        private Label ContactInfoLAbel;



        public PartnerCell() {
            // Create HBox and Labels manually
            Hbox = new HBox(20); // 20px spacing between columns
            NameLabel = new Label();
            TypeLabel = new Label();
            ContactInfoLAbel = new Label();


            Hbox.getChildren().addAll(NameLabel, TypeLabel, ContactInfoLAbel);
        }

        @Override
        protected void updateItem(Partner partner, boolean empty) {
            super.updateItem(partner, empty);
            if (empty || partner == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Set text values for each column
                NameLabel.setText(partner.getName());
                TypeLabel.setText(partner.getType().toString());
                ContactInfoLAbel.setText(partner.getContactInfo());


                setGraphic(Hbox);
            }
        }


    }
    @FXML
    private void generateContract() {
        Partner selectedPartner = partnersList.getSelectionModel().getSelectedItem();

        if (selectedPartner == null) {
            showAlert("No Partner Selected", "Please select a partner from the list.");
            return;
        }

        String partnerName = selectedPartner.getName();
        PartnerType partnerType = selectedPartner.getType();
        String contactInfo = selectedPartner.getContactInfo();
        String description = descriptionField.getText(); // Get the partner's description
        String logoPath = selectedPartner.getImagePath(); // Get the path to the logo image

        Stage stage = (Stage) partnersList.getScene().getWindow();
        PDFGenerator.generateContract(partnerName, partnerType, contactInfo, description, stage);

        showAlert("Success", "Contract for " + partnerName + " generated successfully!");
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
        Partner selectedPartner = partnersList.getSelectionModel().getSelectedItem();

        if (selectedPartner == null) {
            showAlert("No Partner Selected", "Please select a partner from the list.");
            return;
        }

        String imagePath = "/Images/" + selectedPartner.getImagePath(); // Ensure only filename is stored
        System.out.println("Trying to load image from: " + imagePath);

        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl == null) {
            showAlert("Error", "Image not found at: " + imagePath);
            return;
        }

        Image image = new Image(imageUrl.toString());
        myImage.setImage(image);
    }






}
