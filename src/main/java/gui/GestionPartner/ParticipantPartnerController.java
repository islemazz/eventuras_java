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
        partnerBox.setAlignment(Pos.CENTER);
        partnerBox.setPrefWidth(600); // Largeur fixe

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

        // Ajuster la taille de l'image
        partnerImage.setFitWidth(250);
        partnerImage.setFitHeight(150);
        partnerImage.setPreserveRatio(true);

        // Ajouter le nom du partenaire
        Text nameText = new Text(partner.getName());
        nameText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #333333;");

        // Ajouter l'image et le texte à la carte du partenaire
        partnerBox.getChildren().addAll(partnerImage, nameText);

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
