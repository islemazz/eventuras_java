package gui.GestionEvents;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class EventORGAN {


    public Button GoToEvents1;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    public Button create;

    public void showEvents1(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) GoToEvents1.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);

    }

    public void showAcceuil1(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) Acceuil.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);

    }

    public void createEvent(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvent.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) create.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void goToCollab(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipPartner.fxml"));
        Parent root = loader.load();
        // Switch to the AfficherEvent scene
        Stage stage = (Stage) Collaborations.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void checkTickets(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipPartner.fxml"));
        Parent root = loader.load();
        // Switch to the AfficherEvent scene
        Stage stage = (Stage) tickets.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void checkReclams(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AfficherReclamations.fxml"));
        Parent root = loader.load();
        // Switch to the AfficherEvent scene
        Stage stage = (Stage) reclam.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);

    }
}
