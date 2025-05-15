package gui.GestionReserv;

import entities.Event;
import entities.Reservation;
import entities.Ticket;
import gui.GestionEvents.AfficherEventHOME;
import gui.GestionEvents.AjouterParticipation;
import gui.GestionProduit.AfficherProduit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.Service;
import services.ServiceEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherReservation {

    // Boutons de navigation
    public Button ajouter;
    public Button GoToEvents;
    public Button Collaborations;
    public Button Boutique;
    public Button Acceuil;
    public Button reclam;

    // Composants de l'interface
    @FXML
    private ListView<HBox> reservationListView;
    @FXML
    private TextField codeField;
    @FXML
    private Button deleteButton;
    @FXML
    private Button updateButton;
    @FXML
    private TextField etatField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField nbpField;
    @FXML
    private TextField seatField;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private final Service reservationService = new Service();

    @FXML
    public void initialize() {
        try {
            loadReservations();
            setupSelectionListener();
        } catch (SQLException e) {
            showErrorAlert("Erreur de base de données", e.getMessage());
        }
    }

    private void loadReservations() throws SQLException {
        List<Reservation> reservations = reservationService.getAllReservations();
        reservationListView.getItems().clear();

        for (Reservation reservation : reservations) {
            // Obtenir le ticket associé à cette réservation
            Ticket ticket = reservationService.getTicketByReservation(reservation.getId());

            if (ticket != null) {
                HBox hBox = createReservationHBox(reservation, ticket);
                reservationListView.getItems().add(hBox);
            }
        }
    }

    private HBox createReservationHBox(Reservation reservation, Ticket ticket) {
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(10));

        Label etatLabel = new Label("État: " + reservation.getStatus());
        Label prixLabel = new Label("Prix: " + reservation.getPrix() + " TND");
        Label nbpLabel = new Label("Places: " + reservation.getNbPlaces());
        Label numLabel = new Label("Siège: " + ticket.getSeatNumber());

        hBox.getChildren().addAll(etatLabel, prixLabel, nbpLabel, numLabel);
        hBox.setUserData(reservation);

        return hBox;
    }

    private void setupSelectionListener() {
        reservationListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Reservation selectedReservation = (Reservation) newValue.getUserData();
                try {
                    Ticket ticket = reservationService.getTicketByReservation(selectedReservation.getId());
                    if (ticket != null) {
                        updateFields(selectedReservation, ticket);
                    }
                } catch (SQLException e) {
                    showErrorAlert("Erreur", "Impossible de récupérer les détails du ticket.");
                }
            } else {
                clearFields();
            }
        });
    }

    private void updateFields(Reservation reservation, Ticket ticket) {
        etatField.setText(reservation.getStatus());
        priceField.setText(String.valueOf(reservation.getPrix()));
        nbpField.setText(String.valueOf(reservation.getNbPlaces()));
        seatField.setText(ticket.getSeatNumber());
        codeField.setText(ticket.getTicketCode());
    }

    private void clearFields() {
        etatField.clear();
        priceField.clear();
        nbpField.clear();
        seatField.clear();
        codeField.clear();
    }

    @FXML
    public void deleteReservation(ActionEvent actionEvent) {
        String codeToDelete = codeField.getText();
        if (codeToDelete.isEmpty()) {
            showErrorAlert("Erreur", "Veuillez sélectionner une réservation à supprimer.");
            return;
        }

        try {
            reservationService.delete(codeToDelete);
            loadReservations();
            clearFields();
        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la suppression", e.getMessage());
        }
    }

    @FXML
    public void updateReservation(ActionEvent actionEvent) {
        String selectedCode = codeField.getText();
        if (selectedCode.isEmpty()) {
            showErrorAlert("Erreur", "Veuillez sélectionner une réservation à mettre à jour.");
            return;
        }

        String newEtat = etatField.getText();
        int newNbPlaces;
        double newPrix;
        String newSeatNumber = seatField.getText();

        try {
            newNbPlaces = Integer.parseInt(nbpField.getText());
            newPrix = Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur", "Veuillez entrer des valeurs valides pour les champs numériques.");
            return;
        }

        try {
            reservationService.update(selectedCode, newEtat, newNbPlaces, newPrix, newSeatNumber);
            loadReservations();
            Platform.runLater(() -> reservationListView.refresh());
        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la mise à jour", e.getMessage());
        }
    }

    @FXML
    public void deselect() {
        reservationListView.getSelectionModel().clearSelection();
        clearFields();
    }

    @FXML
    public void goToAjouterReservation(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterReservation.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // Méthodes de navigation
    @FXML
    public void showEvents(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) GoToEvents.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Nos événements");
        AfficherEventHOME controller = loader.getController();
        controller.showAllEvents();
    }

    @FXML
    public void goToCollabs(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipPartner.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) Collaborations.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void goToBoutique() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherProduit.fxml"));
        Parent root = loader.load();
        AfficherProduit controller = loader.getController();
        controller.loadProducts();
        Stage stage = (Stage) Boutique.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void showAcceuil(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) Acceuil.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Accueil");
        AfficherEventHOME controller = loader.getController();
        controller.showLastThreeEvents();
    }

    @FXML
    public void goToReclams(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamations.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) reclam.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToAjouterParticipation(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterParticipation.fxml"));
            Parent root = loader.load();
            AjouterParticipation controller = loader.getController();
            controller.setEvent(event);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Participation");
            stage.show();
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de l'ouverture de l'interface de participation.");
        }
    }
}