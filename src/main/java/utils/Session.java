package utils;
import entities.user;

import java.time.LocalDateTime;
import java.util.Properties;


public class Session {
    private static Session instance;
    private LocalDateTime sessionStartTime;
    private static user currentUser;
    private Session() {

    }
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // Démarrer une session
    public void startSession(user user) {
        this.currentUser = user;
        this.sessionStartTime = LocalDateTime.now();
        System.out.println("Session démarrée pour : " + user.getFirstname() +
                " à " + sessionStartTime);
    }

    // Terminer la session
    public void endSession() {
        this.currentUser = null;
        this.sessionStartTime = null;
        System.out.println("Session terminée");
    }
    public static user getCurrentUser() {
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté !");
        }
        return currentUser;
    }


    public static void setCurrentUser(user user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }


}


