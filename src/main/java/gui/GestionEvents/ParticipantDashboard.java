package gui.GestionEvents;

import entities.Role;
import java.io.IOException;

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


public class ParticipantDashboard {

    @FXML public Button GoToEvents1;
    @FXML public Button Collaborations;
    @FXML public Button tickets;
    @FXML public Button Acceuil;
    @FXML public Button reclam;
    @FXML public Text scrolling_text;
    @FXML public Pane news_pane;
    public Button forum;
    @FXML public Label username;
    @FXML public Label role;
    @FXML public Label level;

    private final UserSession userSession = UserSession.getInstance();
    private Scene scene;
    user CurrentUser;

  @FXML
    public void initialize() {
        try {
          UserSession session = UserSession.getInstance();
          // Reconstruct the user object from UserSession
          CurrentUser = new user(
              session.getId(),
              session.getUsername(),
              session.getEmail(),
              session.getPassword(), // Or null/empty string if not needed here
              session.getFirstname(),
              session.getLastname(),
              session.getBirthday(),
              session.getGender(),
              session.getPicture(),
              session.getPhonenumber(),
              session.getLevel(),
              session.getRole()
          );
      } catch (IllegalStateException e) {
          System.err.println("ParticipantDashboard: User session not initialized. " + e.getMessage());
          CurrentUser = null; 
      }
      
      if (CurrentUser != null) {
          animateScrollingText();
      } else {
          scrolling_text.setText("Welcome, Guest!"); 
      }

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
        if (CurrentUser == null) {
            scrolling_text.setText("Welcome!");
            return; 
        }
        String newsText = "° Welcome back!: " + userSession.getFirstname() + " " + userSession.getLastname();
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
    public void goToForum(ActionEvent event) throws IOException {
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("posts.fxml"));
        Parent root = loader.load();


        // Switch to the AfficherEvent scene
        Stage stage = (Stage) forum.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);

    }

    public void goToReclams(ActionEvent event) throws IOException {  // Load the AfficherEvent interface
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Reclamation/AfficherReclamations.fxml"));
        Parent root = loader.load();

//        AfficherEventHOME afficherEventController = loader.getController();
//        afficherEventController.showAllEvents(); // Call the method to display all events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) reclam.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }


    @FXML
    void goToPartnerships(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ParticipPartner.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) Collaborations.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
