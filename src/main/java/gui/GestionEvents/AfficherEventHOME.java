package gui.GestionEvents;

import entities.Event;
import entities.user;
import gui.GestionProduit.AfficherProduit;
import gui.GestionUser.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.ServiceEvent;
import utils.MyConnection;
import utils.Session;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

//AFFICHAGE POUR PARTICIPANT ET ORGANISATEUR
public class AfficherEventHOME implements Initializable {


    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    public ScrollPane scrollablePane;
    public VBox eventContainer;
    public HBox sliderBox;
    private final Connection cnx;
    public Button create;
    public TextField searchBar;
    public Button Boutique;
    public Button forum;

    public AfficherEventHOME() {
        cnx = MyConnection.getInstance().getConnection();
    }
    public final ServiceEvent sE=new ServiceEvent();
    user CurrentUser = Session.getCurrentUser();
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //setButtonVisibilty();
        // Add a listener to the searchBar to filter events dynamically
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEvents(newValue);
        });
    }


    public void showLastThreeEvents() {
        // Hide the ScrollPane and show the Slider with the last 3 events
        scrollablePane.setVisible(false);
        sliderBox.setVisible(true);
        searchBar.setVisible(false);
        loadLastThreeEvents();
    }
    public void showAllEvents() {
        // Hide the Slider and show the ScrollPane with all events
        sliderBox.setVisible(false);
        scrollablePane.setVisible(true);
        eventContainer.setVisible(true);
        searchBar.setVisible(true);
        loadAllEvents();
    }
    //Acceuil
    private int currentIndex = 0; // To track the current event index



    private void loadLastThreeEvents() {
        try {
            ArrayList<Event> lastThreeEvents = sE.afficherLastEve();
            ObservableList<Event> observableList = FXCollections.observableList(lastThreeEvents);
            sliderBox.getChildren().clear();

            HBox eventContainer = new HBox(30);  // Increased spacing between cards
            eventContainer.setAlignment(Pos.CENTER);
            eventContainer.setStyle("-fx-padding: 20px;"); // Add padding around the container

            for (Event event : observableList) {
                HBox eventBox = createEventCard(event);
                // Set a fixed size for each event card to prevent overflow
                eventBox.setPrefSize(400, 300); // Adjusted size to be more compact
                eventBox.setMaxSize(400, 300);
                eventContainer.getChildren().add(eventBox);
            }

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(eventContainer);
            scrollPane.setFitToHeight(true);
            scrollPane.setPrefViewportWidth(1200); // Increased viewport width
            scrollPane.setPrefViewportHeight(350); // Set fixed height
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setStyle("-fx-background-color: transparent;"); // Make background transparent

            // Style the navigation buttons
            Button prevButton = new Button("◀");
            Button nextButton = new Button("▶");

            String buttonStyle =
                    "-fx-background-color: #072571; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-padding: 10px 15px; " +
                            "-fx-cursor: hand;";

            prevButton.setStyle(buttonStyle);
            nextButton.setStyle(buttonStyle);

            // Smooth scrolling for navigation buttons
            prevButton.setOnAction(e -> {
                double newValue = scrollPane.getHvalue() - 0.33; // Scroll by one card
                scrollPane.setHvalue(Math.max(0, newValue));
            });

            nextButton.setOnAction(e -> {
                double newValue = scrollPane.getHvalue() + 0.33; // Scroll by one card
                scrollPane.setHvalue(Math.min(1, newValue));
            });

            HBox navigationBox = new HBox(20, prevButton, scrollPane, nextButton);
            navigationBox.setAlignment(Pos.CENTER);
            navigationBox.setStyle("-fx-padding: 20px;");

            sliderBox.getChildren().add(navigationBox);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ... existing code ...
    private HBox createEventCard(Event event) {
        // Create the main container for the event card
        HBox eventBox = new HBox(20); // Increased spacing between elements
        eventBox.setStyle(
                "-fx-border-color: #cccccc; " + // Light gray border
                        "-fx-border-radius: 10px; " + // Rounded corners
                        "-fx-padding: 20px; " + // Increased padding
                        "-fx-background-color: #f9f9f9; " + // Light background color
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 5, 0, 0);" // Subtle shadow
        );
        eventBox.setAlignment(Pos.CENTER_LEFT);

        // Set preferred size for the HBox (event card)
        eventBox.setPrefSize(900, 350); // Increase width to 900px and height to 350px

        // Load the event image
        String imagePath = event.getImage() != null ? event.getImage().trim() : "";
        ImageView eventImage = new ImageView();

        if (!imagePath.isEmpty()) {
            try {
                String fileName = new File(imagePath).getName();
                URL imageUrl = getClass().getResource("/ImagesEvents/" + fileName);
                if (imageUrl != null) {
                    System.out.println("Loading image from: " + imageUrl.toExternalForm());
                    eventImage.setImage(new Image(imageUrl.toExternalForm()));
                } else {
                    System.out.println("Image not found: " + imagePath);
                    eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
            }
        } else {
            eventImage.setImage(new Image(getClass().getResource("/ImagesEvents/default-image.png").toExternalForm()));
        }

        // Set image size
        eventImage.setFitWidth(600); // Increase image width to 600px
        eventImage.setFitHeight(300); // Increase image height to 300px
        eventImage.setPreserveRatio(true); // Maintain aspect ratio

        // Create a container for the text (title and price)
        VBox textContainer = new VBox(10); // Increased spacing between text elements
        textContainer.setAlignment(Pos.CENTER_LEFT);
        textContainer.setPrefWidth(250); // Set a preferred width for the text container

        // Style the title
        Text titleText = new Text(event.getTitle());
        titleText.setStyle(
                "-fx-font-size: 24px; " + // Larger font size
                        "-fx-font-weight: bold; " + // Bold text
                        "-fx-fill: #333333;" // Dark gray color
        );

        // Add text elements to the container
        textContainer.getChildren().addAll(titleText);

        // Add a "Participer" button
        Button participerButton = new Button("Participer");
        participerButton.setStyle(
                "-fx-background-color: #072571; " + // Green background
                        "-fx-text-fill: white; " + // White text
                        "-fx-font-size: 16px; " + // Larger font size
                        "-fx-padding: 10px 20px;" // Padding
        );

        // Set the button action
        participerButton.setOnAction(e -> {
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
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors de l'ouverture de l'interface de participation.");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        // Add image, text container, and button to the event card
        eventBox.getChildren().addAll(eventImage, textContainer, participerButton);

        return eventBox;
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
    }

    public void goToTickets(ActionEvent event) {
    }

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

    public void goToReclams(ActionEvent event) {
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

    public void goToForum(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("posts.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        Stage stage = (Stage) forum.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);

    }
}







