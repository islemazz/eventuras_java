package entities;

import java.time.LocalDateTime;
import java.util.List;

public class Partner {
    private int id;
    private String name;
    private PartnerType type;
    private String description;
    private String email;
    private String phone;
    private String address;
    private String website;
    private double rating;
    private int ratingCount;
    private String imagePath;
    private String videoPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Partnership> partnerships;

    public Partner() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Partner(int id, String name, PartnerType type, String description, String email, String phone, 
                  String address, String website, double rating, int ratingCount, String imagePath, 
                  String videoPath, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.website = website;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PartnerType getType() { return type; }
    public void setType(PartnerType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Partnership> getPartnerships() { return partnerships; }
    public void setPartnerships(List<Partnership> partnerships) { this.partnerships = partnerships; }

    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
