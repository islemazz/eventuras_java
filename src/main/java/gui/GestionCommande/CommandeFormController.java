package gui.GestionCommande;

import entities.Commande;
import entities.Event;
import entities.Produit;
import gui.GestionEvents.AfficherEventHOME;
import gui.GestionProduit.AfficherProduit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.CommandeService;
import services.ProduitService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class CommandeFormController {

    @FXML private Label productNameLabel;
    @FXML private TextField nomClientField, adresseField, telephoneField, quantiteField;

    private Produit produit;

    // Méthode pour définir le produit sélectionné
    public void setProduit(int produit_id) {
        // Utiliser le service Produit pour récupérer le produit par son ID
        ProduitService produitService = new ProduitService();
        this.produit = produitService.getProduitById(produit_id);

        if (produit != null) {
            productNameLabel.setText("Produit : " + produit.getNom());
            System.out.println("Produit récupéré : " + produit.getNom()); // Vérification du produit
        } else {
            System.out.println("Produit non trouvé pour l'ID : " + produit_id);  // Ajouter un message de débogage
        }
    }


    // Méthode pour valider la commande
    @FXML
    private void validerCommande() {
        try {
            // Vérifier si le produit a un ID valide
            if (produit == null || produit.getId() == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Produit invalide ou non trouvé.");
                alert.showAndWait();
                return;
            }

            // Récupérer les valeurs saisies par l'utilisateur
            String nom_client = nomClientField.getText();
            String adresse = adresseField.getText();
            String telephone = telephoneField.getText();
            int quantite = Integer.parseInt(quantiteField.getText());

            // Calculer le total automatiquement
            Double total = produit.getPrix() * quantite;

            // La date de commande est automatiquement générée avec la date actuelle
            LocalDate localDate = LocalDate.now();  // Utilisation de la date actuelle
            Date date_commande = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Créer la commande
            Commande cmd = new Commande(produit, nom_client, adresse, telephone, quantite, total, date_commande);

            // Afficher dans la console la commande avant insertion
            System.out.println("Commande à ajouter : " + cmd);

            // Ajouter la commande via le service
            CommandeService service = new CommandeService();
            service.ajouterCommande(cmd);  // méthode à créer dans ton service

            // Afficher une alerte de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Commande enregistrée !");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la commande : " + e.getMessage());
            alert.showAndWait();
        }


    }

    }


