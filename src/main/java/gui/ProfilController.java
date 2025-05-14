package gui;

import gui.GestionEvents.AfficherEventHOME;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfilController {

    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    Scene scene;
    Stage stage;

    public void showEvents(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents(); // Call the method to display all events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) GoToEvents.getScene().getWindow();
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
        Stage stage = (Stage) Acceuil.getScene().getWindow();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AfficherReclamations.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) reclam.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }
}
