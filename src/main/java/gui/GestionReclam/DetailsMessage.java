package gui.GestionReclam;

import entities.ConversationMessage;
import entities.ConversationMessageAttachment;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.Reclamation.MessageService;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


public class DetailsMessage implements Initializable {
    @FXML
    private TextField idField;

    @FXML
    private TextArea contentField;

    @FXML
    private VBox attachmentsContainer;

    @FXML
    private ListView<HBox> textAttachmentsList;

    @FXML
    private HBox imageContainer;


    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;



    private ConversationMessage conversationMessage;

    private ReclamationConversation reclamationConversationController;

    public void setReclamationConversationController(ReclamationConversation controller) {
        this.reclamationConversationController = controller;
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate ComboBox with possible subjects

        // Debugging: Show borders for imageContainer to verify layout
        imageContainer.setStyle(

                " -fx-alignment: CENTER"
        );
    }

    public void setMessageData(ConversationMessage conversationMessage) {
        this.conversationMessage = conversationMessage;

        idField.setText(String.valueOf(conversationMessage.getId()));
        contentField.setText(conversationMessage.getMessage());

        // Load attachments
        loadAttachments(conversationMessage.getAttachments());
    }



    private void loadAttachments(List<ConversationMessageAttachment> attachments) {
        textAttachmentsList.getItems().clear();
        imageContainer.getChildren().clear();

        boolean hasTextAttachments = false;

        for (ConversationMessageAttachment attachment : attachments) {
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


    @FXML
    private void handleSupprimerMessage() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Message");
        alert.setHeaderText("Are you sure you want to delete this message?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MessageService messageService = new MessageService();
                messageService.deleteMessage(conversationMessage.getId());

                // ✅ Refresh messages in conversation view after deletion
                if (reclamationConversationController != null) {
                    reclamationConversationController.loadConversation();
                }

                // ✅ Close the details window
                Stage stage = (Stage) btnSupprimer.getScene().getWindow();
                stage.close();

            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setContentText("Failed to delete message: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }



    @FXML
    private void handleModifierMessage() {
        String newMessage = contentField.getText().trim();

        if (newMessage.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText("Message cannot be empty!");
            alert.showAndWait();
            return;
        }

        try {
            MessageService messageService = new MessageService();
            messageService.updateMessage(conversationMessage.getId(), newMessage);

            // ✅ Update local message object
            conversationMessage.setMessage(newMessage);

            // ✅ Notify the main controller to refresh messages
            if (reclamationConversationController != null) {
                reclamationConversationController.loadConversation();
            }

            // ✅ Close the popup after saving changes
            Stage stage = (Stage) btnModifier.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Failed to update message: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void addTextAttachment(ConversationMessageAttachment attachment, File file) {
        Label fileNameLabel = new Label(file.getName());

        // Create Download Button
        Button downloadButton = new Button("Download");
        downloadButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 12px;");

        // Create Delete Button
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 12px;");

        // Attach delete action
        deleteButton.setOnAction(event -> deleteAttachment(attachment.getId(), file, true));

        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(fileNameLabel, downloadButton, deleteButton); // Add all elements

        textAttachmentsList.getItems().add(hbox);
    }


    /** 🔹 Adds an image attachment to HBox (With "X" Button) **/
    private void addImageAttachment(ConversationMessageAttachment attachment, File file) {
        ImageView imageView = createImageView(file);

        // Create Delete Button (X)
        Button deleteButton = new Button("X");
        deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Attach delete action
        deleteButton.setOnAction(event -> deleteAttachment(attachment.getId(), file, false));

        // Position delete button in top-right corner
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(imageView, deleteButton);
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);

        VBox imageBox = new VBox(5, stackPane);
        imageContainer.getChildren().add(imageBox);
    }


    /** 🔹 Deletes an attachment and removes it from UI **/
    /** 🔹 Deletes an attachment and removes it from UI **/
    private void deleteAttachment(int attachmentId, File file, boolean isTextAttachment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Attachment");
        alert.setHeaderText("Are you sure you want to delete this attachment?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MessageService messageService = new MessageService();
                messageService.deleteAttachment(attachmentId);

                // ✅ Remove ONLY the selected image
                if (isTextAttachment) {
                    textAttachmentsList.getItems().removeIf(hbox ->
                            ((Label) hbox.getChildren().get(0)).getText().equals(file.getName())
                    );
                } else {
                    imageContainer.getChildren().removeIf(vbox -> {
                        StackPane stackPane = (StackPane) ((VBox) vbox).getChildren().get(0);
                        ImageView imageView = (ImageView) stackPane.getChildren().get(0);
                        return imageView.getImage().getUrl().equals(file.toURI().toString());
                    });
                }

                // ✅ Delete the physical file (if necessary)
                if (file.exists()) {
                    file.delete();
                }

                System.out.println("✅ Attachment deleted: " + file.getAbsolutePath());

                // ✅ Refresh the conversation after deleting the attachment
                refreshConversation();

            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setContentText("Failed to delete attachment: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }
    private void refreshConversation() {
        if (reclamationConversationController != null) {
            reclamationConversationController.loadConversation();
        }
    }








}