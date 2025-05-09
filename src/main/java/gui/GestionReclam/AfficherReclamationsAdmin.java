package gui.GestionReclam;

import gui.GestionEvents.AfficherEventHOME;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import entities.Reclamation;
import javafx.stage.Stage;
import services.ReclamationService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherReclamationsAdmin implements Initializable {

    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    @FXML private GridPane reclamationsGrid;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private VBox detailsPanel;
    @FXML private Label lblUser, lblSubject, lblDescription;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextArea taResponse;
    @FXML private Label lblTotal, lblPending, lblResolved, lblRejected, lblCurrent;

    private final ReclamationService rs = new ReclamationService();
    private List<Reclamation> reclamationsList;
    private Reclamation currentReclamation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbFilterStatus.setItems(FXCollections.observableArrayList("All", "En attente", "En cours", "Rejeté", "Résolu"));
        cbFilterStatus.setValue("All"); // Default filter
        cbStatus.setItems(FXCollections.observableArrayList("En attente", "Résolu", "Rejeté" , "En cours"));

        loadReclamations();
    }

    private void loadReclamations() {
        try {
            reclamationsList = rs.readAll();
            updateGrid(reclamationsList);
            updateDashboardStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private VBox createReclamationCard(Reclamation rec) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: gray; -fx-padding: 10; -fx-background-radius: 10; -fx-background-color: #f8f9fa;");

        Label lblId = new Label("Ticket #" + rec.getId());
        Label lblStatus = new Label("Status: " + rec.getStatus());
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: " + getStatusColor(rec.getStatus()));

        Button btnDetails = new Button("Voir détails");
        btnDetails.setOnAction(event -> showDetails(rec));

        card.getChildren().addAll(lblId, new Text(rec.getSubject()), lblStatus, btnDetails);
        return card;
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "En attente" -> "#0066FFFF";
            case "Résolu" -> "green";
            case "Rejeté" -> "red";
            case "En cours" -> "#F7FF00FF" ;
            default -> "black";
        };
    }

    private void showDetails(Reclamation rec) {
        currentReclamation = rec;
        lblUser.setText("User: " + rec.getId_user());
        lblSubject.setText("Subject: " + rec.getSubject());
        lblDescription.setText("Description: " + rec.getDescription());
        cbStatus.setValue(rec.getStatus());

        detailsPanel.setVisible(true);
    }

    @FXML
    private void applyFilter() {
        String selectedStatus = cbFilterStatus.getValue();
        if (selectedStatus.equals("All")) {
            updateGrid(reclamationsList);
        } else {
            List<Reclamation> filtered = reclamationsList.stream()
                    .filter(rec -> rec.getStatus().equals(selectedStatus))
                    .toList();
            updateGrid(filtered);
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (currentReclamation == null) return;

        try {
            String newStatus = cbStatus.getValue();
            currentReclamation.setStatus(newStatus);
            rs.update(currentReclamation);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Status Updated");
            alert.setContentText("The status has been successfully updated!");
            alert.showAndWait();

            loadReclamations(); // Refresh UI
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendResponse() {
        if (currentReclamation == null) return;

        String responseText = taResponse.getText();
        if (responseText.isEmpty()) return;

        System.out.println("📩 Admin response: " + responseText);
        taResponse.clear(); // Clear input after sending
    }




    @FXML
    private void closeDetailsPanel() {
        detailsPanel.setVisible(false);
    }

    private void updateDashboardStats() {
        long pending = reclamationsList.stream().filter(r -> r.getStatus().equals("En attente")).count();
        long resolved = reclamationsList.stream().filter(r -> r.getStatus().equals("Résolu")).count();
        long rejected = reclamationsList.stream().filter(r -> r.getStatus().equals("Rejeté")).count();
        long EnCours = reclamationsList.stream().filter(r -> r.getStatus().equals("En cours")).count();



        lblTotal.setText("📊 Total: " + reclamationsList.size());
        lblPending.setText("📍 En attente: " + pending);
        lblResolved.setText("✅ Résolu: " + resolved);
        lblRejected.setText("❌ Rejeté: " + rejected);
        lblCurrent.setText("⌛ En cours: " + EnCours);
    }
    Scene scene;
    Stage stage;

    public void showEvents(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents(); // Call the method to display all events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) GoToEvents.getScene().getWindow();
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
        Stage stage = (Stage) Acceuil.getScene().getWindow();
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
}