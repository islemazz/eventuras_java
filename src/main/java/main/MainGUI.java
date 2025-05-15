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

        } catch (IOException e) {
            System.err.println("Erreur de l'application (IOException):");
            e.printStackTrace();
            System.out.println("Erreur de l'application : " + e.getMessage());


            e.printStackTrace();
        }
    }

}
