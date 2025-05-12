package gui.GestionProduit;

import entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import services.ProduitService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AjouterProduit {

    @FXML private TextField NameField;
    @FXML private TextField DescriptionField;
    @FXML private TextField PriceField;
    @FXML private TextField QuantityField;
    @FXML private Button ImageB;
    @FXML private Button btnsubmit;

    private File selectedImageFile;
    private final ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        // Ajout d'un gestionnaire d'événements pour l'importation d'image
        ImageB.setOnAction(this::handleImportImage);
        btnsubmit.setOnAction(this::handleAddAndRedirect);
    }

    private void handleAddAndRedirect(ActionEvent event) {
        if (addProduit()) {
            redirectToShop(event);
        }
    }

    @FXML
    private boolean addProduit() {
        try {
            String nom = NameField.getText().trim();
            String description = DescriptionField.getText().trim();
            double prix = Double.parseDouble(PriceField.getText().trim());
            int quantite = Integer.parseInt(QuantityField.getText().trim());

            // Vérification si des champs sont vides
            if (nom.isEmpty() || description.isEmpty() || selectedImageFile == null) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs et importer une image.");
                return false;
            }

            String imagePath = selectedImageFile.toURI().toString(); // On récupère le chemin de l'image
            Produit newProduit = new Produit(0, nom, description, prix, quantite, imagePath);
            produitService.create(newProduit); // Ajouter le produit dans la base de données

            return true;

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Prix ou quantité invalide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Échec lors de l'ajout : " + e.getMessage());
        }
        return false;
    }

    @FXML
    public void handleImportImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // On garde la référence du fichier sélectionné
            selectedImageFile = file;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToShop(ActionEvent event) {
        try {
            // Chargement de la page de la boutique après ajout du produit
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherProduit.fxml"));
            Parent shopRoot = loader.load();

            // Récupérer le contrôleur de la page de la boutique pour rafraîchir les produits
            AfficherProduit afficherProduitController = loader.getController();
            afficherProduitController.loadProducts();  // Rafraîchir les produits après l'ajout

            Stage stage = (Stage) btnsubmit.getScene().getWindow();
            stage.setScene(new Scene(shopRoot));
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la boutique.");
        }
    }
}
