package gui.GestionReclam;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Reclamation;
import entities.ReclamationAttachment;
import services.ReclamationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class AjouterReclamation {

    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    public Button btnSubmit;
    @FXML
    private ComboBox<String> CBSuject;

    private final ReclamationService rs = new ReclamationService();

    @FXML
    private TextField TFDescription;

    @FXML
    private TextField TFId_user;

    @FXML
    private TextField TFId_event;

    private List<ReclamationAttachment> selectedAttachments = new ArrayList<>();

    @FXML
    private Button uploadButton;

    @FXML
    private ListView<ReclamationAttachment> attachmentListView;

    @FXML
    public void initialize() {
        // Populate combo box
        CBSuject.getItems().addAll("Organisateur", "Evenement", "Probleme technique");
        CBSuject.setValue("Sujet");

        // Setup the "Upload" button
        uploadButton.setOnAction(event -> selectFiles());

        // Custom cell factory so we can remove attachments
        attachmentListView.setCellFactory(param -> new ListCell<>() {
            private final HBox hbox = new HBox(10);
            private final Label fileNameLabel = new Label();
            private final Button removeButton = new Button("Retirer");

            {
                removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 12px;");
                removeButton.setOnAction(event -> {
                    ReclamationAttachment selectedAttachment = getItem();
                    if (selectedAttachment != null) {
                        attachmentListView.getItems().remove(selectedAttachment);
                        selectedAttachments.remove(selectedAttachment);
                        System.out.println("Removed: " + selectedAttachment.getFilePath());
                    }
                });
                hbox.getChildren().addAll(fileNameLabel, removeButton);
            }

            @Override
            protected void updateItem(ReclamationAttachment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    fileNameLabel.setText(item.getFilePath());
                    setGraphic(hbox);
                }
            }
        });
    }


    // FileChooser for selecting multiple files
    private void selectFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachments");

        // Allow multiple selection
        List<File> files = fileChooser.showOpenMultipleDialog(uploadButton.getScene().getWindow());
        if (files == null) return;

        // This is the folder in src/main/resources where we'll copy the files
        File destDir = new File("src/main/resources/ReclamAttachments");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        for (File file : files) {
            try {
                // Copy the file into ReclamAttachments
                File destFile = new File(destDir, file.getName());
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // We'll store the *relative path* from "src/main/resources" downward
                String relativePath = "ReclamAttachments/" + file.getName();

                // Or you could store the entire absolute path if you prefer
                // But you're already using relative in your example.
                ReclamationAttachment attachment = new ReclamationAttachment(0, 0, relativePath);

                selectedAttachments.add(attachment);
                attachmentListView.getItems().add(attachment);

                System.out.println("File copied to: " + destFile.getAbsolutePath() +
                        " -> Exists: " + destFile.exists());
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Erreur de upload de fichier: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    void ajouter(ActionEvent event) {
        // Collect user input
        int id_user = Integer.parseInt(TFId_user.getText());
        int id_event = Integer.parseInt(TFId_event.getText());
        String description = TFDescription.getText();
        String subject = CBSuject.getValue();

        // Current date/time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String created_at = now.format(formatter);

        // Create Reclamation with attachments
        Reclamation r = new Reclamation(id_user, id_event, created_at, subject, description, selectedAttachments);

        try {
            rs.create(r);
            System.out.println("Reclamation added successfully with attachments!");

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Succès");
            successAlert.setHeaderText("Réclamation ajoutée");
            successAlert.setContentText("Votre réclamation a été créée avec succès !");
            successAlert.showAndWait();

            // Refresh the reclamation display if we have a reference
            if (afficherReclamationsController != null) {
                afficherReclamationsController.refreshReclamationsDisplay();
            }

            // Close the window
            Stage stage = (Stage) CBSuject.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la création de la réclamation: " + e.getMessage());
            alert.showAndWait();
        }
    }
    private AfficherReclamations afficherReclamationsController;

    public void setAfficherReclamationsController(AfficherReclamations controller) {
        this.afficherReclamationsController = controller;
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
    }*/
}
