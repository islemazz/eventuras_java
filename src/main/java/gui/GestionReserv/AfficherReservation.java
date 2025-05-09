package gui.GestionReserv;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import entities.Reservation;
import entities.Ticket;
import services.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AfficherReservation {

    public Button ajouter;
    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    @FXML
    private ListView<HBox> Reservation;  // Une seule ListView
    @FXML
    private TextField code;
    @FXML
    private Button delete;
    private Connection connection;
    @FXML
    private Button updateButton;

    @FXML
    private TextField etatLabel;
    @FXML
    private TextField priceLabel;
    @FXML
    private TextField nbpLabel;
    @FXML
    private TextField seatLabel;

    private ObservableList<Reservation> allReservations = FXCollections.observableArrayList();

    private Service service = new Service();

    public void initialize() {
        try {
            loadReservationsAndTickets();
            setupSelectionListener();
        } catch (SQLException e) {
            showErrorAlert("Erreur de base de données", e.getMessage());
        }
    }


    private void loadReservationsAndTickets() throws SQLException {
        List<Reservation> reservations = service.getAllReservations();
        List<Ticket> tickets = service.getAllTickets();

        Reservation.getItems().clear();

        int size = Math.min(reservations.size(), tickets.size());

        for (int i = 0; i < size; i++) {
            Reservation reservation = reservations.get(i);
            Ticket ticket = tickets.get(i);

            HBox hBox = createSharedHBox(reservation, ticket);
            Reservation.getItems().add(hBox);
        }
    }

    private HBox createSharedHBox(Reservation reservation, Ticket ticket) {
        HBox hBox = new HBox(10);

        Label etatLabel = new Label("État: " + reservation.getStatus());
        Label prixLabel = new Label("Prix: " + reservation.getPrix() + " TND");
        Label nbpLabel = new Label("Nombre de places: " + reservation.getNbPlaces());
        Label numLabel = new Label("Siège: " + ticket.getSeatNumber());


        hBox.getChildren().addAll(etatLabel, prixLabel, nbpLabel,numLabel);
        hBox.setUserData(ticket);

        return hBox;
    }

    public Reservation getReservationFromTicket(Ticket ticket) throws SQLException {
        String ticketCode = ticket.getTicketCode();
        System.out.println("Ticket Code: " + ticketCode);

        Reservation reservation = service.getReservationWithTicketByTicketCode(ticketCode);
        if (reservation == null) {
            showErrorAlert("Erreur", "La réservation associée au ticket n'a pas été trouvée.");
            System.out.println("Aucune réservation trouvée pour le ticket Code: " + ticketCode);
        }
        return reservation;
    }



    private void setupSelectionListener() {

        Reservation.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Ticket selectedTicket = (Ticket) newValue.getUserData();
                Reservation selectedReservation = null;

                try {

                    selectedReservation = getReservationFromTicket(selectedTicket);
                } catch (SQLException e) {
                    showErrorAlert("Erreur", "Impossible de récupérer la réservation du ticket.");
                }


                if (selectedReservation != null) {

                    etatLabel.setText(selectedReservation.getStatus());
                    priceLabel.setText(String.valueOf(selectedReservation.getPrix()));
                    nbpLabel.setText(String.valueOf(selectedReservation.getNbPlaces()));
                    seatLabel.setText(String.valueOf(selectedTicket.getSeatNumber()));


                    code.setText(selectedTicket.getTicketCode());
                } else {

                    etatLabel.clear();
                    priceLabel.clear();
                    nbpLabel.clear();
                    seatLabel.clear();
                    code.clear();
                }
            } else {

                etatLabel.clear();
                priceLabel.clear();
                nbpLabel.clear();
                seatLabel.clear();
                code.clear();
            }
        });
    }



    @FXML
    public void delete(ActionEvent actionEvent) {
        String codeToDelete = code.getText();
        if (codeToDelete.isEmpty()) {
            showErrorAlert("Erreur", "Veuillez sélectionner un ticket à supprimer.");
            return;
        }

        try {
            service.delete(codeToDelete);
            loadReservationsAndTickets();
            code.clear();
        } catch (SQLException e) {
            showErrorAlert("Erreur lors de la suppression", e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void deselect() {
        Reservation.getSelectionModel().clearSelection();
        etatLabel.clear();
        priceLabel.clear();
        nbpLabel.clear();
        seatLabel.clear();
    }



    // Update UI components in the ListView
    private void updateTicketInListView(HBox hBox, Ticket ticket, String newEtat, int newNbPlaces, double newPrix, String newSeatNumber) {
        Label etatLabel = (Label) hBox.getChildren().get(0); // Ensure correct indexing
        etatLabel.setText("État: " + newEtat);

        Label nbpLabel = (Label) hBox.getChildren().get(1); // Ensure correct indexing
        nbpLabel.setText("Nombre de places: " + newNbPlaces);

        Label priceLabel = (Label) hBox.getChildren().get(2); // Ensure correct indexing
        priceLabel.setText("Prix: " + newPrix + " TND");

        Label seatLabel = (Label) hBox.getChildren().get(3); // Ensure correct indexing
        seatLabel.setText("Siège: " + newSeatNumber);
    }

    @FXML
    public void update(ActionEvent actionEvent) {
        String selectedCode = code.getText();
        if (selectedCode.isEmpty()) {
            showErrorAlert("Erreur", "Veuillez sélectionner un ticket à mettre à jour.");
            return;
        }


        String newEtat = etatLabel.getText();
        int newNbPlaces;
        double newPrix;
        String newSeatNumber = seatLabel.getText();

        // Handle invalid inputs
        try {
            newNbPlaces = Integer.parseInt(nbpLabel.getText());
            newPrix = Double.parseDouble(priceLabel.getText());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur", "Veuillez entrer des valeurs valides pour les champs numériques.");
            return;
        }


        for (HBox hBox : Reservation.getItems()) {
            Ticket selectedTicket = (Ticket) hBox.getUserData();
            if (selectedTicket.getTicketCode().equals(selectedCode)) {

                updateTicketInListView(hBox, selectedTicket, newEtat, newNbPlaces, newPrix, newSeatNumber);


                try {
                    service.update(selectedTicket.getTicketCode(), newEtat, newNbPlaces, newPrix, newSeatNumber);


                    loadReservationsAndTickets();

                    Platform.runLater(() -> Reservation.refresh());

                } catch (SQLException e) {
                    showErrorAlert("Erreur lors de la mise à jour", e.getMessage());
                }
                break;
            }
        }
    }

    public void goToAjouterReservation(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterReservation.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    /*
    public void showEvents(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents(); // Call the method to display all events

        // Switch to the AfficherEvent scene
        stage = (Stage) GoToEvents.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }

    //display last 3 events in the home section
    public void showAcceuil(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) Acceuil.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);

    }

    public void goToCollabs(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipPartner.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) Collaborations.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }

    public void goToTickets(ActionEvent event) throws IOException {

    }

    public void goToReclams(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamations.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) reclam.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }
*/

}