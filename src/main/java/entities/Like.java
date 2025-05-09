package entities;

import java.util.Date;

public class Like {
    private int id;
    private int userId;
    private int postId;
    private Date createdAt;

    public Like(int id, int userId, int postId, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }

    public Like(int userId, int postId) {
        this.userId = userId;
        this.postId = postId;
    }

    // Getters et setters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getPostId() { return postId; }
    public Date getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setPostId(int postId) { this.postId = postId; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
