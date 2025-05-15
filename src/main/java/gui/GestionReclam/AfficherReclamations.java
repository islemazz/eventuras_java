package gui.GestionReclam;

import entities.Event;
import gui.GestionEvents.AfficherEventHOME;
import gui.GestionEvents.AjouterParticipation;
import gui.GestionProduit.AfficherProduit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import entities.Reclamation;
import services.ReclamationService;
import services.ServiceEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class AfficherReclamations {

    private final ReclamationService rs = new ReclamationService();
    public Button GoToEvents;
    public Button Collaborations;
    public Button Acceuil;
    public Button tickets;
    public Button reclam;
    public Button adminpage;


    public Button Boutique;

    public VBox eventContainer;

    public final ServiceEvent sE=new ServiceEvent();
    
    @FXML
    private GridPane reclamationsGrid; // GridPane to hold all cards

    @FXML
    void initialize() {
        refreshReclamationsDisplay();
    }

    @FXML
    public VBox createReclamationCard(Reclamation rec) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPrefWidth(200);

        Label ticketNumber = new Label("Ticket #" + rec.getId());
        ticketNumber.getStyleClass().add("ticket-number");

        Label ticketDate = new Label("Créé à: " + rec.getCreated_at());
        ticketDate.getStyleClass().add("ticket-date");

        Label ticketDescription = new Label("Description: " + rec.getDescription());
        ticketDescription.getStyleClass().add("ticket-description");

        // Action Buttons (hidden by default)
        HBox actionButtons = new HBox(10);
        Button editButton = new Button("Modifier");
        Button detailsButton = new Button("Détails");

        editButton.getStyleClass().add("modifier-btn");
        detailsButton.getStyleClass().add("details-btn");

        actionButtons.getChildren().addAll(editButton, detailsButton);
        actionButtons.setOpacity(0);

        // Show the buttons on hover
        card.setOnMouseEntered(event -> actionButtons.setOpacity(1));
        card.setOnMouseExited(event -> actionButtons.setOpacity(0));

        // "Modifier" leads to the details page
        editButton.setOnAction(event -> goToEditPage(event, rec));
        // Alternatively, "Détails" can do the same or different

        card.getChildren().addAll(ticketNumber, ticketDate, ticketDescription, actionButtons);
        return card;
    }

    public void goToAjouterPage(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterReclamation.fxml"));
            Parent root = loader.load();

            // Get the controller
            AjouterReclamation controller = loader.getController();
            // Pass reference of this controller so we can refresh after adding
            controller.setAfficherReclamationsController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter Réclamation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Loading Error");
            alert.setContentText("Error while loading add reclamation: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void goToEditPage(javafx.event.ActionEvent event, Reclamation rec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsReclamation.fxml"));
            Parent root = loader.load();

            DetailsReclamation controller = loader.getController();
            controller.setReclamationData(rec);
            controller.setAfficherReclamationsController(this); // 🔹 Pass controller reference

            Stage stage = new Stage();
            stage.setTitle("Modifier Reclamation");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Loading Error");
            alert.setContentText("Error while loading DetailsReclamation: " + e.getMessage());
            alert.showAndWait();
        }
    }
    public void refreshReclamationsDisplay() {
        try {
            List<Reclamation> reclamations = rs.readAll(); // Fetch updated list
            reclamationsGrid.getChildren().clear(); // Clear previous items

            int column = 0;
            int row = 0;

            for (Reclamation rec : reclamations) {
                VBox card = createReclamationCard(rec);
                reclamationsGrid.add(card, column, row);

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du chargement des réclamations: " + e.getMessage());
            alert.showAndWait();
        }
    }


    public void refreshReclamationsDisplayAfterDelete() {
        Platform.runLater(() -> { // Ensure UI update runs on JavaFX thread
            try {
                List<Reclamation> reclamations = rs.readAll(); // Fetch updated list
                reclamationsGrid.getChildren().clear(); // Clear previous items

                int column = 0;
                int row = 0;

                for (Reclamation rec : reclamations) {
                    VBox card = createReclamationCard(rec);
                    reclamationsGrid.add(card, column, row);

                    column++;
                    if (column == 3) {
                        column = 0;
                        row++;
                    }
                }

                System.out.println("✅ Reclamations successfully refreshed after delete!");

            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors du chargement des réclamations: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }


    public void goToAjouterReservation(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamationsAdmin.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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
    }

 */
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
            AfficherProduit controller = loader.getController();
            controller.loadProducts(); // Appelle ta méthode d'initialisation si elle existe

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
            AfficherProduit controller = loader.getController();
            controller.loadProducts(); // Appelle ta méthode d'initialisation si elle existe

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

