package gui.GestionProduit;

import entities.*;
import gui.GestionEvents.AfficherEventHOME;
import gui.GestionEvents.AjouterParticipation;
import gui.GestionPartner.PartnershipCellController;
import gui.GestionReclam.AfficherReclamations;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import services.ProduitService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import services.ServiceEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

public class AjouterProduit {

    @FXML
    private TextField NameField;
    @FXML
    private TextField DescriptionField;
    @FXML
    private TextField PriceField;
    @FXML
    private TextField QuantityField;
    @FXML
    private Button ImageB;
    @FXML
    private Button btnsubmit;

    public Button GoToEvents;
    public Button Collaborations;
    public Button Acceuil;
    public Button reclam;

    public Button Boutique;

    public VBox eventContainer;

    private File selectedImageFile;
    private final ProduitService produitService = new ProduitService();
    public final ServiceEvent sE=new ServiceEvent();

    @FXML
    public void initialize() {
        // Ajout d'un gestionnaire d'événements pour l'importation d'image
        ImageB.setOnAction(this::handleImportImage);
        btnsubmit.setOnAction(this::handleAddAndRedirect);
    }

    private void handleAddAndRedirect(ActionEvent event) {
        if (addProduit()) {
            redirectToShop(event);
        }
    }

    @FXML
    private boolean addProduit() {
        try {
            String nom = NameField.getText().trim();
            String description = DescriptionField.getText().trim();
            double prix = Double.parseDouble(PriceField.getText().trim());
            int quantite = Integer.parseInt(QuantityField.getText().trim());

            // Vérification si des champs sont vides
            if (nom.isEmpty() || description.isEmpty() || selectedImageFile == null) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs et importer une image.");
                return false;
            }

            String imagePath = selectedImageFile.toURI().toString(); // On récupère le chemin de l'image
            Produit newProduit = new Produit(0, nom, description, prix, quantite, imagePath);
            produitService.create(newProduit); // Ajouter le produit dans la base de données

            return true;

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Prix ou quantité invalide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Échec lors de l'ajout : " + e.getMessage());
        }
        return false;
    }

    @FXML
    public void handleImportImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // On garde la référence du fichier sélectionné
            selectedImageFile = file;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToShop(ActionEvent event) {
        try {
            // Chargement de la page de la boutique après ajout du produit
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherProduit.fxml"));
            Parent shopRoot = loader.load();

            // Récupérer le contrôleur de la page de la boutique pour rafraîchir les produits
            AfficherProduit afficherProduitController = loader.getController();
            afficherProduitController.loadProducts();  // Rafraîchir les produits après l'ajout

            Stage stage = (Stage) btnsubmit.getScene().getWindow();
            stage.setScene(new Scene(shopRoot));
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la boutique.");
        }
    }

    @FXML
    public void goToBoutique() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherProduit.fxml"));
            Parent root = loader.load();

            // Facultatif : Appelle une méthode dans le contrôleur si besoin
            AfficherProduit controller = loader.getController();
            controller.loadProducts(); // Appelle ta méthode d'initialisation si elle existe

            // Changement de scène
            Stage stage = (Stage) Boutique.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Optionnel : alerte utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger la boutique");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    public void showEvents(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("AfficherEventHOME.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        Stage stage = (Stage) GoToEvents.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Nos évenements");
        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents(); // Call the method to display all events
    }

    public void goToCollabs(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipPartner.fxml"));
            Parent root = loader.load();

            // Facultatif : Appelle une méthode dans le contrôleur si besoin
            PartnershipCellController controller = loader.getController();


            //controller.initializeCell(Partner p, Partnership ps); // Appelle ta méthode d'initialisation si elle existe

            // Changement de scène
            Stage stage = (Stage)  Collaborations.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Optionnel : alerte utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger la liste");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void showAcceuil(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("AfficherEventHOME.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        Stage stage = (Stage) Acceuil.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Acceuil");
        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents(); // Call the method to display all events
    }
    private void navigateToAjouterParticipation(Event event) {
        try {
            // Load the AjouterParticipation FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterParticipation.fxml"));
            Parent root = loader.load();

            // Get the AjouterParticipation controller
            AjouterParticipation controller = loader.getController();

            // Pass the selected event to the AjouterParticipation controller
            controller.setEvent(event);

            // Create a new stage for the AjouterParticipation interface
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Participation");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de l'interface de participation.");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    private void loadAllEvents() {
        try {
            ArrayList<Event> events = sE.afficherAll(); // Fetch all events from the service
            System.out.println("Number of events fetched: " + events.size()); // Debug: Check the number of events

            ObservableList<Event> observableList = FXCollections.observableList(events);
            eventContainer.getChildren().clear(); // Clear the container before adding new cards
            eventContainer.setSpacing(20); // Add spacing between event cards

            for (Event event : observableList) {
                System.out.println("Adding event: " + event.getTitle()); // Debug: Check each event being added

                VBox eventCard = new VBox(10);
                eventCard.setPadding(new Insets(15));
                eventCard.setAlignment(Pos.CENTER);
                eventCard.setStyle("-fx-border-color: #9E9E9E; -fx-border-radius: 10px; -fx-background-color: rgba(255,255,255,0.45); -fx-padding: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

                // Load event image
                String imagePath = event.getImage() != null ? event.getImage().trim() : "";
                ImageView eventImage = new ImageView();

                if (!imagePath.isEmpty()) {
                    try {
                        String fileName = new File(imagePath).getName();
                        System.out.println("Extracted filename: " + fileName);

                        URL imageUrl = getClass().getResource("/ImagesEvents/" + fileName);
                        if (imageUrl != null) {
                            System.out.println("Loading image from: " + imageUrl.toExternalForm());
                            eventImage.setImage(new Image(imageUrl.toExternalForm()));
                        } else {
                            System.out.println("Image not found: " + fileName);
                            eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
                    }
                } else {
                    System.out.println("Image path is empty, using default image.");
                    eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
                }
                eventImage.setFitWidth(300);
                eventImage.setFitHeight(250);
                eventImage.setPreserveRatio(true);

                // Event details
                Text titleText = new Text(event.getTitle());
                titleText.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-fill: #1A237E;");
                Text descriptionText = new Text(event.getDescription());
                descriptionText.setStyle("-fx-font-size: 16px; -fx-fill: white;");
                Text dateText = new Text(event.getDate_event().toString());
                dateText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #1A237E;");

                // Add a "Participer" button
                Button participerButton = new Button("Participer");
                participerButton.setStyle("-fx-background-color: #1A237E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px;");
                participerButton.setOnAction(e -> navigateToAjouterParticipation(event));

                // Add elements to the event card
                eventCard.getChildren().addAll(eventImage, titleText, descriptionText, dateText,participerButton);
                eventContainer.getChildren().add(eventCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createEventCardForFilter(Event event) {
        VBox eventCard = new VBox(10);
        eventCard.setPadding(new Insets(15));
        eventCard.setAlignment(Pos.CENTER);
        eventCard.setStyle("-fx-border-color: #9E9E9E; -fx-border-radius: 10px; -fx-background-color: rgba(255,255,255,0.45); -fx-padding: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        // Load event image
        String imagePath = event.getImage() != null ? event.getImage().trim() : "";
        ImageView eventImage = new ImageView();

        if (!imagePath.isEmpty()) {
            try {
                String fileName = new File(imagePath).getName();
                URL imageUrl = getClass().getResource("/ImagesEvents/" + fileName);
                if (imageUrl != null) {
                    eventImage.setImage(new Image(imageUrl.toExternalForm()));
                } else {
                    eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
            }
        } else {
            eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
        }
        eventImage.setFitWidth(300);
        eventImage.setFitHeight(250);
        eventImage.setPreserveRatio(true);

        // Event details
        Text titleText = new Text(event.getTitle());
        titleText.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-fill: #1A237E;");
        Text descriptionText = new Text(event.getDescription());
        descriptionText.setStyle("-fx-font-size: 16px; -fx-fill: white;");
        Text dateText = new Text(event.getDate_event().toString());
        dateText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-fill: #1A237E;");

        // Add a "Participer" button
        Button participerButton = new Button("Participer");
        participerButton.setStyle("-fx-background-color: #1A237E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-border-radius: 5px;");
        participerButton.setOnAction(e -> navigateToAjouterParticipation(event));

        // Add elements to the event card
        eventCard.getChildren().addAll(eventImage, titleText, descriptionText, dateText,participerButton);
        return eventCard;
    }
    public void goToReclams(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamations.fxml"));
            Parent root = loader.load();

            // Facultatif : Appelle une méthode dans le contrôleur si besoin
            AfficherReclamations controller = loader.getController();
            Reclamation rec = new Reclamation();
            controller.createReclamationCard(rec) ; // Appelle ta méthode d'initialisation si elle existe

            // Changement de scène
            Stage stage = (Stage) reclam.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Optionnel : alerte utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger la liste");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void filterEvents(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAllEvents(); // If the search bar is empty, show all events
            return;
        }

        try {
            ArrayList<Event> allEvents = sE.afficherAll(); // Fetch all events from the service
            ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

            String lowerCaseQuery = query.toLowerCase();
            for (Event event : allEvents) {
                if (event.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        event.getLocation().toLowerCase().contains(lowerCaseQuery)) {
                    filteredEvents.add(event);
                }
            }

            eventContainer.getChildren().clear(); // Clear the container before adding filtered cards
            for (Event event : filteredEvents) {
                VBox eventCard = createEventCardForFilter(event);
                eventContainer.getChildren().add(eventCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
