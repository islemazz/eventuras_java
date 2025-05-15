package gui.GestionPartner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import entities.Partner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.PartnerService;

public class ParticipantPartnerController {

    @FXML
    private VBox partnerContainer; // VBox pour contenir les partenaires

    @FXML
    private ScrollPane scrollPane; // ScrollPane pour permettre le défilement si la liste est longue

    @FXML
    private Button returnToDashboardButton;

    private final PartnerService partnerService = new PartnerService();

    @FXML
    public void initialize() {
        try {
            loadAllPartners(); // Charger tous les partenaires au démarrage
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", 
                "Erreur lors du chargement des partenaires : " + e.getMessage());
        }
    }

    // Méthode pour créer une carte de partenaire sous forme de VBox
    private VBox createPartnerCard(Partner partner) {
        VBox partnerBox = new VBox(10); // Espacement entre les éléments
        partnerBox.setStyle(
                "-fx-border-color: #cccccc; " + // Bordure grise
                        "-fx-border-radius: 10px; " + // Coins arrondis
                        "-fx-padding: 20px; " + // Espacement interne
                        "-fx-background-color: #f9f9f9; " + // Couleur de fond claire
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 5, 0, 0);" // Ombre légère
        );
        partnerBox.setAlignment(Pos.CENTER_LEFT); // Align content to the left for better readability
        partnerBox.setPrefWidth(600); 
        partnerBox.setMinWidth(VBox.USE_PREF_SIZE); 
        partnerBox.setMaxWidth(VBox.USE_PREF_SIZE);

        // Charger l'image du partenaire
        String imagePath = partner.getImagePath() != null ? partner.getImagePath().trim() : "";
        ImageView partnerImage = new ImageView();

        if (!imagePath.isEmpty()) {
            try {
                String fileName = new File(imagePath).getName();
                URL imageUrl = getClass().getResource("/Images/" + fileName);
                if (imageUrl != null) {
                    partnerImage.setImage(new Image(imageUrl.toExternalForm()));
                } else {
                    partnerImage.setImage(new Image(getClass().getResource("/Images/Delice.png").toExternalForm()));
                }
            } catch (Exception e) {
                partnerImage.setImage(new Image(getClass().getResource("/Images/Delice.png").toExternalForm()));
            }
        } else {
            partnerImage.setImage(new Image(getClass().getResource("/Images/Delice.png").toExternalForm()));
        }

        partnerImage.setFitWidth(250);
        partnerImage.setFitHeight(150);
        partnerImage.setPreserveRatio(true);
        
        VBox imageContainer = new VBox(partnerImage);
        imageContainer.setAlignment(Pos.CENTER); 
        imageContainer.setPadding(new Insets(0, 0, 10, 0)); 

        Text nameText = new Text(partner.getName());
        nameText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: #333333;");
        nameText.setWrappingWidth(560); 

        VBox textDetailsBox = new VBox(5); 
        textDetailsBox.setAlignment(Pos.CENTER_LEFT);
        textDetailsBox.getChildren().add(nameText);

        if (partner.getDescription() != null && !partner.getDescription().isEmpty()) {
            Text descriptionLabel = new Text("Description:");
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #555555;");
            Text descriptionText = new Text(partner.getDescription());
            descriptionText.setStyle("-fx-font-size: 14px; -fx-fill: #555555;");
            descriptionText.setWrappingWidth(560); 
            textDetailsBox.getChildren().addAll(descriptionLabel, descriptionText);
        }

        if (partner.getType() != null) {
            Text typeLabel = new Text("Type:");
            typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #555555;");
            Text typeText = new Text(partner.getType().toString());
            typeText.setStyle("-fx-font-size: 14px; -fx-fill: #555555;");
            textDetailsBox.getChildren().addAll(typeLabel, typeText);
        }
        
        if (partner.getEmail() != null && !partner.getEmail().isEmpty()) {
            Text emailLabel = new Text("Email:");
            emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #555555;");
            Text emailText = new Text(partner.getEmail());
            emailText.setStyle("-fx-font-size: 14px; -fx-fill: #555555;");
            textDetailsBox.getChildren().addAll(emailLabel, emailText);
        }

        if (partner.getPhone() != null && !partner.getPhone().isEmpty()) {
            Text phoneLabel = new Text("Phone:");
            phoneLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #555555;");
            Text phoneText = new Text(partner.getPhone());
            phoneText.setStyle("-fx-font-size: 14px; -fx-fill: #555555;");
            textDetailsBox.getChildren().addAll(phoneLabel, phoneText);
        }

        if (partner.getWebsite() != null && !partner.getWebsite().isEmpty()) {
            Text websiteLabel = new Text("Website:");
            websiteLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #555555;");
            Text websiteText = new Text(partner.getWebsite());
            websiteText.setStyle("-fx-font-size: 14px; -fx-fill: #007bff; -fx-underline: true;"); 
            websiteText.setOnMouseClicked(event -> {
                // Placeholder: System.out.println("Opening website: " + partner.getWebsite());
                // For a real app: Desktop.getDesktop().browse(new URI(partner.getWebsite()));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Website Link");
                alert.setHeaderText(null);
                alert.setContentText("Would open: " + partner.getWebsite());
                alert.showAndWait();
            });
            textDetailsBox.getChildren().addAll(websiteLabel, websiteText);
        }
        
        partnerBox.getChildren().addAll(imageContainer, textDetailsBox);

        return partnerBox;
    }

    // Méthode pour charger tous les partenaires
    private void loadAllPartners() throws SQLException {
        List<Partner> partners = partnerService.readAll();

        // Nettoyer le VBox avant d'ajouter les nouvelles cartes
        partnerContainer.getChildren().clear();

        for (Partner partner : partners) {
            VBox partnerCard = createPartnerCard(partner);
            partnerContainer.getChildren().add(partnerCard);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    Scene scene;
    Stage stage;

    //display last 3 events in the home sectio

    public void goToTickets(ActionEvent event) throws IOException {

    }

    @FXML
    private void handleReturnToDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/participantDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) returnToDashboardButton.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
