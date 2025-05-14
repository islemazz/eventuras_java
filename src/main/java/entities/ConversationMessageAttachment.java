package entities;

public class ConversationMessageAttachment {
    private int id;
    private int messageId; // Links to ConversationMessage
    private String filePath;

    public ConversationMessageAttachment() {}


    public ConversationMessageAttachment(int id, int messageId, String filePath) {
        this.id = id;
        this.messageId = messageId;
        this.filePath = filePath;
    }


    public ConversationMessageAttachment(int reclamationId, String filePath) {
        this.messageId = reclamationId;
        this.filePath = filePath;
    }


    @Override
    public String toString() {
        return "ConversationMessageAttachment{" +
                "id=" + id +
                ", messageId=" + messageId +
                ", filePath='" + filePath + '\'' +
                '}';
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

}