package gui.GestionForum;

import gui.GestionUser.UserSession;
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
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javafx.geometry.Pos;
import services.userService;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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
            String path = "/images/" + author.getPicture();
            System.out.println("Loading image from classpath: " + path);
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                is = getClass().getResourceAsStream("/images/profil.png");
            }
            Image userImage = new Image(is);
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
        UserSession session = UserSession.getInstance();
        int currentUserId = session.getId();
        int likeCount = serviceLike.getNombreLikes(post.getId());
        boolean alreadyLiked = (currentUserId != -1) && serviceLike.aDejaLike(currentUserId, post.getId());
        Button likeButton = new Button((alreadyLiked ? "Unlike" : "Like") + " (" + likeCount + ")");

        // 8. Bouton pour afficher/masquer les commentaires
        Button toggleCommentsBtn = new Button("Afficher les commentaires");
        toggleCommentsBtn.setOnAction(e -> {
            Node existingCommentsContainer = postBox.lookup("#commentsContainer");
            if (existingCommentsContainer != null) {
                existingCommentsContainer.setVisible(!existingCommentsContainer.isVisible());
                toggleCommentsBtn.setText(existingCommentsContainer.isVisible() ? "Masquer les commentaires" : "Afficher les commentaires");
            } else {
                VBox newCommentsContainer = createCommentsContainer(post, postBox, toggleCommentsBtn, commentCountLabel);
                postBox.getChildren().add(newCommentsContainer);
                toggleCommentsBtn.setText("Masquer les commentaires");
            }
        });

        // Ajout des éléments au postBox
        postBox.getChildren().addAll(userInfoBox, titleLabel, postInfoLabel, contentLabel);
        if (postImageView != null) {
            postBox.getChildren().add(postImageView);
        }
        postBox.getChildren().addAll(likeButton, toggleCommentsBtn);

        // 9. Boutons Modifier et Supprimer pour le propriétaire du post
        HBox editDeleteBox = new HBox();
        editDeleteBox.setSpacing(10);
        editDeleteBox.setAlignment(Pos.CENTER_LEFT);
        if (session != null && currentUserId == post.getUser_id()) {
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

                btnValider.setOnAction(event -> {
                    try {
                        post.setTitle(titleEditField.getText());
                        post.setContent(contentEditArea.getText());
                        servicePost.update(post);
                        postBox.getChildren().remove(editBox);
                        titleLabel.setText(post.getTitle());
                        contentLabel.setText(post.getContent());
                        postBox.getChildren().add(indexTitle, titleLabel);
                        postBox.getChildren().add(indexContent, contentLabel);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            });

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
            SnapshotParameters params = new SnapshotParameters();
            WritableImage snapshot = postBox.snapshot(params, null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le post");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                    ImageIO.write(bufferedImage, "png", file);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Ajout des boutons d'action
        HBox actionButtonsBox = new HBox(10, likeButton, toggleCommentsBtn, shareButton);
        actionButtonsBox.setAlignment(Pos.CENTER_LEFT);
        postBox.getChildren().add(actionButtonsBox);

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
            String path = "/images/" + commenter.getPicture();
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                is = getClass().getResourceAsStream("/images/profil.png");
            }
            Image commenterImage = new Image(is);
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
        UserSession session = UserSession.getInstance();
        int currentUserId = session.getId();

        if (currentUserId == c.getUser_id()) {
            Button btnModifier = new Button("Modifier");
            Button btnSupprimer = new Button("Supprimer");

            btnModifier.setOnAction(e -> {
                TextField editField = new TextField(c.getContent());
                Button btnValider = new Button("Valider");
                HBox editBox = new HBox(5, editField, btnValider);
                editBox.setAlignment(Pos.CENTER_LEFT);

                int index = commentContentBox.getChildren().indexOf(contentNode);
                commentContentBox.getChildren().remove(contentNode);
                commentContentBox.getChildren().add(index, editBox);

                btnValider.setOnAction(event -> {
                    try {
                        c.setContent(editField.getText());
                        ServiceComment commentService = new ServiceComment();
                        commentService.update(c);
                        commentContentBox.getChildren().remove(editBox);
                        Label newContentLabel = new Label(c.getContent());
                        newContentLabel.setWrapText(true);
                        newContentLabel.setStyle("-fx-font-size: 12px;");
                        commentContentBox.getChildren().add(index, newContentLabel);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            });

            btnSupprimer.setOnAction(e -> {
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce commentaire ?", ButtonType.YES, ButtonType.NO);
                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            ServiceComment commentService = new ServiceComment();
                            commentService.delete(c);
                            commentBox.getParent().getChildrenUnmodifiable().remove(commentBox);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            });

            actionButtonsBox.getChildren().addAll(btnModifier, btnSupprimer);
        }

        commentBox.getChildren().addAll(commenterImageView, commentContentBox, actionButtonsBox);
        return commentBox;
    }

    private VBox createCommentsContainer(Post post, VBox postBox, Button toggleCommentsBtn, Label commentCountLabel) {
        VBox commentsContainer = new VBox();
        commentsContainer.setSpacing(5);
        commentsContainer.setStyle("-fx-padding: 5; -fx-background-color: #e9e9e9; -fx-border-color: #ccc;");
        commentsContainer.setId("commentsContainer");

        ServiceComment commentService = new ServiceComment();
        try {
            List<Comment> comments = commentService.getCommentsByPostId(post.getId());
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
            TextField searchGifField = new TextField();
            searchGifField.setPromptText("Rechercher un GIF...");
            Button searchGifButton = new Button("Rechercher");
            VBox searchBox = new VBox(5, searchGifField, searchGifButton);
            TilePane gifResultsPane = new TilePane();
            gifResultsPane.setHgap(5);
            gifResultsPane.setVgap(5);
            VBox gifSearchContainer = new VBox(10, searchBox, gifResultsPane);

            searchGifButton.setOnAction(event -> {
                try {
                    String query = URLEncoder.encode(searchGifField.getText(), "UTF-8");
                    String apiKey = "YOUR_GIPHY_API_KEY"; // Remplacez par votre clé API Giphy
                    URL url = new URL("https://api.giphy.com/v1/gifs/search?api_key=" + apiKey + "&q=" + query + "&limit=10");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() == 200) {
                        Scanner scanner = new Scanner(conn.getInputStream());
                        StringBuilder response = new StringBuilder();
                        while (scanner.hasNextLine()) {
                            response.append(scanner.nextLine());
                        }
                        scanner.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONArray data = jsonResponse.getJSONArray("data");
                        gifResultsPane.getChildren().clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject gif = data.getJSONObject(i);
                            String gifUrl = gif.getJSONObject("images").getJSONObject("fixed_height").getString("url");
                            ImageView gifImageView = new ImageView(new Image(gifUrl));
                            gifImageView.setFitWidth(150);
                            gifImageView.setPreserveRatio(true);
                            gifImageView.setOnMouseClicked(clickEvent -> {
                                TFNewComment.setText("[GIF] " + gifUrl);
                                gifSearchContainer.setVisible(false);
                            });
                            gifResultsPane.getChildren().add(gifImageView);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            gifSearchContainer.setVisible(false);
            commentsContainer.getChildren().add(gifSearchContainer);
            btnAjouterGIF.setOnAction(event -> gifSearchContainer.setVisible(!gifSearchContainer.isVisible()));
        });

        btnAjouterComment.setOnAction(e -> {
            String content = TFNewComment.getText();
            if (!content.isEmpty()) {
                try {
                    UserSession session = UserSession.getInstance();
                    int currentUserId = session.getId();
                    Timestamp createdAt = new Timestamp(System.currentTimeMillis());
                    Comment newComment = new Comment(0, post.getId(), currentUserId, content, createdAt);
                    ServiceComment commentService2 = new ServiceComment();
                    commentService2.ajouter(newComment);
                    TFNewComment.clear();
                    HBox commentBox = createCommentBox(newComment);
                    commentsContainer.getChildren().add(commentBox);
                    int newCount = servicePost.getCommentCount(post.getId());
                    commentCountLabel.setText("Commentaires: " + newCount);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        addCommentBox.getChildren().addAll(TFNewComment, btnAjouterComment, btnAjouterGIF);
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
        UserSession session = UserSession.getInstance();
        int currentUserId = session.getId();

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
}
