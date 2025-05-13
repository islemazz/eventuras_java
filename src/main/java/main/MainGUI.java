package main;
import java.io.IOException;
import java.time.LocalDateTime;

import entities.Partnership;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.PartnershipService;

public class MainGUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Ajouter Event");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Test the PartnershipService
            testPartnershipService();
        } catch (IOException e) {
            System.out.println("Erreur de l'application");
        }
    }

    private void testPartnershipService() {
        PartnershipService service = new PartnershipService();
        Partnership partnership = new Partnership();
        partnership.setPartnerId(1);
        partnership.setOrganizerId(1);
        partnership.setContractType("Standard");
        partnership.setDescription("Test partnership");
        partnership.setSigned(false);
        partnership.setStatus("Pending");
        partnership.setCreatedAt(LocalDateTime.now());
        partnership.setSignedContractFile(null);
        partnership.setSignedAt(null);

        Partnership created = service.create(partnership);
        if (created != null) {
            System.out.println("Partnership created: " + created);
        } else {
            System.out.println("Failed to create partnership");
        }
    }
}
