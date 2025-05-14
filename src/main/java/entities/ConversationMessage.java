package entities;

import utils.Session;

import java.util.List;

public class ConversationMessage {
    private int id;
    private int conversationId;
    private int senderId;  // This should NOT be overridden with the current session user
    private String message;
    private String createdAt;
    private List<ConversationMessageAttachment> attachments;

    // ✅ Remove `currentUser.getId()` from constructor
    public ConversationMessage(int id, int conversationId, int senderId, String message, String createdAt, List<ConversationMessageAttachment> attachments) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;  // ✅ Keep the senderId from the database
        this.message = message;
        this.createdAt = createdAt;
        this.attachments = attachments;
    }

    public ConversationMessage(int conversationId, int senderId, String message, String createdAt, List<ConversationMessageAttachment> attachments) {
        this.conversationId = conversationId;
        this.senderId = senderId;  // ✅ Keep the senderId from input
        this.message = message;
        this.createdAt = createdAt;
        this.attachments = attachments;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getSenderId() { return senderId; }  // ✅ No override with currentUser
    public void setSenderId(int senderId) { this.senderId = senderId; }  // ✅ No override

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<ConversationMessageAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<ConversationMessageAttachment> attachments) { this.attachments = attachments; }
}
