package gui.GestionProduit;

import entities.Produit;
import gui.GestionCommande.CommandeFormController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import services.ProduitService;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class AfficherProduit {

    @FXML
    private TilePane productTilePane;

    @FXML
    public void initialize() {
        loadProducts();
    }

    public void loadProducts() {
        ProduitService produitService = new ProduitService();
        List<Produit> produits = produitService.getAllProduits();

        // Vider le TilePane avant d'ajouter de nouveaux produits
        productTilePane.getChildren().clear();

        // Parcours de chaque produit
        for (Produit p : produits) {
            VBox box = new VBox(10);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");




            try {


                String imagePath = p.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    Image image = new Image(imagePath, true);

                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(120);
                    imageView.setFitWidth(120);
                    imageView.setPreserveRatio(true);  // Maintenir les proportions de l'image

                    // Créer les labels pour le nom, la description, le prix et la quantité
                    Label nameLabel = new Label(p.getNom());
                    nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                    Label descLabel = new Label(p.getDescription());
                    descLabel.setWrapText(true);

                    Label priceLabel = new Label("Prix : " + p.getPrix() + " DT");
                    Label stockLabel = new Label("Quantité : " + p.getQuantite());

                    // Ajouter un bouton pour ajouter au panier
                    Button buyButton = new Button("Ajouter au panier");
                    buyButton.setOnAction(e -> {
                        try {
                            // Charger la vue de commande
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommandeForm.fxml"));
                            Parent root = loader.load();

                            // Passer le produit au formulaire de commande
                            CommandeFormController controller = loader.getController();
                            controller.setProduit(p.getId());

                            Stage stage = new Stage();
                            stage.setTitle("Commande");
                            stage.setScene(new Scene(root, 400, 600));
                            stage.show();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    // Ajouter tous les éléments dans la boîte
                    box.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, stockLabel, buyButton);

                    // Ajouter la boîte au TilePane
                    productTilePane.getChildren().add(box);

                } else {
                    // Si le fichier n'existe pas, afficher un message dans la console
                    System.out.println("Image introuvable à : " + imagePath);
                }

            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de l'image pour le produit : " + p.getNom());
                e.printStackTrace();
            }
        }
    }
}
