package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import entities.Comment;
import entities.Post;
import entities.user;
import javafx.stage.Stage;
import services.ServiceComment;
import services.ServicePost;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javafx.geometry.Pos;
import services.userService;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.scene.layout.TilePane;

public class PostsController {
    @FXML private TextField TFTitle;
    @FXML private TextArea TFContent;
    @FXML private ImageView imageView;
    @FXML private VBox postsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button btnSearch;

    private final ServicePost servicePost = new ServicePost();
    private String imagePath = null;

    @FXML
    public void initialize() {
        filterComboBox.setValue("Tous");
        sortComboBox.setValue("Plus récents");
        afficherPosts();
        postsContainer.setAlignment(Pos.CENTER);
    }

    @FXML
    private void rechercher() {
        try {
            // Récupérer tous les posts depuis le service
            List<Post> allPosts = servicePost.afficherAll();
            String query = searchField.getText().trim().toLowerCase();
            String filter = filterComboBox.getValue();
            String sort = sortComboBox.getValue();

            // Filtrer selon le texte de recherche et le filtre choisi
            List<Post> filteredPosts = new java.util.ArrayList<>();
            for (Post post : allPosts) {
                boolean match = false;
                if (query.isEmpty()) {
                    match = true;
                } else {
                    switch (filter) {
                        case "Titre":
                            if (post.getTitle().toLowerCase().contains(query)) {
                                match = true;
                            }
                            break;
                        case "Contenu":
                            if (post.getContent().toLowerCase().contains(query)) {
                                match = true;
                            }
                            break;
                        case "Tous":
                        default:
                            if (post.getTitle().toLowerCase().contains(query) ||
                                    post.getContent().toLowerCase().contains(query)) {
                                match = true;
                            }
                            break;
                    }
                }
                if (match) {
                    filteredPosts.add(post);
                }
            }

            // Trier la liste selon l'option sélectionnée
            if (sort != null) {
                if (sort.equals("Plus récents")) {
                    filteredPosts.sort((p1, p2) -> p2.getCreated_at().compareTo(p1.getCreated_at()));
                } else if (sort.equals("Plus anciens")) {
                    filteredPosts.sort((p1, p2) -> p1.getCreated_at().compareTo(p2.getCreated_at()));
                }
            }

            // Mettre à jour l'affichage dans postsContainer
            postsContainer.getChildren().clear();
            for (Post post : filteredPosts) {
                VBox postBox = createPostBox(post);
                postsContainer.getChildren().add(postBox);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void afficherPosts() {
        postsContainer.getChildren().clear();
        try {
            List<Post> posts = servicePost.afficherAll();
            System.out.println(posts);
            for (Post post : posts) {
                VBox postBox = createPostBox(post);
                postsContainer.getChildren().add(postBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createPostBox(Post post) throws SQLException {
        VBox postBox = new VBox();
        postBox.setSpacing(10);
        postBox.setAlignment(Pos.CENTER_LEFT);
        postBox.getStyleClass().add("card");

        // 1. Récupération des informations de l'auteur du post
        userService serviceUser = new userService();
        user author = serviceUser.getUserById(post.getUser_id());

        // Zone d'info utilisateur (photo et username)
        HBox userInfoBox = new HBox();
        userInfoBox.setSpacing(10);
        userInfoBox.setAlignment(Pos.CENTER_LEFT);
        ImageView userImageView = new ImageView();
        userImageView.setFitWidth(40);
        userImageView.setFitHeight(40);
        if (author != null && author.getPicture() != null && !author.getPicture().isEmpty()) {
            Image userImage = new Image(getClass().getResourceAsStream("/Images/" + author.getPicture()));
            if (!userImage.isError()) {
                userImageView.setImage(userImage);
                // Arrondir la photo avec un clip circulaire
                Circle clip = new Circle(20, 20, 20);
                userImageView.setClip(clip);
            }
        }
        Label usernameLabel = new Label(author != null ? author.getUsername() : "Inconnu");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        userInfoBox.getChildren().addAll(userImageView, usernameLabel);

        // 2. Titre du post
        Label titleLabel = new Label(post.getTitle());
        titleLabel.getStyleClass().add("card-title");

        // 3. Informations supplémentaires : temps relatif depuis la création
        String relativeTime = getRelativeTime(post.getCreated_at());
        Label postInfoLabel = new Label("( " + relativeTime + " )");
        postInfoLabel.getStyleClass().add("card-info");
        postInfoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: gray;");

        // 4. Contenu du post
        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("card-content");
        contentLabel.setWrapText(true);

        // 5. Nombre de commentaires
        int commentCount = servicePost.getCommentCount(post.getId());
        Label commentCountLabel = new Label("Commentaires: " + commentCount);

        // 6. Image du post (si présente)
        ImageView postImageView = null;
        if (post.getImage_path() != null && !post.getImage_path().isEmpty()) {
            postImageView = new ImageView();
            postImageView.setPreserveRatio(true);
            postImageView.setFitWidth(250); // Image agrandie
            Image img = new Image("file:" + post.getImage_path());
            postImageView.setImage(img);
        }

        // 7. Système de like (affichage du nombre de likes dans le bouton)
        services.ServiceLike serviceLike = new services.ServiceLike();
        user currentUser = utils.Session.getInstance().getCurrentUser();
        int currentUserId = (currentUser != null) ? currentUser.getId() : -1;
        int likeCount = serviceLike.getNombreLikes(post.getId());
        boolean alreadyLiked = (currentUserId != -1) && serviceLike.aDejaLike(currentUserId, post.getId());
        Button likeButton = new Button((alreadyLiked ? "Unlike" : "Like") + " (" + likeCount + ")");
        likeButton.setOnAction(e -> {
            try {
                if (serviceLike.aDejaLike(currentUserId, post.getId())) {
                    serviceLike.supprimerLike(currentUserId, post.getId());
                } else {
                    serviceLike.ajouterLike(currentUserId, post.getId());
                }
                int newLikeCount = serviceLike.getNombreLikes(post.getId());
                // Met à jour le texte du bouton avec le nouvel état et le nombre de likes
                likeButton.setText((serviceLike.aDejaLike(currentUserId, post.getId()) ? "Unlike" : "Like") + " (" + newLikeCount + ")");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // 8. Bouton pour afficher/masquer la zone de commentaires
        Button toggleCommentsBtn = new Button("Afficher commentaires");
        toggleCommentsBtn.setOnAction(e -> {
            VBox commentsContainer = null;
            for (javafx.scene.Node node : postBox.getChildren()) {
                if (node instanceof VBox && "commentsContainer".equals(node.getId())) {
                    commentsContainer = (VBox) node;
                    break;
                }
            }
            if (commentsContainer != null) {
                postBox.getChildren().remove(commentsContainer);
                toggleCommentsBtn.setText("Afficher commentaires");
                try {
                    int newCount = servicePost.getCommentCount(post.getId());
                    commentCountLabel.setText("Commentaires: " + newCount);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                VBox newCommentsContainer = createCommentsContainer(post, postBox, toggleCommentsBtn, commentCountLabel);
                postBox.getChildren().add(newCommentsContainer);
                toggleCommentsBtn.setText("Réduire");
            }
        });

        // 9. Boutons Modifier et Supprimer pour le propriétaire du post
        HBox editDeleteBox = new HBox();
        editDeleteBox.setSpacing(10);
        editDeleteBox.setAlignment(Pos.CENTER_LEFT);
        if (currentUser != null && currentUser.getId() == post.getUser_id()) {
            Button btnModifier = new Button("Modifier");
            Button btnSupprimer = new Button("Supprimer");

            // Action Modifier : remplacement du titre et du contenu par des champs éditables
            btnModifier.setOnAction(e -> {
                TextField titleEditField = new TextField(post.getTitle());
                TextArea contentEditArea = new TextArea(post.getContent());
                contentEditArea.setWrapText(true);
                Button btnValider = new Button("Valider");
                VBox editBox = new VBox(10, titleEditField, contentEditArea, btnValider);
                editBox.setAlignment(Pos.CENTER_LEFT);

                int indexTitle = postBox.getChildren().indexOf(titleLabel);
                int indexContent = postBox.getChildren().indexOf(contentLabel);
                postBox.getChildren().remove(titleLabel);
                postBox.getChildren().remove(contentLabel);
                postBox.getChildren().add(indexTitle, editBox);

                btnValider.setOnAction(ev -> {
                    String newTitle = titleEditField.getText().trim();
                    String newContent = contentEditArea.getText().trim();
                    if (newTitle.isEmpty() || newContent.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs !");
                        alert.show();
                        return;
                    }
                    post.setTitle(newTitle);
                    post.setContent(newContent);
                    try {
                        servicePost.update(post);
                        Label newTitleLabel = new Label(newTitle);
                        newTitleLabel.getStyleClass().add("card-title");
                        Label newContentLabel = new Label(newContent);
                        newContentLabel.getStyleClass().add("card-content");
                        newContentLabel.setWrapText(true);
                        int editIndex = postBox.getChildren().indexOf(editBox);
                        postBox.getChildren().remove(editBox);
                        postBox.getChildren().add(editIndex, newTitleLabel);
                        postBox.getChildren().add(editIndex + 1, newContentLabel);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            });

            // Action Supprimer : confirmation puis suppression
            btnSupprimer.setOnAction(e -> {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce post ?", ButtonType.YES, ButtonType.NO);
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            servicePost.delete(post);
                            afficherPosts();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            });
            editDeleteBox.getChildren().addAll(btnModifier, btnSupprimer);
        }

        // 10. Bouton Partager : capture un snapshot du post et propose de le sauvegarder
        Button shareButton = new Button("Enregistrer");
        shareButton.setOnAction(e -> {
            // Prendre un snapshot du postBox
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(javafx.scene.paint.Color.TRANSPARENT);
            WritableImage snapshot = postBox.snapshot(params, null);
            // Ouvrir un FileChooser pour sauvegarder l'image
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le snapshot du post");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
            File file = fileChooser.showSaveDialog(postBox.getScene().getWindow());
            if (file != null) {
                try {
                    SwingFXUtils.fromFXImage(snapshot, null); // conversion
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Snapshot enregistré avec succès !");
                    alert.showAndWait();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Assemblage final de la "card"
        postBox.getChildren().addAll(userInfoBox, titleLabel, postInfoLabel, contentLabel);
        if (postImageView != null) {
            HBox imageContainer = new HBox();
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.getChildren().add(postImageView);
            postBox.getChildren().add(imageContainer);
        }
        HBox likeCommentBox = new HBox();
        likeCommentBox.setSpacing(10);
        likeCommentBox.setAlignment(Pos.CENTER_LEFT);
        likeCommentBox.getChildren().addAll(likeButton, toggleCommentsBtn, shareButton);
        // Note : Le nombre de likes est désormais affiché dans le bouton lui-même
        postBox.getChildren().add(likeCommentBox);
        if (!editDeleteBox.getChildren().isEmpty()) {
            postBox.getChildren().add(editDeleteBox);
        }

        // Ajout du label de commentaires (affiché séparément)
        postBox.getChildren().add(commentCountLabel);

        return postBox;
    }

    private HBox createCommentBox(Comment c) throws SQLException {
        HBox commentBox = new HBox();
        commentBox.setSpacing(10);
        commentBox.setAlignment(Pos.TOP_LEFT);
        commentBox.getStyleClass().add("comment-box");

        // Récupérer les informations du commentateur
        userService serviceUser = new userService();
        user commenter = serviceUser.getUserById(c.getUser_id());

        // Image du commentateur
        ImageView commenterImageView = new ImageView();
        commenterImageView.setFitWidth(30);
        commenterImageView.setFitHeight(30);
        if (commenter != null && commenter.getPicture() != null && !commenter.getPicture().isEmpty()) {
            Image commenterImage = new Image(getClass().getResourceAsStream("/Images/" + commenter.getPicture()));
            if (!commenterImage.isError()) {
                commenterImageView.setImage(commenterImage);
                // Arrondir l'image
                Circle clip = new Circle(15, 15, 15);
                commenterImageView.setClip(clip);
            }
        }

        VBox commentContentBox = new VBox();
        commentContentBox.setSpacing(3);
        commentContentBox.setAlignment(Pos.TOP_LEFT);

        Label usernameLabel = new Label(commenter != null ? commenter.getUsername() : "Inconnu");
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        // Vérifier si le contenu contient un GIF
        String content = c.getContent();
        // On suppose que si le contenu commence par "[GIF] ", c'est une URL de GIF
        Node contentNode;
        if(content.startsWith("[GIF] ")) {
            String gifUrl = content.substring(6).trim(); // Extrait l'URL en enlevant le préfixe
            Image gifImage = new Image(gifUrl);
            ImageView gifImageView = new ImageView(gifImage);
            gifImageView.setFitWidth(150);
            gifImageView.setPreserveRatio(true);
            contentNode = gifImageView;
        } else {
            Label commentTextLabel = new Label(content);
            commentTextLabel.setWrapText(true);
            commentTextLabel.setStyle("-fx-font-size: 12px;");
            contentNode = commentTextLabel;
        }

        String commentRelativeTime = getRelativeTime(c.getCreated_at());
        Label timeLabel = new Label("(" + commentRelativeTime + ")");
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        commentContentBox.getChildren().addAll(usernameLabel, contentNode, timeLabel);

        // Zone d'actions pour modifier/supprimer (si propriétaire)
        HBox actionButtonsBox = new HBox();
        actionButtonsBox.setSpacing(5);
        actionButtonsBox.setAlignment(Pos.CENTER_LEFT);
        user currentUser = utils.Session.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId() == c.getUser_id()) {
            Button btnModifier = new Button("Modifier");
            Button btnSupprimer = new Button("Supprimer");

            // Action Modifier : remplacement du label par un champ éditable et un bouton Valider
            btnModifier.setOnAction(e -> {
                TextField editField = new TextField(c.getContent());
                Button btnValider = new Button("Valider");
                HBox editBox = new HBox(editField, btnValider);
                editBox.setSpacing(5);

                // Remplacer temporairement la partie texte par le champ d'édition
                int index = commentContentBox.getChildren().indexOf(contentNode);
                commentContentBox.getChildren().remove(contentNode);
                commentContentBox.getChildren().add(index, editBox);

                btnValider.setOnAction(ev -> {
                    String newContent = editField.getText().trim();
                    if (newContent.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Le commentaire ne peut pas être vide !");
                        alert.show();
                        return;
                    }
                    c.setContent(newContent);
                    try {
                        ServiceComment serviceComment = new ServiceComment();
                        serviceComment.update(c); // Vous devez implémenter update dans ServiceComment
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    // Reconstruire le contenu en vérifiant le format GIF
                    Node updatedContentNode;
                    if(newContent.startsWith("[GIF] ")) {
                        String gifUrlUpdated = newContent.substring(6).trim();
                        Image gifImageUpdated = new Image(gifUrlUpdated);
                        ImageView gifImageViewUpdated = new ImageView(gifImageUpdated);
                        gifImageViewUpdated.setFitWidth(150);
                        gifImageViewUpdated.setPreserveRatio(true);
                        updatedContentNode = gifImageViewUpdated;
                    } else {
                        Label updatedCommentLabel = new Label(newContent);
                        updatedCommentLabel.setWrapText(true);
                        updatedCommentLabel.setStyle("-fx-font-size: 12px;");
                        updatedContentNode = updatedCommentLabel;
                    }
                    int editIndex = commentContentBox.getChildren().indexOf(editBox);
                    commentContentBox.getChildren().remove(editBox);
                    commentContentBox.getChildren().add(editIndex, updatedContentNode);
                });
            });

            // Action Supprimer : confirmation puis suppression
            btnSupprimer.setOnAction(e -> {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            ServiceComment serviceComment = new ServiceComment();
                            serviceComment.delete(c); // Implémentez delete dans ServiceComment
                            ((VBox) commentBox.getParent()).getChildren().remove(commentBox);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            });
            actionButtonsBox.getChildren().addAll(btnModifier, btnSupprimer);
        }

        if (!actionButtonsBox.getChildren().isEmpty()) {
            commentContentBox.getChildren().add(actionButtonsBox);
        }

        commentBox.getChildren().addAll(commenterImageView, commentContentBox);
        return commentBox;
    }

    private VBox createCommentsContainer(Post post, VBox postBox, Button toggleCommentsBtn, Label commentCountLabel) {
        VBox commentsContainer = new VBox();
        commentsContainer.setSpacing(5);
        commentsContainer.setStyle("-fx-padding: 5; -fx-background-color: #e9e9e9; -fx-border-color: #ccc;");
        commentsContainer.setId("commentsContainer");

        ServiceComment serviceComment = new ServiceComment();
        try {
            List<Comment> comments = serviceComment.getCommentsByPostId(post.getId());
            for (Comment c : comments) {
                HBox commentBox = createCommentBox(c);
                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Zone pour écrire un nouveau commentaire et ajouter un GIF
        HBox addCommentBox = new HBox();
        addCommentBox.setSpacing(5);
        TextField TFNewComment = new TextField();
        TFNewComment.setPromptText("Écrire un commentaire...");
        Button btnAjouterComment = new Button("Ajouter");
        Button btnAjouterGIF = new Button("Ajouter GIF");

        // Action pour rechercher et ajouter un GIF dans le champ de commentaire
        btnAjouterGIF.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Recherche de GIF");
            dialog.setHeaderText("Entrez un mot-clé pour rechercher des GIFs");
            dialog.setContentText("Mot-clé:");
            dialog.showAndWait().ifPresent(keyword -> {
                List<String> gifs = searchGifs(keyword); // Assurez-vous d'avoir implémenté cette méthode
                if (!gifs.isEmpty()) {
                    // Créer une fenêtre pour afficher les GIFs dans une galerie
                    Stage gifStage = new Stage();
                    gifStage.setTitle("Sélectionnez un GIF");

                    TilePane tilePane = new TilePane();
                    tilePane.setHgap(10);
                    tilePane.setVgap(10);
                    tilePane.setPrefColumns(3);
                    tilePane.setAlignment(Pos.CENTER);

                    for (String gifUrl : gifs) {
                        Image gifImage = new Image(gifUrl);
                        ImageView gifImageView = new ImageView(gifImage);
                        gifImageView.setFitWidth(100);
                        gifImageView.setPreserveRatio(true);
                        gifImageView.setOnMouseClicked(ev -> {
                            // Insérer l'URL du GIF dans le champ de commentaire ou, par exemple, formater différemment
                            TFNewComment.setText("[GIF] " + gifUrl);
                            gifStage.close();
                        });
                        tilePane.getChildren().add(gifImageView);
                    }

                    ScrollPane sp = new ScrollPane(tilePane);
                    sp.setFitToWidth(true);
                    Scene scene = new Scene(sp, 400, 300);
                    gifStage.setScene(scene);
                    gifStage.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Aucun GIF trouvé pour ce mot-clé.");
                    alert.showAndWait();
                }
            });
        });

        addCommentBox.getChildren().addAll(TFNewComment, btnAjouterComment, btnAjouterGIF);

        btnAjouterComment.setOnAction(e -> {
            String newCommentText = TFNewComment.getText().trim();
            if (!newCommentText.isEmpty()) {
                try {
                    user currentUser = utils.Session.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Aucun utilisateur connecté !");
                        alert.showAndWait();
                        return;
                    }
                    int currentUserId = currentUser.getId();
                    Comment newComment = new Comment(0, post.getId(), currentUserId, newCommentText, new Date());
                    ServiceComment serviceComment2 = new ServiceComment();
                    serviceComment2.ajouter(newComment);
                    // Rafraîchir le conteneur des commentaires
                    VBox refreshedContainer = createCommentsContainer(post, postBox, toggleCommentsBtn, commentCountLabel);
                    int index = postBox.getChildren().indexOf(commentsContainer);
                    postBox.getChildren().set(index, refreshedContainer);
                    // Mettre à jour le nombre de commentaires dans la carte
                    int newCount = servicePost.getCommentCount(post.getId());
                    commentCountLabel.setText("Commentaires: " + newCount);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        commentsContainer.getChildren().add(addCommentBox);
        return commentsContainer;
    }

    @FXML
    private void ajouter() {
        String title = TFTitle.getText();
        String content = TFContent.getText();

        if (title.isEmpty() || content.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs !");
            alert.show();
            return;
        }

        // Récupérer l'utilisateur courant depuis la session
        user currentUser = utils.Session.getInstance().getCurrentUser();
        int currentUserId = 25;
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Aucun utilisateur connecté !");
            alert.show();
            //return;
        }
        else{
            currentUserId = currentUser.getId();
        }
        try {
            Timestamp createdAt = new Timestamp(System.currentTimeMillis());
            // Utiliser l'ID de l'utilisateur courant au lieu de la valeur fixe "1"
            Post newPost = new Post(0, title, content, createdAt, currentUserId, imagePath);
            servicePost.ajouter(newPost);
            afficherPosts();
            TFTitle.clear();
            TFContent.clear();
            imageView.setImage(null);
            imagePath = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            Image img = new Image("file:" + imagePath);
            imageView.setImage(img);
        }
    }


    private String getRelativeTime(Date date) {
        long diffMillis = System.currentTimeMillis() - date.getTime();
        System.out.println("date "+date);


        // Si la date est dans le futur
        if(diffMillis < 0) {
            return "À l'avenir";
        }

        long diffSeconds = diffMillis / 1000;
        if(diffSeconds < 60) {
            return "Il y a " + diffSeconds + " s";
        }

        long diffMinutes = diffSeconds / 60;
        System.out.println("System.currentTimeMillis() "+System.currentTimeMillis());
        System.out.println("date.getTime() "+date.getTime());

        if(diffMinutes < 60) {
            return "Il y a " + diffMinutes + " m";
        }

        long diffHours = diffMinutes / 60;
        if(diffHours < 24) {
            return "Il y a " + diffHours + " h";
        }

        long diffDays = diffHours / 24;
        return "Il y a " + diffDays + " j";
    }

    private List<String> searchGifs(String query) {
        List<String> gifUrls = new ArrayList<>();
        try {
            String apiKey = "Gsaeg8PQFRMhpwwZQ113uYCZAMi94m8K"; // Remplacez par votre clé API
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = "https://api.giphy.com/v1/gifs/search?api_key=" + apiKey + "&q=" + encodedQuery + "&limit=10";
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Erreur de réponse HTTP: " + responseCode);
                return gifUrls;
            }
            StringBuilder inline = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();
            JSONObject json = new JSONObject(inline.toString());
            JSONArray data = json.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject gifObject = data.getJSONObject(i);
                // On choisit par exemple l'URL du GIF de taille fixe (petite)
                String gifUrl = gifObject.getJSONObject("images")
                        .getJSONObject("fixed_height_small")
                        .getString("url");
                gifUrls.add(gifUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gifUrls;
    }
}
