package entities;

public class Partnership {
    private int id;
    private int organizerId;
    private int partnerId;
    private ContractType contractType;
    private String description;
    private boolean isSigned;          // True if both parties have signed

    public Partnership( int organizerId, int partnerId, ContractType contractType, String description, boolean isSigned ) {

        this.organizerId = organizerId;
        this.partnerId = partnerId;
        this.contractType = contractType;
        this.description = description;
        this.isSigned = false;
    }

    public Partnership(int id, int organizerId, int partnerId, ContractType contractType, String description, boolean isSigned) {
        this.id = id;
        this.organizerId = organizerId;
        this.partnerId = partnerId;
        this.contractType = contractType;
        this.description = description;
        this.isSigned = isSigned;
    }


    // Getters and Setters
    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
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





    @Override
    public String toString() {
        return "Partnership{" +
                "organizerId=" + organizerId +
                ", partnerId=" + partnerId +
                ", contractType=" + contractType +
                ", description='" + description + '\'' +
                ", isSigned=" + isSigned +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

