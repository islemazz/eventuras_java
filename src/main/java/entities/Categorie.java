package entities;


public class Categorie {
    private int category_id;
    private String name;
    public Categorie(int category_id) {
        this.category_id = category_id;
    }
    public Categorie(int category_id, String name) {
        this.category_id = category_id;
        this.name = name;
    }
    public Categorie(String name) {
        this.name = name;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Categorie{" +
                "category_id=" + category_id +
                ", name='" + name + '\'' +
                '}';
    }

}
