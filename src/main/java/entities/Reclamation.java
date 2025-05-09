package entities;

import java.util.List;

public class Reclamation {

    private int id, id_user, id_event;
    private String created_at, description, subject, status;
    private List<ReclamationAttachment> attachments; // Store list of attachment objects

    public Reclamation() {}

    public Reclamation(int id_user, int id_event, String created_at, String subject, String description, List<ReclamationAttachment> attachments) {
        this.id_user = id_user;
        this.id_event = id_event;
        this.created_at = created_at;
        this.subject = subject;
        this.description = description;
        this.attachments = attachments;
        this.status = "En attente";
    }

    public Reclamation(int id, String subject, String description, List<ReclamationAttachment> attachments) {
        this.id = id;
        this.subject = subject;
        this.description = description;
        this.attachments = attachments;
        this.status = "En attente"; // Default status

    }

    public Reclamation(int id, int id_user, int id_event, String created_at, String subject, String description, String status, List<ReclamationAttachment> attachments) {
        this.id = id;
        this.id_user = id_user;
        this.id_event = id_event;
        this.created_at = created_at;
        this.subject = subject;
        this.description = description;
        this.attachments = attachments;
        this.status = status; // Default status

    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", id_user=" + id_user +
                ", id_event=" + id_event +
                ", created_at='" + created_at + '\'' +
                ", subject='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", attachments=" + attachments +
                '}';
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public int getId_event() {
        return id_event;
    }

    public void setId_event(int id_event) {
        this.id_event = id_event;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<ReclamationAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ReclamationAttachment> attachments) {
        this.attachments = attachments;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}