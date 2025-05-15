package gui.GestionEvents;

import entities.Role;
import entities.user;
import gui.GestionUser.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.Crole;
import services.ServiceEvent;
import services.userService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class OrganisateurDashboard {
    @FXML public Button GoToEvents;
    @FXML public Button Collaborations;
    @FXML public Button tickets;
    @FXML public Button Acceuil;
    @FXML public Button reclam;
    @FXML public Button create;
    @FXML public Button forum;
    @FXML public BarChart<String,Number> monthlyEventsChart;
    @FXML public PieChart categoryPieChart;
    @FXML public BarChart<String,Number> participantsChart;
    @FXML public Label username, role, level;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private final UserSession userSession = UserSession.getInstance();

    @FXML
    public void initialize() {
        try {
            user currentUser = new userService().getUserById(userSession.getId());
            username.setText(currentUser.getUsername());
            level.setText(String.valueOf(userSession.getLevel()));

            Role currentRole = new Crole().getRoleById(userSession.getRole());
            role.setText(currentRole.getRoleName());

            loadStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() throws SQLException {
        Map<Integer, Long> monthlyStats = serviceEvent.calculateEventsPerMonth();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        monthlyStats.forEach((month, count) ->
                series.getData().add(new XYChart.Data<>(Month.of(month).toString(), count)));
        monthlyEventsChart.getData().add(series);

        Map<String, Long> categoryStats = serviceEvent.calculateEventsByCategory();
        categoryStats.forEach((category, count) ->
                categoryPieChart.getData().add(new PieChart.Data(category + " (" + count + ")", count)));

        Map<String, Integer> participantStats = serviceEvent.calculateParticipantsPerEventCategory();
        XYChart.Series<String, Number> participantSeries = new XYChart.Series<>();
        participantStats.forEach((category, count) ->
                participantSeries.getData().add(new XYChart.Data<>(category, count)));
        participantsChart.getData().add(participantSeries);
    }

    public void showAcceuil(ActionEvent event) throws IOException {
        switchScene("/organisateurDashboard.fxml", Acceuil);
    }

    public void showEvents2(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEvent.fxml"));
        Parent root = loader.load();
        AfficherEvent afficherEvent = loader.getController();
        afficherEvent.initialize();
        Stage stage = (Stage) GoToEvents.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    public void createEvent(ActionEvent event) throws IOException {
        switchScene("/AjouterEvent.fxml", create);
    }

    public void goToForum(ActionEvent event) throws IOException {
        switchScene("/posts.fxml", forum);
    }

    private void switchScene(String fxml, Button button) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
