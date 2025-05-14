package gui.GestionEvents;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Month;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import services.ServiceEvent;

public class OrganisateurDashboard {


    public Button GoToEvents;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    public Button create;
    public Button forum;
    public Button partners;
    public BarChart<String,Number> monthlyEventsChart;
    public PieChart categoryPieChart;
    public BarChart<String,Number> participantsChart;
    private ServiceEvent serviceEvent=new ServiceEvent();
    public void initialize() {
        try {
            loadStatistics();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() throws SQLException {
        // Statistiques par mois
        Map<Integer, Long> monthlyStats = serviceEvent.calculateEventsPerMonth();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        monthlyStats.forEach((month, count) ->
                series.getData().add(new XYChart.Data<>(Month.of(month).toString(), count)));
        monthlyEventsChart.getData().add(series);

        // Statistiques par catégorie
        Map<String, Long> categoryStats = serviceEvent.calculateEventsByCategory();
        categoryStats.forEach((category, count) ->
                categoryPieChart.getData().add(new PieChart.Data(category + " (" + count + ")", count)));

        // Statistiques participants par catégorie
        Map<String, Integer> participantStats = serviceEvent.calculateParticipantsPerEventCategory();
        XYChart.Series<String, Number> participantSeries = new XYChart.Series<>();
        participantStats.forEach((category, count) ->
                participantSeries.getData().add(new XYChart.Data<>(category, count)));
        participantsChart.getData().add(participantSeries);
    }
    public void showAcceuil(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/organisateurDashboard.fxml"));
        Parent root = loader.load();

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) Acceuil.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void showEvents2(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEvent.fxml"));
        Parent root = loader.load();

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) GoToEvents.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        AfficherEvent afficherEvent = loader.getController();
        afficherEvent.initialize(); // Call the method to display all events
    }

    public void createEvent(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterEvent.fxml"));
        Parent root = loader.load();

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) create.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void goToForum(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/posts.fxml"));
        Parent root = loader.load();

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) forum.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void goToCollaborations(ActionEvent event) throws IOException {
        // Load the UserParnter interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserParnter.fxml"));
        Parent root = loader.load();

        // Switch to the UserParnter scene
        Stage stage = (Stage) Collaborations.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}
