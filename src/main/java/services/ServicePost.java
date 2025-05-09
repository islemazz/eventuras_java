package services;

import entities.Post;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;

public class ServicePost implements IService<Post> {
    private Connection cnx;

    public ServicePost(){
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Post post) throws SQLException {
        String sql = "INSERT INTO post (title, content, created_at, user_id, image_path) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, post.getTitle());
        pst.setString(2, post.getContent());
        pst.setTimestamp(3, new java.sql.Timestamp(post.getCreated_at().getTime()));
        pst.setInt(4, post.getUser_id());
        pst.setString(5, post.getImage_path());

        pst.executeUpdate();
        System.out.println("Post added successfully!");
    }

    @Override
    public void update(Post post) throws SQLException {
        String sql = "UPDATE post SET title = ?, content = ?, image_path = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setString(1, post.getTitle());
        pst.setString(2, post.getContent());
        pst.setString(3, post.getImage_path());
        pst.setInt(4, post.getId());
        pst.executeUpdate();
    }

    @Override
    public void delete(Post post) throws SQLException {
        String sql = "DELETE FROM post WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, post.getId());
        pst.executeUpdate();
    }

    @Override
    public ArrayList<Post> afficherAll() throws SQLException {
        ArrayList<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post ORDER BY created_at DESC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            String content = rs.getString("content");
            Date created_at = new Date(rs.getTimestamp("created_at").getTime());
            int user_id = rs.getInt("user_id");
            String image_path = rs.getString("image_path");

            Post post = new Post(id, title, content, created_at, user_id, image_path);
            posts.add(post);
        }
        return posts;
    }

    public int getCommentCount(int postId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM comment WHERE post_id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, postId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }
}
