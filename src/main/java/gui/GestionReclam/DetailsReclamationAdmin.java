package gui.GestionReclam;


import entities.Reclamation;
import entities.ReclamationAttachment;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.Reclamation.ConversationService;
import services.Reclamation.EmailJSService;
import services.Reclamation.ReclamationService;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class DetailsReclamationAdmin implements Initializable {
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

    @FXML
    private Label refusalReasonLabel;

    private Reclamation currentReclamation;
    private final ReclamationService rs = new ReclamationService();

    private AfficherReclamationsAdmin afficherReclamationsControllerAdmin;

    public void setAfficherReclamationsController(AfficherReclamationsAdmin controller) {
        this.afficherReclamationsControllerAdmin = controller;
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
        System.out.println("subj"+reclamation.getSubject());
        System.out.println("refuse reason"+reclamation.getRefuseReason());

        // ✅ Load attachments
        loadAttachments(reclamation.getAttachments());

        // ✅ Show buttons only if status is "En attente"
        if ("En attente".equals(reclamation.getStatus())) {
            btnAccepter.setVisible(true);
            btnAccepter.setManaged(true);
            btnRefuser.setVisible(true);
            btnRefuser.setManaged(true);
            refusalReasonLabel.setVisible(false);
        } else {
            btnAccepter.setVisible(false);
            btnAccepter.setManaged(false);
            btnRefuser.setVisible(false);
            btnRefuser.setManaged(false);
        }

        // ✅ Show refusal reason if status is "Refusé"
        if ("Rejeté".equals(reclamation.getStatus())) {
            refusalReasonLabel.setText("Raison de refut: " + reclamation.getRefuseReason());
            System.out.println("reason;" + reclamation.getRefuseReason());
            refusalReasonLabel.setVisible(true);
        } else {
            refusalReasonLabel.setVisible(false);
        }
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


    /** 🔹 Adds a text attachment to ListView (Without "Supprimer" Button) **/
    private void addTextAttachment(ReclamationAttachment attachment, File file) {
        Label fileNameLabel = new Label(file.getName());

        // Create Download Button
        Button downloadButton = new Button("Download");
        downloadButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 12px;");
        downloadButton.setOnAction(event -> downloadFile(file));

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(fileNameLabel, downloadButton); // Add both label and button

        textAttachmentsList.getItems().add(hbox);
    }

    /** 🔹 Adds an image attachment to HBox (Without "X" Button) **/
    private void addImageAttachment(ReclamationAttachment attachment, File file) {
        ImageView imageView = createImageView(file);

        VBox imageBox = new VBox(5, imageView); // Remove delete button
        // Set Click Event to Open Enlarged View
        imageView.setOnMouseClicked(event -> openImagePopup(file));

        imageContainer.getChildren().add(imageBox);
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


    /** 🔹 Opens Image in a New Stage (Popup) **/
    private void openImagePopup(File file) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Image Preview");

        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600); // Set larger width
        imageView.setFitHeight(600); // Set larger height

        VBox vbox = new VBox(imageView);
        vbox.setStyle("-fx-background-color: white; -fx-padding: 10;");

        Scene scene = new Scene(vbox);
        popupStage.setScene(scene);
        popupStage.show();
    }

    private void downloadFile(File file) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As");
        fileChooser.setInitialFileName(file.getName());

        // Set default directory (optional)
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Show save dialog
        File destination = fileChooser.showSaveDialog(null);
        if (destination != null) {
            try {
                Files.copy(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ File downloaded: " + destination.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Download Success");
                alert.setHeaderText(null);
                alert.setContentText("File downloaded successfully!");
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Download Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to download file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }


    @FXML
    private Button btnAccepter;

    @FXML
    private Button btnRefuser;

    private EmailJSService mailingService;

    @FXML
    private void handleAccepter() {
        if (currentReclamation == null) {
            System.out.println("❌ Aucune réclamation sélectionnée.");
            return;
        }

        try {
            // ✅ Ensure we are getting the correct user ID
            int reclamationCreatorId = currentReclamation.getId_user();
            System.out.println("🟢 Correct User ID for Reclamation: " + reclamationCreatorId);

            // ✅ Fetch the correct user info
            Map<String, String> userInfo = rs.getUserInfoById(reclamationCreatorId);

            if (userInfo.isEmpty()) {
                System.out.println("❌ Error: No user data found for ID: " + reclamationCreatorId);
                return;
            }

            String userName = userInfo.get("name");
            String userEmail = userInfo.get("email");

            if (userEmail == null || userEmail.isEmpty()) {
                System.out.println("❌ Error: User email is empty or null.");
                return;
            }

            // ✅ Send email to the correct user
            System.out.println("📧 Sending email to: " + userEmail + " | Name: " + userName);
            mailingService.sendEmail("accepted", userName, userEmail);


            // ✅ Update reclamation status
            changeReclamationStatus("En cours", null, true);
            System.out.println("✅ Email successfully sent to: " + userEmail);

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la récupération des informations de l'utilisateur: " + e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML

    private void handleRefuser() {
        if (currentReclamation == null) {
            System.out.println("❌ Aucune réclamation sélectionnée.");
            return;
        }

        // ✅ Show input dialog for refusal reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Refuser la réclamation");
        dialog.setHeaderText("Veuillez spécifier la raison du refus.");
        dialog.setContentText("Raison du refus :");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String refuseReason = result.get().trim();

            try {
                // ✅ Ensure we are getting the correct user ID
                int reclamationCreatorId = currentReclamation.getId_user();
                System.out.println("🟢 Correct User ID for Reclamation: " + reclamationCreatorId);

                // ✅ Fetch the correct user info
                Map<String, String> userInfo = rs.getUserInfoById(reclamationCreatorId);

                if (userInfo.isEmpty()) {
                    System.out.println("❌ Error: No user data found for ID: " + reclamationCreatorId);
                    return;
                }

                String userName = userInfo.get("name");
                String userEmail = userInfo.get("email");

                if (userEmail == null || userEmail.isEmpty()) {
                    System.out.println("❌ Error: User email is empty or null.");
                    return;
                }

                // ✅ Send rejection email to the correct user
                System.out.println("📧 Sending rejection email to: " + userEmail + " | Name: " + userName);
                mailingService.sendEmail("declined " , userName, userEmail);

                // ✅ Update reclamation status
                changeReclamationStatus("Rejeté", refuseReason, false);
                System.out.println("✅ Rejection email successfully sent to: " + userEmail);

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors de la récupération des informations de l'utilisateur: " + e.getMessage());
                alert.showAndWait();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Action annulée");
            alert.setHeaderText(null);
            alert.setContentText("Vous devez spécifier une raison pour refuser la réclamation.");
            alert.showAndWait();
        }
    }
    private void changeReclamationStatus(String newStatus, String refuseReason, boolean createConversation) {
        if (currentReclamation == null) {
            System.out.println("❌ Aucune réclamation sélectionnée.");
            return;
        }

        // ✅ Show a confirmation dialog before updating the status
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Le status de la réclamation changera à \"" + newStatus + "\" , continuez ?");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // ✅ Update reclamation status
                rs.updateReclamationStatus(currentReclamation.getId(), newStatus, refuseReason);

                // ✅ Create conversation if required
                if (createConversation) {
                    ConversationService cs = new ConversationService();
                    cs.createConversation(currentReclamation.getId());
                    System.out.println("🗨️ Conversation créée pour la réclamation #" + currentReclamation.getId());
                }

                currentReclamation.setStatus(newStatus);
                currentReclamation.setRefuseReason(refuseReason);

                // ✅ Show success message in French
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le status de la réclamation a été mis à jour avec succès ! ");
                alert.showAndWait();

                // Refresh the reclamation list if available
                if (afficherReclamationsControllerAdmin != null) {
                    afficherReclamationsControllerAdmin.refreshReclamationsDisplay();
                }

                // Optionally close the window
                Stage stage = (Stage) btnAccepter.getScene().getWindow();
                stage.close();

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Échec de la mise à jour du status : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

}