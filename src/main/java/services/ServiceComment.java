package services;

import entities.Comment;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceComment implements IService<Comment> {
    private Connection cnx;

    public ServiceComment() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Comment comment) throws SQLException {
        String sql = "INSERT INTO comment (post_id, user_id, content, created_at) VALUES (?, ?, ?, ?)";

        PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, comment.getPost_id());
        pst.setInt(2, comment.getUser_id());
        pst.setString(3, comment.getContent());
        pst.setDate(4, new java.sql.Date(comment.getCreated_at().getTime()));
        try {
            pst.executeUpdate();
            System.out.println("Comment added successfully!");
        } catch (SQLException e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    @Override
    public void update(Comment comment) throws SQLException {
        String sql = "UPDATE comment SET content = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setString(1, comment.getContent());
        pst.setInt(2, comment.getId());
        pst.executeUpdate();
    }

    @Override
    public void delete(Comment comment) throws SQLException {
        String sql = "DELETE FROM comment WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, comment.getId());
        pst.executeUpdate();
    }

    @Override
    public ArrayList<Comment> afficherAll() throws SQLException {
        ArrayList<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            int post_id = rs.getInt("post_id");
            int user_id = rs.getInt("user_id");
            String content = rs.getString("content");
            Date created_at = new Date(rs.getTimestamp("created_at").getTime());

            Comment comment = new Comment(id, post_id, user_id, content, created_at);
            comments.add(comment);
        }
        return comments;
    }

    public List<Comment> getCommentsByPostId(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE post_id = ? ORDER BY created_at DESC";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, postId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            int userId = rs.getInt("user_id");
            String content = rs.getString("content");
            Timestamp timestamp = rs.getTimestamp("created_at");
            Date createdAt = new Date(timestamp.getTime());
            comments.add(new Comment(id, postId, userId, content, createdAt));
        }
        return comments;
    }

}
