package entities;
import java.util.ArrayList;
import java.util.List;

public class ReclamationConversation {
    private int id;
    private int reclamationId;
    private String createdAt;
    private String status; // "active", "locked", "closed"
    private List<ConversationMessage> messages; // ✅ Store messages within the conversation

    public ReclamationConversation() {
        this.messages = new ArrayList<>(); // Initialize messages list
    }

    public ReclamationConversation(int id, int reclamationId, String createdAt, String status) {
        this.id = id;
        this.reclamationId = reclamationId;
        this.createdAt = createdAt;
        this.status = status;
        this.messages = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ConversationMessage> getMessages() { return messages; }
    public void setMessages(List<ConversationMessage> messages) { this.messages = messages; }

    public void addMessage(ConversationMessage message) {
        this.messages.add(message);
    }


}
