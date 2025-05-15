package gui.GestionEvents;

import entities.Role;
import entities.user;
import gui.GestionUser.UserSession;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.Crole;
import services.userService;

import java.io.IOException;

public class ParticipantDashboard {

    @FXML public Button GoToEvents1;
    @FXML public Button Collaborations;
    @FXML public Button tickets;
    @FXML public Button Acceuil;
    @FXML public Button reclam;
    @FXML public Text scrolling_text;
    @FXML public Pane news_pane;

    @FXML public Label username;
    @FXML public Label role;
    @FXML public Label level;

    private final UserSession userSession = UserSession.getInstance();
    private Scene scene;

    @FXML
    public void initialize() {
        animateScrollingText();

        try {
            user currentUser = new userService().getUserById(userSession.getId());
            username.setText(currentUser.getUsername());
            level.setText(String.valueOf(userSession.getLevel()));

            Role currentRole = new Crole().getRoleById(userSession.getRole());
            role.setText(currentRole.getRoleName());
        } catch (Exception e) {
            role.setText("Unknown");
            e.printStackTrace();
        }
    }

    public void showEvents1(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents();

        Stage stage = (Stage) GoToEvents1.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }

    public void showAcceuil1(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showLastThreeEvents();

        Stage stage = (Stage) Acceuil.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }

    private void animateScrollingText() {
        String newsText = "Â° Welcome back!: " + userSession.getFirstname() + " " + userSession.getLastname();
        scrolling_text.setText(newsText);

        scrolling_text.setTranslateX(news_pane.getWidth());
        Rectangle clip = new Rectangle(1027, 60);
        news_pane.setClip(clip);

        news_pane.layout();
        double textWidth = scrolling_text.getLayoutBounds().getWidth();
        int animationDurationMillis = (int) (textWidth * 20);

        TranslateTransition transitionOut = new TranslateTransition(Duration.millis(animationDurationMillis), scrolling_text);
        transitionOut.setFromX(news_pane.getWidth());
        transitionOut.setByX(-textWidth);

        TranslateTransition transitionIn = new TranslateTransition(Duration.ZERO, scrolling_text);
        transitionIn.setFromX(news_pane.getWidth());

        SequentialTransition sequentialTransition = new SequentialTransition(transitionOut, transitionIn);
        sequentialTransition.setCycleCount(SequentialTransition.INDEFINITE);
        sequentialTransition.play();
    }
}
