package gui;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;

import entities.Role;
import entities.user;
import gui.GestionUser.UserSession;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.Crole;
import services.userService;
public class adminDashboard {
    public Label welcome;
    public Button Dashboard;
    public Button userMan;
    public Button colisMan;
    public Button ForMan;
    public Button FactMan;
    public Button editProf;
    public Button disco;
    public ImageView picture;
    public Button events;
    @FXML
    private Text totalReclamation;

    @FXML
    private Text totalbillet;

    @FXML
    private Text totalfacture;
    private Stage stage;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private BarChart<String, Number> age_barchart;
    @FXML
    private Text date;
    @FXML
    private PieChart gender_piechart;
    @FXML
    private Pane ranking_pane;
    @FXML
    private VBox ranking_box;
    @FXML
    private Text totalusers;
    @FXML
    private Text scrolling_text;
    @FXML
    private Pane news_pane;
    @FXML
    private Text time;
    UserSession userSession = UserSession.getInstance();

    @FXML private Label username;
    @FXML private Label role;
    @FXML private Label level;



    @FXML
    void initialize() throws SQLException {
        updateTime();
// Récupère l'utilisateur connecté
        user connectedUser = new userService().getUserById(userSession.getId());  // utile si tu veux des infos fraîches depuis la DB

        // Remplir les labels
        username.setText(userSession.getUsername());
        level.setText(String.valueOf(userSession.getLevel()));

        try {
            Role currentRole = new Crole().getRoleById(userSession.getRole());
            role.setText(currentRole.getRoleName());
        } catch (Exception e) {
            role.setText("Unknown");
            System.err.println("Erreur lors de la récupération du rôle : " + e.getMessage());
        }
        userService userService = new userService();
        ObservableList<user> userList = FXCollections.observableList(userService.getallUserdata());
        animateScrollingText();
        animateTotalUsers(userList.size());
        userList.sort(Comparator.comparingInt(user::getLevel).reversed());
        Label titleLabel = new Label("User Ranking:");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        ListView<user> listView = new ListView<>(userList);
        listView.setStyle("-fx-background-color: rgba(0, 0, 0, 0); -fx-background-radius: 20;");
        listView.setCellFactory(param -> new RankingListCell());
        VBox vBox = new VBox(10);
        vBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0); -fx-background-radius: 20;");
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(titleLabel, listView);
        vBox.setAlignment(Pos.CENTER);
        ranking_box.getChildren().setAll(vBox);

        Map<String, Long> genderDistribution = userService.getGenderDistribution();

        PieChart.Data[] pieChartData = genderDistribution.entrySet().stream()
                .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                .toArray(PieChart.Data[]::new);

        gender_piechart.getData().addAll(pieChartData);
        Map<String, Long> ageDistribution = userService.getAgeDistribution();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ageDistribution.entrySet().forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        age_barchart.getData().add(series);
    }

    public void goto_dashboard(ActionEvent event) throws IOException {

    }
    //
    public void goto_user(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/listUser.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        stage = (Stage) userMan.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    //events(listEVENTS)
    public void goto_event(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventBACK.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        stage = (Stage) events.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    
    public void goto_partners(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminPartner.fxml"));
        Parent root = loader.load();

        // Switch to the Partners scene
        stage = (Stage) events.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void goto_partnerships(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminPartnership.fxml"));
        Parent root = loader.load();

        // Switch to the Partnerships scene
        stage = (Stage) events.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

//Reclamations
    public void goto_forum(ActionEvent event) throws IOException {
        /*  mainController.loadFXML("/login.fxml");*/
    }
    //Forum
    public void goto_blog(ActionEvent event) throws IOException {
        /*  mainController.loadFXML("/login.fxml");*/
    }

    public void goto_edit(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/editCurrentuser.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        stage = (Stage) editProf.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    //
    public void disconnect(ActionEvent event) throws IOException {
        userSession.cleanUserSession();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        stage = (Stage) disco.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    static class RankingListCell extends ListCell<user> {
        @Override
        protected void updateItem(user user, boolean empty) {
            super.updateItem(user, empty);
            if (empty || user == null) {
                setText(null);
                setGraphic(null);
            } else {
                StackPane stackPane = new StackPane();
                stackPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0); -fx-background-radius: 20;");
                Label usernameLabel = new Label(user.getUsername());
                ProgressBar progressBar = new ProgressBar(user.getLevel()/ 10.0);
                Label levelLabel = new Label("Level: " + user.getLevel());

                stackPane.getChildren().addAll(usernameLabel, progressBar, levelLabel);
                StackPane.setAlignment(usernameLabel, Pos.CENTER_LEFT);
                StackPane.setAlignment(progressBar, Pos.CENTER);
                StackPane.setAlignment(levelLabel, Pos.CENTER_RIGHT);
                setGraphic(stackPane);
            }
        }
    }

    private void animateTotalUsers(int totalUsers) {
        int animationDurationMillis = 2000;
        int initialCount = 1;

        Timeline timeline = new Timeline();

        totalusers.setText(String.valueOf(initialCount));

        for (int count = initialCount + 1; count <= totalUsers; count++) {
            KeyValue keyValue = new KeyValue(totalusers.textProperty(), String.valueOf(count), Interpolator.LINEAR);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(animationDurationMillis * (count - initialCount) / (totalUsers - initialCount)), keyValue);
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play();
    }

    private void animateScrollingText() {
        String newsText = "° Welcome back!: " + userSession.getFirstname() + " " + userSession.getLastname() ;
        scrolling_text.setText(newsText);
        Rectangle clip = new Rectangle(1027, 60);
        news_pane.setClip(clip);
        news_pane.layout();
        double textWidth = scrolling_text.getLayoutBounds().getWidth();
        int animationDurationMillis = (int) (textWidth * 20);
        TranslateTransition transitionOut = new TranslateTransition(Duration.millis(animationDurationMillis), scrolling_text);
        transitionOut.setByX(-textWidth);
        TranslateTransition transitionIn = new TranslateTransition(Duration.ZERO, scrolling_text);
        transitionIn.setByX(1027);
        SequentialTransition sequentialTransition = new SequentialTransition(transitionOut, transitionIn);
        sequentialTransition.setCycleCount(SequentialTransition.INDEFINITE);
        sequentialTransition.play();
    }
    private void updateTime() {
        // Get the current time
        LocalTime currentTime = LocalTime.now();

        // Format the time using a DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentTime.format(formatter);

        LocalDate currentDate = LocalDate.now();

        // Format the date using a DateTimeFormatter
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter1);
        // Set the formatted time to the time Text element
        date.setText(formattedDate+"  |  "+formattedTime);
    }


}





