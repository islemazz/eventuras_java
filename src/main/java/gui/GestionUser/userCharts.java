package gui.GestionUser;

import gui.mainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import services.userService;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class userCharts {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private PieChart gender_piechart;
    @FXML
    private BarChart<String, Number> age_barchart;


    private userService userService = new userService();
    @FXML
    void backtolist(ActionEvent event) throws IOException {
        navigateTo("/listUser.fxml", event);
    }
    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get the current stage using the event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


    @FXML
    void initialize() {
        Map<String, Long> genderDistribution = userService.getGenderDistribution();

        // Create PieChart data
        PieChart.Data[] pieChartData = genderDistribution.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .toArray(PieChart.Data[]::new);

        // Add data to PieChart
        gender_piechart.getData().addAll(pieChartData);

        // Retrieve age distribution data
        Map<String, Long> ageDistribution = userService.getAgeDistribution(); // Implement this method in userService

        // Create BarChart data
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ageDistribution.entrySet().forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        // Add data series to BarChart
        age_barchart.getData().add(series);
    }
}