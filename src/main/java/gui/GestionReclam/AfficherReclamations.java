package gui.GestionReclam;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import entities.Reclamation;
import services.Reclamation.ReclamationService;


public class AfficherReclamations {

    private final ReclamationService rs = new ReclamationService();

    @FXML
    private GridPane reclamationsGrid; // GridPane to hold all cards

    @FXML
    void initialize() {
        cbFilterStatus.setItems(FXCollections.observableArrayList("All", "En attente", "En cours", "Rejeté", "Résolu"));
        cbFilterStatus.setValue("All");

        populateSubjects();

        // 🔹 Apply filters automatically when selection changes
        cbFilterStatus.setOnAction(event -> applyFilter());
        cbFilterSubject.setOnAction(event -> applyFilter());

        refreshReclamationsDisplay();
    }

    @FXML
    private void applyFilter() {
        String selectedStatus = cbFilterStatus.getValue();
        String selectedSubject = cbFilterSubject.getValue();

        List<Reclamation> filtered = reclamationsList.stream()
                .filter(rec -> selectedStatus.equals("All") || rec.getStatus().equals(selectedStatus))
                .filter(rec -> selectedSubject.equals("All") || rec.getSubject().equals(selectedSubject))
                .toList();

        updateGrid(filtered);
    }

    private void updateGrid(List<Reclamation> list) {
        reclamationsGrid.getChildren().clear();
        int column = 0, row = 0;
        for (Reclamation rec : list) {
            VBox card = createReclamationCard(rec);
            reclamationsGrid.add(card, column, row);
            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }


    private void populateSubjects() {
        Set<String> uniqueSubjects = new HashSet<>();
        try {
            reclamationsList = rs.readAllById();
            for (Reclamation rec : reclamationsList) {
                uniqueSubjects.add(rec.getSubject());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<String> subjectOptions = new ArrayList<>(uniqueSubjects);
        subjectOptions.add(0, "All"); // Default option
        cbFilterSubject.setItems(FXCollections.observableArrayList(subjectOptions));
        cbFilterSubject.setValue("All");
    }

    private List<Reclamation> reclamationsList;

    private VBox createReclamationCard(Reclamation rec) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPrefWidth(200);

        Label ticketNumber = new Label("Ticket #" + rec.getId());
        ticketNumber.getStyleClass().add("ticket-number");

        Label ticketDate = new Label("Créé à: " + rec.getCreated_at());
        ticketDate.getStyleClass().add("ticket-date");

        Label ticketDescription = new Label("Description: " + rec.getDescription());
        ticketDescription.getStyleClass().add("ticket-description");

        Label ticketStatus = new Label("Status: " + rec.getStatus());
        ticketStatus.getStyleClass().add("ticket-status");

        // ✅ Apply dynamic color based on status
        ticketStatus.setStyle("-fx-text-fill: " + getStatusColor(rec.getStatus()) + ";");

        // Action Buttons
        HBox actionButtons = new HBox(10);
        Button editButton = new Button("Détails");
        Button detailsButton = new Button("Suivre");

        editButton.getStyleClass().add("modifier-btn");
        detailsButton.getStyleClass().add("details-btn");

        actionButtons.getChildren().addAll(editButton, detailsButton);
        actionButtons.setOpacity(0);

        // Show buttons on hover
        card.setOnMouseEntered(event -> actionButtons.setOpacity(1));
        card.setOnMouseExited(event -> actionButtons.setOpacity(0));

        // "Modifier" leads to the details page
        editButton.setOnAction(event -> goToEditPage(event, rec));

        // ✅ Handle "Suivre" button click
        detailsButton.setOnAction(event -> {
            if ("En cours".equals(rec.getStatus()) || "Résolu".equals(rec.getStatus())) {
                // ✅ Allow navigation if status is "En cours" OR "Résolu"
                goToConversation(event, rec);
            } else {
                // ❌ Otherwise show alert
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Réclamation non acceptée");
                alert.setHeaderText(null);
                alert.setContentText("Cette réclamation n'est pas encore acceptée !");
                alert.showAndWait();
            }
        });


        card.getChildren().addAll(ticketNumber, ticketDate, ticketDescription, ticketStatus, actionButtons);
        return card;
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "En attente" -> "blue";  // or "#0066FF"
            case "Résolu" -> "green";
            case "Rejeté" -> "red";
            case "En cours" -> "orange";  // or "#F7FF00"
            default -> "black";
        };
    }


    public void goToAjouterPage(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AjouterReclamation.fxml"));
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
            alert.setHeaderText("An error occurred while loading the page.");

            // Create a TextArea with the full stack trace
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane content = new GridPane();
            content.setMaxWidth(Double.MAX_VALUE);
            content.add(new Label("The exception stacktrace was:"), 0, 0);
            content.add(textArea, 0, 1);

            alert.getDialogPane().setContent(content);
            alert.showAndWait();
        }

    }

    private void goToEditPage(javafx.event.ActionEvent event, Reclamation rec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DetailsReclamation.fxml"));
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
            reclamationsList = rs.readAllById();
            applyFilter(); // Apply filter automatically after loading
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
                List<Reclamation> reclamations = rs.readAllById(); // Fetch updated list
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


    public void goToAdminXD(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AfficherReclamationsAdmin.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


    public void goToConversation(ActionEvent event, Reclamation rec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/ReclamationConversationUser.fxml"));
            Parent root = loader.load();

            // ✅ Get the controller instance
            ReclamationConversationUser controller = loader.getController();

            // ✅ Pass the selected reclamation data
            controller.setReclamationData(rec);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir la conversation !");
            alert.showAndWait();
        }
    }
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private ComboBox<String> cbFilterSubject;


}

