package gui.GestionPartner;

import entities.Partner;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.PartnerService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminControllerPartner {

    private final PartnerService ps = new PartnerService();
    public Button Actualiser;

    @FXML
    private ListView<Partner> partnersList;

    String currentPartner;
    @FXML
    private Label PartnerLabel;

    @FXML
    private Button Ajouter;
    @FXML
    private Button Supprimer;
    @FXML
    private Button Modifier;

    @FXML
    void initialize() {
        try {
            // Récupérer les données depuis la base de données
            List<Partner> partners = ps.readAll();
            ObservableList<Partner> observableList = FXCollections.observableArrayList(partners);
            partnersList.setItems(observableList);

            // Définir une cellule personnalisée pour afficher plusieurs colonnes
            partnersList.setCellFactory(listView -> new UserControllerPartner.PartnerCell());
            partnersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Partner>(){

                @Override
                public void changed(ObservableValue<? extends Partner> observableValue, Partner partner, Partner t1) {
                    currentPartner = partnersList.getSelectionModel().getSelectedItem().getName();
                    PartnerLabel.setText(currentPartner);
                }
            });
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void addPartner(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddPartner.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Partenaire");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement");
            alert.setContentText("Erreur lors du chargement de la fenêtre d'ajout : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void delPartner(MouseEvent mouseEvent) {
        Partner selectedPartner = partnersList.getSelectionModel().getSelectedItem();

        if (selectedPartner == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur de sélection", "Veuillez sélectionner un partenaire à supprimer.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce partenaire ?");
        confirmationAlert.setContentText("Partenaire : " + selectedPartner.getName());

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ps.delete(selectedPartner);

                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Partenaire supprimé avec succès !");
                    initialize(); // Rafraîchir la liste après suppression
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Erreur lors de la suppression : " + e.getMessage());
                }
            }
        });
    }

    public void setPartner(MouseEvent mouseEvent) {
        Partner selectedPartner = partnersList.getSelectionModel().getSelectedItem();

        if (selectedPartner == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur de sélection", "Veuillez sélectionner un partenaire à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyPartner.fxml"));
            Parent root = loader.load();

            ModifyPartnerController modifyController = loader.getController();
            modifyController.setPartner(selectedPartner);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Partenaire");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Erreur lors du chargement de la fenêtre de modification : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void Refresh(MouseEvent mouseEvent) {
        try {
            List<Partner> partners = ps.readAll();
            ObservableList<Partner> observableList = FXCollections.observableArrayList(partners);
            partnersList.setItems(observableList);
            partnersList.getSelectionModel().clearSelection();

            showAlert(Alert.AlertType.INFORMATION, "Mise à jour réussie", "Liste des partenaires actualisée avec succès !");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Erreur lors de l'actualisation : " + e.getMessage());
        }
    }

    // Classe personnalisée pour afficher plusieurs colonnes
    static class PartnerCell extends ListCell<Partner> {
        @FXML
        private HBox Hbox;
        @FXML
        private Label NameLabel;
        @FXML
        private Label TypeLabel;
        @FXML
        private Label ContactInfoLabel;

        public PartnerCell() {
            Hbox = new HBox(20); // Espacement de 20px entre les colonnes
            NameLabel = new Label();
            TypeLabel = new Label();
            ContactInfoLabel = new Label();

            Hbox.getChildren().addAll(NameLabel, TypeLabel, ContactInfoLabel);
        }

        @Override
        protected void updateItem(Partner partner, boolean empty) {
            super.updateItem(partner, empty);
            if (empty || partner == null) {
                setText(null);
                setGraphic(null);
            } else {
                NameLabel.setText(partner.getName());
                TypeLabel.setText(partner.getType().toString());
                ContactInfoLabel.setText(partner.getContactInfo());

                setGraphic(Hbox);
            }
        }
    }
}
