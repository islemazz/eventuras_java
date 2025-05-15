package entities;

import java.time.LocalDateTime;

public class Partnership {
    private int id;
    private int partnerId;
    private int organizerId;
    private String contractType;
    private String description;
    private boolean isSigned;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String signedContractFile;
    private LocalDateTime signedAt;
    private String generatedContractPath;

    // Default constructor
    public Partnership() {
    }

    // Constructor with all fields
    public Partnership(int id, int partnerId, int organizerId, String contractType, 
                      String description, boolean isSigned, String status, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.organizerId = organizerId;
        this.contractType = contractType;
        this.description = description;
        this.isSigned = isSigned;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor without id (for new partnerships)
    public Partnership(int partnerId, int organizerId, String contractType, 
                      String description, boolean isSigned, String status) {
        this.partnerId = partnerId;
        this.organizerId = organizerId;
        this.contractType = contractType;
        this.description = description;
        this.isSigned = isSigned;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(boolean signed) {
        isSigned = signed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSignedContractFile() {
        return signedContractFile;
    }

    public void setSignedContractFile(String signedContractFile) {
        this.signedContractFile = signedContractFile;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public String getGeneratedContractPath() {
        return generatedContractPath;
    }

    public void setGeneratedContractPath(String generatedContractPath) {
        this.generatedContractPath = generatedContractPath;
    }

    @Override
    public String toString() {
        return "Partnership{" +
                "id=" + id +
                ", partnerId=" + partnerId +
                ", organizerId=" + organizerId +
                ", contractType='" + contractType + '\'' +
                ", description='" + description + '\'' +
                ", isSigned=" + isSigned +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", signedContractFile='" + signedContractFile + '\'' +
                ", signedAt=" + signedAt +
                ", generatedContractPath='" + generatedContractPath + '\'' +
                '}';
    }
}

