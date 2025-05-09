package entities;

import java.util.Date;

public class Comment {
    private int id;
    private int post_id;
    private int user_id;
    private String content;
    private Date created_at;

    public Comment(int id, int post_id, int user_id, String content, Date created_at) {
        this.id = id;
        this.post_id = post_id;
        this.user_id = user_id;
        this.content = content;
        this.created_at = created_at;
    }

    /*-------Pour Modification-------*/
    public Comment(int id, String content){
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", post_id=" + post_id +
                ", user_id=" + user_id +
                ", content='" + content + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}
