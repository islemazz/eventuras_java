package gui.GestionEvents;
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
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ParticipantDashboard {

    public Button GoToEvents1;
    public Button Collaborations;
    public Button tickets;
    public Button Acceuil;
    public Button reclam;
    public Text scrolling_text;
    public Pane news_pane;
    private Scene scene;
    user CurrentUser;

  @FXML
  void initialize(){
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
  }
    public void showEvents1(ActionEvent event) throws IOException {  // Load the AfficherEvent interface
        // Load the AfficherEvent interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherEventHOME.fxml"));
        Parent root = loader.load();

        AfficherEventHOME afficherEventController = loader.getController();
        afficherEventController.showAllEvents(); // Call the method to display all events

        // Switch to the AfficherEvent scene
        Stage stage = (Stage) GoToEvents1.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
    }

    public void showAcceuil1(ActionEvent event) throws IOException {
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

    private void animateScrollingText() {
        if (CurrentUser == null) {
            scrolling_text.setText("Welcome!");
            return; 
        }
        String newsText = "° Welcome back!: " + CurrentUser.getFirstname() + " " + CurrentUser.getLastname() + " hetheka houwa °";
        scrolling_text.setText(newsText);

        // Make sure the text is initially positioned off-screen to the right
        scrolling_text.setTranslateX(news_pane.getWidth());

        Rectangle clip = new Rectangle(1027, 60);
        news_pane.setClip(clip);  // Ensure the clipping area is large enough for the text

        // Make sure the layout is recalculated to get the correct width of the text
        news_pane.layout();
        double textWidth = scrolling_text.getLayoutBounds().getWidth();

        // Calculate animation duration based on text width
        int animationDurationMillis = (int) (textWidth * 20);

        // TranslateTransition for the scrolling effect
        TranslateTransition transitionOut = new TranslateTransition(Duration.millis(animationDurationMillis), scrolling_text);
        transitionOut.setFromX(news_pane.getWidth()); // Start position (off-screen right)
        transitionOut.setByX(-textWidth); // Move left by text width

        // Transition to reset the text position to the right after it's off-screen
        TranslateTransition transitionIn = new TranslateTransition(Duration.ZERO, scrolling_text);
        transitionIn.setFromX(news_pane.getWidth());  // Reset to right side

        // Sequential transition to repeat the scrolling
        SequentialTransition sequentialTransition = new SequentialTransition(transitionOut, transitionIn);
        sequentialTransition.setCycleCount(SequentialTransition.INDEFINITE);
        sequentialTransition.play();
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
