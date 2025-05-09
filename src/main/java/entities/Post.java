package entities;

import java.sql.Timestamp;
import java.util.Date;

public class Post {
    private int id;
    private String title;
    private String content;
    private Date created_at;
    private int user_id;
    private String image_path;

    public Post(int id, String title, String content, Date created_at, int user_id, String image_path) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.created_at = created_at;
        this.user_id = user_id;
        this.image_path = image_path;
    }

    public Post(int id, String title, String content, Date created_at, int user_id) {
        this(id, title, content, created_at, user_id, ""); // Image path vide par défaut
    }


    /*-------Pour Modification-------*/
    public Post(int id, String title){
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", created_at='" + created_at + '\'' +
                ", user_id=" + user_id +
                ", image_path=" + image_path +
                '}';
    }

}
