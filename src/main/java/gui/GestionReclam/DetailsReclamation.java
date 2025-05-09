package gui.GestionReclam;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import entities.Reclamation;
import entities.ReclamationAttachment;
import services.ReclamationService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ResourceBundle;

import javafx.scene.control.Button;
import javafx.scene.control.Label; // 🔹 Import Label

public class DetailsReclamation implements Initializable {

    @FXML
    private TextField idField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> CBSubject;

    @FXML
    private VBox attachmentsContainer;

    @FXML
    private ListView<HBox> textAttachmentsList; // Change ListView<String> -> ListView<HBox>

    @FXML
    private HBox imageContainer;

    private Reclamation currentReclamation;
    private final ReclamationService rs = new ReclamationService();

    private AfficherReclamations afficherReclamationsController;

    public void setAfficherReclamationsController(AfficherReclamations controller) {
        this.afficherReclamationsController = controller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate ComboBox with possible subjects
        CBSubject.getItems().addAll("Organisateur", "Evenement", "Probleme technique");

        // Debugging: Show borders for imageContainer to verify layout
        imageContainer.setStyle(

                " -fx-alignment: CENTER"
                );
    }

    public void setReclamationData(Reclamation reclamation) {
        this.currentReclamation = reclamation;

        idField.setText(String.valueOf(reclamation.getId()));
        descriptionField.setText(reclamation.getDescription());
        CBSubject.setValue(reclamation.getSubject());

        // Load attachments
        loadAttachments(reclamation.getAttachments());
    }

    private void loadAttachments(List<ReclamationAttachment> attachments) {
        textAttachmentsList.getItems().clear();
        imageContainer.getChildren().clear();

        boolean hasTextAttachments = false;

        for (ReclamationAttachment attachment : attachments) {
            File file = new File("src/main/resources/" + attachment.getFilePath());

            if (!file.exists()) {
                System.err.println("❌ ERROR: File does not exist -> " + file.getAbsolutePath());
                continue;
            }

            String fileExtension = getFileExtension(file.getName()).toLowerCase();

            if (fileExtension.matches("jpg|jpeg|png|gif")) {
                addImageAttachment(attachment, file);
            } else {
                addTextAttachment(attachment, file);
                hasTextAttachments = true;
            }
        }

        textAttachmentsList.setVisible(hasTextAttachments);
        textAttachmentsList.setManaged(hasTextAttachments);
    }


    /** 🔹 Adds a removable text attachment to ListView **/
    private void addTextAttachment(ReclamationAttachment attachment, File file) {
        Label fileNameLabel = new Label(file.getName());
        Button removeButton = new Button("Supprimer");

        removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 12px;");
        removeButton.setOnAction(event -> deleteAttachment(attachment));

        HBox hbox = new HBox(10); // Create HBox
        hbox.getChildren().addAll(fileNameLabel, removeButton); // 🔹 FIX: Proper method

        textAttachmentsList.getItems().add(hbox); // 🔹 Add HBox to ListView
    }

    /** 🔹 Adds a removable image attachment to HBox **/
    private void addImageAttachment(ReclamationAttachment attachment, File file) {
        ImageView imageView = createImageView(file);
        Button deleteButton = new Button("X");

        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 12px;");
        deleteButton.setOnAction(event -> deleteAttachment(attachment));

        VBox imageBox = new VBox(5, deleteButton, imageView);
        imageContainer.getChildren().add(imageBox);
    }

    /** 🔹 Handles attachment deletion **/
    private void deleteAttachment(ReclamationAttachment attachment) {
        try {
            rs.deleteAttachment(attachment.getId());

            // Refresh UI
            currentReclamation.getAttachments().removeIf(att -> att.getId() == attachment.getId());
            loadAttachments(currentReclamation.getAttachments());

            System.out.println("✅ Deleted attachment: " + attachment.getFilePath());

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to delete attachment: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private ImageView createImageView(File file) {
        try {
            String encodedUrl = file.toURI().toString().replace(" ", "%20");
            Image image = new Image(encodedUrl, 500, 500, true, true);

            if (image.isError()) {
                System.err.println("ERROR: Failed to load image -> " + file.getAbsolutePath());
                return new ImageView();
            }

            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);

            System.out.println("✅ Image added: " + file.getAbsolutePath());
            return imageView;
        } catch (Exception e) {
            System.err.println("EXCEPTION: Error loading image -> " + file.getAbsolutePath() + " | " + e.getMessage());
            return new ImageView();
        }
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return (idx == -1) ? "" : fileName.substring(idx + 1);
    }

    @FXML
    private void handleSubmit() {
        if (currentReclamation != null) {
            try {
                currentReclamation.setSubject(CBSubject.getValue());
                currentReclamation.setDescription(descriptionField.getText());

                rs.update(currentReclamation);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Reclamation Updated");
                alert.setContentText("The reclamation has been updated successfully.");
                alert.showAndWait();

                Stage stage = (Stage) idField.getScene().getWindow();
                stage.close();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Failed to update reclamation: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }


    @FXML
    private void handleDeleteReclamation() {
        if (currentReclamation != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Supprimer la réclamation ?");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

            ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.OK_DONE);
            ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmation.getButtonTypes().setAll(yesButton, noButton);

            confirmation.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    try {
                        rs.delete(currentReclamation.getId());

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Suppression réussie");
                        successAlert.setHeaderText("Réclamation supprimée");
                        successAlert.setContentText("La réclamation a été supprimée avec succès.");
                        successAlert.showAndWait();

                        // Close window FIRST
                        Stage stage = (Stage) idField.getScene().getWindow();
                        stage.close();

                        // Refresh the reclamation display if we have a reference
                        if (afficherReclamationsController != null) {
                            System.out.println("🔄 Refreshing reclamations after delete...");
                            afficherReclamationsController.refreshReclamationsDisplayAfterDelete();
                        } else {
                            System.err.println("❌ Error: afficherReclamationsController is NULL, cannot refresh!");
                        }

                    } catch (SQLException e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur");
                        errorAlert.setContentText("Échec de la suppression de la réclamation : " + e.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            });
        }
    }





    @FXML
    private void handleAddAttachment() {
        if (currentReclamation == null) {
            System.out.println("❌ No reclamation selected!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachment");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.txt", "*.docx")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            System.out.println("❌ No file selected!");
            return;
        }

        // Destination folder inside "src/main/resources/ReclamAttachments"
        File destDir = new File("src/main/resources/ReclamAttachments");
        if (!destDir.exists()) destDir.mkdirs();

        // Copy file to the destination
        File destFile = new File(destDir, selectedFile.getName());
        try {
            Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String relativePath = "ReclamAttachments/" + selectedFile.getName();

            // Save to database
            ReclamationAttachment newAttachment = new ReclamationAttachment(currentReclamation.getId(), relativePath);
            rs.addAttachmentToReclamation(newAttachment);

            // Refresh UI
            currentReclamation.getAttachments().add(newAttachment);
            loadAttachments(currentReclamation.getAttachments());

            System.out.println("✅ Attachment added: " + relativePath);
        } catch (IOException | SQLException e) {
            System.err.println("❌ Error adding attachment: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ajouter la pièce jointe: " + e.getMessage());
            alert.showAndWait();
        }
    }
  /*  public void showEvents(ActionEvent event) throws IOException {
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