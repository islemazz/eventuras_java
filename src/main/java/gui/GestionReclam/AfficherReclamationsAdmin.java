package gui.GestionReclam;

import entities.Reclamation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import services.Reclamation.ReclamationService;


public class AfficherReclamationsAdmin implements Initializable {

    @FXML private GridPane reclamationsGrid;
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private VBox detailsPanel;
    @FXML private Label lblUser, lblSubject, lblDescription;
    @FXML private ComboBox<String> cbStatus,cbFilterSubject;
    @FXML private TextArea taResponse;
    @FXML private Label lblTotal, lblPending, lblResolved, lblRejected, lblCurrent;


    private final ReclamationService rs = new ReclamationService();
    private List<Reclamation> reclamationsList;
    private Reclamation currentReclamation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbFilterStatus.setItems(FXCollections.observableArrayList("All", "En attente", "En cours", "Rejeté", "Résolu"));
        cbFilterStatus.setValue("All");

        cbStatus.setItems(FXCollections.observableArrayList("En attente", "Résolu", "Rejeté", "En cours"));

        // 🔹 Populate subjects dynamically
        Set<String> uniqueSubjects = new HashSet<>();
        try {
            reclamationsList = rs.readAll();
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

        // 🔹 Apply filter automatically when selections change
        cbFilterStatus.setOnAction(event -> applyFilter());
        cbFilterSubject.setOnAction(event -> applyFilter());

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

        // Determine button text and action based on status
        String buttonText;
        EventHandler<ActionEvent> buttonAction;

        if (rec.getStatus().equals("En cours") || rec.getStatus().equals("Résolu")) {
            buttonText = "Suivre";
            buttonAction = event -> goToConversation(event, rec);
        } else {
            buttonText = "Voir détails";
            buttonAction = event -> goToDetailsPage(event, rec);
        }

        Button actionButton = new Button(buttonText);
        actionButton.setOnAction(buttonAction);

        card.getChildren().addAll(lblId, new Text(rec.getSubject()), lblStatus, actionButton);
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



    private void goToDetailsPage(ActionEvent event, Reclamation rec) {
        System.out.println("DEBUG: Opening details for reclamation ID " + rec.getId());
        System.out.println("DEBUG: Refuse reason before passing: " + rec.getRefuseReason());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/DetailsReclamationAdmin.fxml"));
            Parent root = loader.load();

            DetailsReclamationAdmin controller = loader.getController();
            controller.setReclamationData(rec);
            controller.setAfficherReclamationsController(this);

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


    public void goToConversation(ActionEvent event, Reclamation rec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/ReclamationConversation.fxml"));
            Parent root = loader.load();

            // ✅ Get the controller instance
            ReclamationConversation controller = loader.getController();

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




    public void refreshReclamationsDisplay() {
        try {
            List<Reclamation> reclamations = rs.readAll();
            reclamationsGrid.getChildren().clear();

            int column = 0, row = 0;
            for (Reclamation rec : reclamations) {
                VBox card = createReclamationCard(rec);
                reclamationsGrid.add(card, column, row);

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

            this.updateDashboardStats();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Error loading reclamations: " + e.getMessage());
            alert.showAndWait();
        }
    }

}