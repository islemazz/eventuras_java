package entities;

public class ReclamationAttachment {

    private int id;
    private int reclamationId; // ID of the related reclamation
    private String filePath;   // Path to the stored file

    public ReclamationAttachment() {}

    public ReclamationAttachment(int id, int reclamationId, String filePath) {
        this.id = id;
        this.reclamationId = reclamationId;
        this.filePath = filePath;
    }

    public ReclamationAttachment(int reclamationId, String filePath) {
        this.reclamationId = reclamationId;
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "ReclamationAttachment{" +
                "id=" + id +
                ", reclamationId=" + reclamationId +
                ", filePath='" + filePath + '\'' +
                '}';
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReclamationId() {
        return reclamationId;
    }

    public void setReclamationId(int reclamationId) {
        this.reclamationId = reclamationId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
