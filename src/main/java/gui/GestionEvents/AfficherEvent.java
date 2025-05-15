package gui.GestionEvents;

import entities.Categorie;
import entities.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.ServiceCategorie;
import services.ServiceEvent;
import utils.MyConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

//AFFICHAGE POUR EVENTS DE L'ORGANISATEUR
public class AfficherEvent {
    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button reclam;
    public Button Acceuil;
    public HBox headerBox;
    public Button Supprimer;
    public Button modify;
    public TextField searchBar;
    public ComboBox filterTypeComboBox;
    public DatePicker datePicker;
    public ComboBox<String> categoryComboBox;
    public CheckBox freeCheckBox;
    ServiceEvent sE=new ServiceEvent();
    ServiceCategorie sc=new ServiceCategorie();
    private final Connection cnx;
    public AfficherEvent() {
        cnx = MyConnection.getInstance().getConnection();
    }
    @FXML
    private ListView<Event> listView;
    private ObservableList<Event> observableEvents;
    private ObservableList<Event> originalEvents;
    private String currentFilterType = null;
    private Stage stage;
    private Scene scene;
    @FXML
    void initialize() {
        try {
            // Create the column headers (titles for each "column")
            Text titleHeader = new Text("Title");
            Text descriptionHeader = new Text("Description");
            Text dateHeader = new Text("Date");
            Text locationHeader = new Text("Location");
            Text activitiesHeader = new Text("Activities");
            Text statusHeader = new Text("Status"); // Add activities header

            // Add the headers to the headerBox (HBox)
            headerBox.getChildren().addAll(titleHeader, descriptionHeader, dateHeader, locationHeader,activitiesHeader,statusHeader);
            headerBox.setSpacing(10);  // Adjust spacing between the column headers

            // Fetch the list of events from the service
            ArrayList<Event> events = sE.afficherAllForOrg(); // Fetch events with activities

            // Convert the List to an ObservableList for ListView binding
            originalEvents = FXCollections.observableList(events);
            observableEvents = FXCollections.observableList(events);
            listView.setItems(observableEvents);
            searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
                filterEvents(newValue);
            });

            // Customize the ListView to display the Event data
            listView.setCellFactory(new Callback<ListView<Event>, ListCell<Event>>() {
                @Override
                public ListCell<Event> call(ListView<Event> param) {
                    return new ListCell<Event>() {
                        @Override
                        protected void updateItem(Event item, boolean empty) {
                            super.updateItem(item, empty);

                            if (empty || item == null) {
                                setGraphic(null);
                            } else {
                                // Create a new HBox for each row (acting as columns)
                                HBox hBox = new HBox(10);  // 10px spacing between the "columns"
                                hBox.setStyle("-fx-padding: 10px; -fx-border-color: lightgray; -fx-border-width: 1px;");

                                // Format date using SimpleDateFormat (optional)
                                String formattedDate = "N/A"; // Default value if date is null
                                if (item.getDate_event() != null) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    formattedDate = sdf.format(item.getDate_event());
                                }

                                // Create Text nodes for each field to simulate columns
                                Text titleText = new Text(item.getTitle() != null ? item.getTitle() : "N/A");
                                titleText.setStyle("-fx-font-weight: bold; -fx-fill: #333333;");

                                Text descriptionText = new Text(item.getDescription() != null ? item.getDescription() : "N/A");
                                descriptionText.setStyle("-fx-fill: #555555;");

                                Text dateText = new Text(formattedDate); // Use the formatted date or "N/A"
                                dateText.setStyle("-fx-fill: #777777;");

                                Text locationText = new Text(item.getLocation() != null ? item.getLocation() : "N/A");
                                locationText.setStyle("-fx-fill: #555555;");

                                // Convert the activiteList to a comma-separated string for display
                                String activitiesString = "N/A";
                                if (item.getActivities() != null && !item.getActivities().isEmpty()) {
                                    activitiesString = String.join(", ", item.getActivities());
                                }
                                Text activitiesText = new Text(activitiesString); // Display activities
                                activitiesText.setStyle("-fx-fill: #555555;");

                                // Add the status field
                                Text statusText = new Text(item.getStatus() != null ? item.getStatus() : "N/A");
                                statusText.setStyle("-fx-fill: #555555;");

                                // Add the text nodes to the HBox (excluding event_id, user_id, and category_id)
                                hBox.getChildren().addAll(titleText, descriptionText, dateText, locationText, activitiesText, statusText);

                                // Set the HBox as the graphic of the ListCell
                                setGraphic(hBox);
                            }
                        }
                    };
                }
            });

            // Initialize the filter type combo box
            filterTypeComboBox.getItems().addAll("Date", "Category");
            filterTypeComboBox.setValue("Filter by"); // Default prompt text

            // Fetch categories from the database and populate the categoryComboBox
            List<Categorie> categories = sc.afficherAll();
            ObservableList<String> categoryNames = FXCollections.observableArrayList();
            for (Categorie c : categories) {
                categoryNames.add(c.getName());
            }

            // Set the items in the categoryComboBox
            categoryComboBox.setItems(categoryNames);

            // Debug: Print the categories to verify
            System.out.println("Categories loaded: " + categoryNames);

            // Add listener to filter type combo box
            filterTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Hide all filter components initially
                datePicker.setVisible(false);
                categoryComboBox.setVisible(false);

                // Show the appropriate filter component based on selection
                if ("Date".equals(newValue)) {
                    datePicker.setVisible(true);
                } else if ("Category".equals(newValue)) {
                    categoryComboBox.setVisible(true);
                }
                if (newValue.equals(currentFilterType)) {
                    resetObservableEvents();
                    currentFilterType = (String) newValue; // Cast newValue to String
                }
            });

            // Add listeners to filter components
            datePicker.valueProperty().addListener((observable, oldValue, newValue) -> filterByDate(newValue));
            categoryComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterByCategory(newValue));

        } catch (SQLException e) {
            // Handle any SQL exceptions by showing an alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void resetObservableEvents() {
        observableEvents.setAll(originalEvents); // Reset to the original list
    }
    private void filterByDate(LocalDate date) {
        if (date == null) return;

        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();
        for (Event event : observableEvents) {
            Date dateEvent = event.getDate_event();
            if (dateEvent == null) {
                continue;
            }

            LocalDate eventDate;
            if (dateEvent instanceof java.sql.Date) {
                // Eviter l'UnsupportedOperationException
                eventDate = ((java.sql.Date) dateEvent).toLocalDate();
            } else {
                // Cas "classique" si c'est un java.util.Date normal
                eventDate = dateEvent.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            if (eventDate.equals(date)) {
                filteredEvents.add(event);
            }
        }

        listView.setItems(filteredEvents);
        System.out.println("Filtered events by date: " + filteredEvents.size());
    }

    private void filterByCategory(String category) {
        if (category == null || category.isEmpty()) {
            listView.setItems(observableEvents); // Reset to full list if no category is selected
            return;
        }

        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();
        for (Event event : observableEvents) {
            if (event.getCategory_name().equals(category)) {
                filteredEvents.add(event);
            }
        }

        listView.setItems(filteredEvents);
        System.out.println("Filtered events by category: " + filteredEvents.size());
    }
    private void filterEvents(String query) {
        if (observableEvents == null) {
            System.out.println("Erreur: observableEvents est null !");
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            listView.setItems(observableEvents);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

        for (Event event : observableEvents) {
            if (event.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    event.getLocation().toLowerCase().contains(lowerCaseQuery)) {
                filteredEvents.add(event);
            }
        }

        listView.setItems(filteredEvents);
    }
    public void deleteEvent(ActionEvent event) {
        // Get the selected event from the ListView
        Event selectedEvent = listView.getSelectionModel().getSelectedItem();

        // Check if an event is selected
        if (selectedEvent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur de sélection");
            alert.setContentText("Veuillez sélectionner un événement à supprimer.");
            alert.showAndWait();
            return;
        }

        // Confirmation dialog before deletion
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer cet événement ?");
        confirmationAlert.setContentText("Événement: " + selectedEvent.getTitle());

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the event from the database
                sE.delete(selectedEvent);

                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setContentText("L'événement a été supprimé avec succès !");
                successAlert.showAndWait();

                // Refresh the ListView
                refreshListView();
            } catch (SQLException e) {
                showErrorAlert("Erreur lors de la suppression de l'événement: " + e.getMessage());
            }
        }
    }
    private void refreshListView() {
        try {
            // Fetch the updated list of events from the database
            ArrayList<Event> events = sE.afficherAllForOrg();

            // Convert the List to an ObservableList for ListView binding
            ObservableList<Event> observableEvents = FXCollections.observableList(events);
            listView.setItems(observableEvents);
        } catch (SQLException e) {
            showErrorAlert("Erreur lors du chargement des événements: " + e.getMessage());
        }
    }
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void modifierEvent(ActionEvent event) {
        // Get the selected event from the ListView
        Event selectedEvent = listView.getSelectionModel().getSelectedItem();

        // Check if an event is selected
        if (selectedEvent == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur de sélection");
            alert.setContentText("Veuillez sélectionner un événement à modifier.");
            alert.showAndWait();
            return;
        }

        // Create a custom dialog
        Dialog<Event> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'événement");
        dialog.setHeaderText("Modifier les détails de l'événement");

        // Set the button types (OK and Cancel)
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the layout for the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create text fields and list view for activities
        TextField titleField = new TextField(selectedEvent.getTitle());
        TextField descriptionField = new TextField(selectedEvent.getDescription());
        ListView<String> activitiesListView = new ListView<>(FXCollections.observableArrayList(selectedEvent.getActivities()));
        TextField newActivityField = new TextField();
        Button addActivityButton = new Button("Ajouter une activité");

        // Add components to the grid
        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Prix:"), 0, 2);
        grid.add(new Label("Activités:"), 0, 3);
        grid.add(activitiesListView, 1, 3);
        grid.add(newActivityField, 1, 4);
        grid.add(addActivityButton, 2, 4);

        // Add an event handler to the "Ajouter une activité" button
        addActivityButton.setOnAction(e -> {
            String newActivity = newActivityField.getText().trim();
            if (!newActivity.isEmpty()) {
                activitiesListView.getItems().add(newActivity);
                newActivityField.clear();
            }
        });

        // Set the grid to the dialog pane
        dialog.getDialogPane().setContent(grid);

        // Convert the result to an Event object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selectedEvent.setTitle(titleField.getText());
                selectedEvent.setDescription(descriptionField.getText());
                selectedEvent.setActivities(new ArrayList<>(activitiesListView.getItems()));
                return selectedEvent;
            }
            return null;
        });

        // Show the dialog and wait for the user's response
        Optional<Event> result = dialog.showAndWait();

        // If the user clicked "Enregistrer", update the event in the database and refresh the ListView
        result.ifPresent(updatedEvent -> {
            try {
                sE.update(updatedEvent);
                refreshListView();
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Succès");
                successAlert.setContentText("L'événement a été modifié avec succès !");
                successAlert.showAndWait();
            } catch (SQLException e) {
                showErrorAlert("Erreur lors de la modification de l'événement: " + e.getMessage());
            }
        });
    }

    public void showEvents(ActionEvent event) {
        
    }

    public void goToCollabs(ActionEvent event) {
    }

    public void goToTickets(ActionEvent event) {
    }

    public void showAcceuil(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/organisateurDashboard.fxml"));
        Parent root = loader.load();// Call the method to display last 3 events

        // Switch to the AfficherEvent scene
        stage = (Stage) Acceuil.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);

    }

    public void goToReclams(ActionEvent event) {
    }

}
