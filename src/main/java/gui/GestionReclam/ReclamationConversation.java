package gui.GestionReclam;

import entities.ConversationMessage;
import entities.ConversationMessageAttachment;
import entities.Reclamation;
import gui.GestionUser.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.Reclamation.ConversationService;
import services.Reclamation.MessageService;
import services.Reclamation.ReclamationService;
import utils.Session;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ReclamationConversation {
    private Reclamation currentReclamation;

    private List<ConversationMessageAttachment> selectedAttachments = new ArrayList<>();

    @FXML
    private ListView<ConversationMessageAttachment> attachmentListView;


    @FXML private VBox messageContainer;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Button btnRetour; // Make sure it's declared
    @FXML private Button btnAttach;

    @FXML private Label reclamationSubject;
    @FXML private Label reclamationId;
    @FXML private Label reclamationDescription;



    private int conversationId;
    private MessageService messageService = new MessageService();
    private ConversationService conversationService = new ConversationService(); // ✅ Use ConversationService
    private final MessageService ms = new MessageService();



    public void setReclamationData(Reclamation reclamation) {
        this.currentReclamation = reclamation;

        // ✅ Fetch conversation ID
        this.conversationId = conversationService.getConversationIdForReclamation(reclamation.getId());

        // ✅ Update FXML fields
        reclamationId.setText("Réclamation N°'" + reclamation.getId() + "'");
        reclamationSubject.setText("Réclamation sur '" + reclamation.getSubject() + "'");
        reclamationDescription.setText("Description: " + reclamation.getDescription());

        // ✅ Check if status is "Résolu"
        if ("Résolu".equals(reclamation.getStatus())) {
            btnSatisfait.setVisible(false); // ✅ Hide the button
            txtMessage.setDisable(true); // ✅ Disable message input
            btnSend.setDisable(true); // ✅ Disable send button
        } else {
            btnSatisfait.setVisible(true); // ✅ Show button if not resolved
            txtMessage.setDisable(false); // ✅ Enable message input
            btnSend.setDisable(false); // ✅ Enable send button
        }

        // ✅ Load messages
        loadConversation();
    }


    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
        loadConversation();
    }

    @FXML
    private void initialize() {
        btnRetour.setOnAction(event -> goBack(event)); // ✅ Add event handler
        btnAttach.setOnAction(event -> selectFiles());
    }


@FXML
void sendMessage(ActionEvent event) {
    String messageText = txtMessage.getText().trim();
    if (messageText.isEmpty()) return;

    // ✅ Get the logged-in user ID from the session
    //int senderId = Session.getInstance().getCurrentUser().getId();

    UserSession session = UserSession.getInstance();
    int senderId = session.getId();

    // ✅ Current date/time
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String created_at = now.format(formatter);

    txtMessage.clear();

    // ✅ Create message with the logged-in user's ID as senderId
    ConversationMessage msg = new ConversationMessage(conversationId, senderId, messageText, created_at, selectedAttachments);

    try {
        ms.sendMessage(msg);
        System.out.println("Message added successfully with attachments!");

        loadConversation();
    } catch (SQLException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText("Erreur lors de l'envoi du message: " + e.getMessage());
        alert.showAndWait();
    }
}



    private void selectFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attachments");

        // Allow multiple selection
        List<File> files = fileChooser.showOpenMultipleDialog(btnAttach.getScene().getWindow());
        if (files == null) return;

        // This is the folder in src/main/resources where we'll copy the files
        File destDir = new File("src/main/resources/MessageAttachments");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        for (File file : files) {
            try {
                // Copy the file into ReclamAttachments
                File destFile = new File(destDir, file.getName());
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // We'll store the *relative path* from "src/main/resources" downward
                String relativePath = "MessageAttachments/" + file.getName();

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String uploaded_at = now.format(formatter);
                // Or you could store the entire absolute path if you prefer
                // But you're already using relative in your example.
                ConversationMessageAttachment attachment = new ConversationMessageAttachment(0, 0, relativePath,uploaded_at);

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



    @FXML private ScrollPane messageScrollPane; // ✅ Make sure it's declared

    public void loadConversation() {
        messageContainer.getChildren().clear();
        mediaContainer.getChildren().clear(); // ✅ Clear media container once

        try {
            List<ConversationMessage> messages = messageService.getMessages(conversationId);
            List<ConversationMessageAttachment> allImages = new ArrayList<>();

            for (ConversationMessage message : messages) {
                messageContainer.getChildren().add(createMessageBubble(message));

                // ✅ Collect images only
                for (ConversationMessageAttachment attachment : message.getAttachments()) {
                    if (isImage(attachment.getFilePath())) {
                        allImages.add(attachment);
                    }
                }
            }

            // ✅ Load only images into mediaContainer
            loadMediaAttachments(allImages);

            Platform.runLater(() -> messageScrollPane.setVvalue(1.0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private HBox createMessageBubble(ConversationMessage message) {
        VBox bubbleContent = new VBox();
        bubbleContent.setSpacing(10);
        bubbleContent.setPadding(new Insets(10));
        bubbleContent.getStyleClass().add("message-bubble");

        // ✅ Message Text
        Text messageText = new Text(message.getMessage());
        messageText.setWrappingWidth(250);
        TextFlow textFlow = new TextFlow(messageText);
        textFlow.setPadding(new Insets(5));
        bubbleContent.getChildren().add(textFlow);

        // ✅ Get the logged-in user's ID
        //int loggedInUserId = Session.getInstance().getCurrentUser().getId();
        UserSession session = UserSession.getInstance();
        int loggedInUserId = session.getId();


        int senderId = message.getSenderId();



        // 🔹 Debug: Print sender ID before deciding alignment
        System.out.println("📢 Comparing for UI -> Message ID: " + message.getId() + " | Sender ID: " + senderId + " | Logged-in User ID: " + loggedInUserId);

        // ✅ Attachments Handling (Including Images)
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            HBox attachmentsBox = new HBox();
            attachmentsBox.setSpacing(5);
            attachmentsBox.setAlignment(Pos.CENTER_LEFT);

            for (ConversationMessageAttachment attachment : message.getAttachments()) {
                String filePath = "src/main/resources/" + attachment.getFilePath();
                File file = new File(filePath);

                if (!file.exists()) {
                    System.out.println("❌ File not found: " + filePath);
                    continue;
                }

                if (isImage(filePath)) {
                    // ✅ Display images in a square
                    Image image = new Image(file.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);

                    // Add border around image
                    imageView.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

                    // Click to enlarge image
                    imageView.setOnMouseClicked(e -> openImagePopup(file));

                    attachmentsBox.getChildren().add(imageView);
                } else {
                    // ✅ Display non-image attachments as a text label
                    Label fileLabel = new Label(file.getName());
                    fileLabel.setStyle("-fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: blue;");

                    // Open file on click
                    fileLabel.setOnMouseClicked(e -> openFile(filePath));

                    attachmentsBox.getChildren().add(fileLabel);
                }
            }

            bubbleContent.getChildren().add(attachmentsBox);
        }

        // ✅ Message Bubble Container (Aligning Left or Right)
        HBox container = new HBox(bubbleContent);
        container.setSpacing(10);
        container.setPadding(new Insets(5));

        if (senderId == loggedInUserId) {
            System.out.println("✅ Aligning message left (Sent by logged-in user)");
            container.setAlignment(Pos.CENTER_LEFT);
            bubbleContent.getStyleClass().add("message-left");

            // ✅ Allow clicking only if the logged-in user sent the message
            bubbleContent.setOnMouseClicked(event -> openMessageDetails(message));
        } else {
            System.out.println("⬅ Aligning message right (Sent by someone else)");
            container.setAlignment(Pos.CENTER_RIGHT);
            bubbleContent.getStyleClass().add("message-right");

            // ❌ Disable clicking for messages not sent by the logged-in user
            bubbleContent.setOnMouseClicked(null);
        }

        return container;
    }



    private void openMessageDetails(ConversationMessage message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/MessageDetails.fxml"));
            Parent root = loader.load();

            // ✅ Get the controller and pass the message data
            DetailsMessage controller = loader.getController();
            controller.setMessageData(message);

            // ✅ Pass the current `ReclamationConversation` controller for refreshing messages
            controller.setReclamationConversationController(this);

            // ✅ Show as a new popup window
            Stage stage = new Stage();
            stage.setTitle("Message Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to open message details: " + e.getMessage());
            alert.showAndWait();
        }
    }





    private boolean isImage(String filePath) {
        String lowerCasePath = filePath.toLowerCase();
        return lowerCasePath.endsWith(".png") || lowerCasePath.endsWith(".jpg") ||
                lowerCasePath.endsWith(".jpeg") || lowerCasePath.endsWith(".gif");
    }

    private void openImagePopup(File file) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Image Preview");

        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);
        imageView.setFitHeight(600);

        VBox vbox = new VBox(imageView);
        vbox.setStyle("-fx-background-color: white; -fx-padding: 10;");

        Scene scene = new Scene(vbox);
        popupStage.setScene(scene);
        popupStage.show();
    }



    private void openFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Cannot open file: " + filePath);
                alert.showAndWait();
            }
        }
    }


    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AfficherReclamationsAdmin.fxml")); // ✅ Change to your previous FXML file
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible de retourner en arrière !");
            alert.showAndWait();
        }
    }



@FXML
private GridPane mediaContainer ;


    private void loadMediaAttachments(List<ConversationMessageAttachment> attachments) {
        mediaContainer.getChildren().clear(); // ✅ Clear before adding images

        int column = 0;
        int row = 0;

        for (ConversationMessageAttachment attachment : attachments) {
            File file = new File("src/main/resources/" + attachment.getFilePath());

            if (!file.exists()) {
                System.err.println("❌ ERROR: File does not exist -> " + file.getAbsolutePath());
                continue;
            }

            // ✅ Process only images
            if (isImage(file.getAbsolutePath())) {
                ImageView imageView = createImageView(file);
                mediaContainer.add(imageView, column, row); // ✅ Add image to grid

                column++;
                if (column == 3) { // ✅ Move to next row after 3 images
                    column = 0;
                    row++;
                }
            }
        }
    }

    /** 🔹 Utility: Create ImageView with Styling **/
    private ImageView createImageView(File file) {
        try {
            Image image = new Image(file.toURI().toString(), 100, 100, true, true);

            if (image.isError()) {
                System.err.println("ERROR: Failed to load image -> " + file.getAbsolutePath());
                return new ImageView();
            }

            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(80);
            imageView.setFitHeight(80);

            // Set Click Event to Open Enlarged View
            imageView.setOnMouseClicked(event -> openImagePopup(file));

            return imageView;
        } catch (Exception e) {
            System.err.println("EXCEPTION: Error loading image -> " + file.getAbsolutePath() + " | " + e.getMessage());
            return new ImageView();
        }
    }

    /** 🔹 Utility: Get File Extension from a Filename **/
    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return (idx == -1) ? "" : fileName.substring(idx + 1);
    }



    private final ReclamationService rs = new ReclamationService();


    @FXML private Button btnSatisfait;
    @FXML
    private void handleSatisfied() {
        // ✅ Update the status to "Résolu"
        changeReclamationStatus("Résolu");

        // ✅ Hide the satisfied button and disable message input
        Platform.runLater(() -> {
            btnSatisfait.setVisible(false);
            btnSatisfait.setManaged(false);
            txtMessage.setDisable(true);
            btnSend.setDisable(true);


        });

        // ✅ Reload the conversation to reflect the changes
        loadConversation();
    }


    private void changeReclamationStatus(String newStatus) {
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
                rs.updateReclamationStatus(currentReclamation.getId(), newStatus,null);


                currentReclamation.setStatus(newStatus);

                // ✅ Show success message in French
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le status de la réclamation a été mis à jour avec succès ! ");
                alert.showAndWait();


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

