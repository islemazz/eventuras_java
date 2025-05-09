package entities;

import java.util.List;

public class Partner {
    private int id;
    private String name;
    private PartnerType type;
    private String contactInfo;
    private String ImagePath;
    private List<Partnership> partnerships;


    public Partner(int id, String name, PartnerType type, String contactInfo, String ImagePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.contactInfo = contactInfo;
        this.ImagePath = ImagePath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PartnerType getType() { return type; }
    public void setType(PartnerType type) { this.type = type; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public List<Partnership> getPartnerships() { return partnerships; }
    public void setPartnerships(List<Partnership> partnerships) { this.partnerships = partnerships; }

    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", contactInfo='" + contactInfo + '\'' +
                '}';
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }
}
