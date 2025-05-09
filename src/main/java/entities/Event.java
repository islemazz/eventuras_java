package entities;

import javafx.scene.control.TextField;
import utils.Session;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Event  {
    private int id_event;
    private String title;
    private String description;
    private Date date_event;
    private String location;
    private int user_id;
    private int category_id;
    private String category_name;
    private String image;
    private Timestamp creation_date;//nzid nchouf aleha
    public List<String> activiteList;
    public String Status;

    public Event(int id, String title, String description, java.sql.Date date_event, String location, int user_id, int category_id, String category_name, List<String> activiteList){
    this.activiteList = new ArrayList<>();
    }
    user currentUser = Session.getInstance().getCurrentUser();
    public Event(String title, String description, Date date_event, String location, int user_id, String category_name) {
        this.title = title;
        this.description = description;
        this.date_event = date_event;
        this.location = location;
        this.user_id = user_id;
        this.category_name = category_name;

    }

    public Event(int id_event) {
        this.id_event = id_event;
    }

    public Event(int id_event,String title, String description, Date date_event, String location,int user_id, int category_id ,String category_name,String image) {
        this.id_event = id_event;
        this.title = title;
        this.description = description;
        this.date_event = date_event;
        this.location = location;
        this.user_id = user_id;
        this.category_id = category_id;
        this.category_name = category_name;
        this.image = image;
        this.activiteList = activiteList;

    }
    public Event(int id_event,String title, String description, Date date_event, String location,int user_id, int category_id,String image) {
        this.id_event = id_event;
        this.title = title;
        this.description = description;
        this.date_event = date_event;
        this.location = location;
        this.user_id = user_id;
        this.category_id = category_id;
        this.image = image;
        this.activiteList = activiteList;

    }

    public Event(String title, String description, Date date_event, String location,int user_id, int category_id, String category_name,String image) {
        this.title = title;
        this.description = description;
        this.date_event = date_event;
        this.location = location;
        this.user_id = user_id;
        this.category_id = category_id;
        this.category_name = category_name;
        this.image = image;
        this.activiteList = activiteList;

    }
    public Event(String title, String description, Date date_event, String location,int user_id, int category_id,String image) {
        this.title = title;
        this.description = description;
        this.date_event = date_event;
        this.location = location;
        this.user_id = user_id;
        this.category_id = category_id;
        this.image = image;
        this.activiteList = activiteList;
    }

    /*-------Pour Modification-------*/
    public Event(int id_event,String title){
        this.id_event = id_event;
        this.title = title;


    }
    /*Event event = new Event(title,description,date_event,location,price,category_name);*/
    public Event(String title,String dscription,Date date_event,String location,String category_name){
        this.title = title;
        this.description = dscription;
        this.date_event = date_event;
        this.location = location;
        this.category_name = category_name;
        this.activiteList = activiteList;
    }
    public Event(String title, String desc, String loc, String date, String categ) {
        this.title = title;
        this.description = desc;
        this.location = loc;
        try {
            this.date_event = java.sql.Date.valueOf(date); // Conversion sécurisée
        } catch (IllegalArgumentException e) {
            System.err.println("Format de date invalide : " + date);
            this.date_event = null;
        }
        this.category_name = categ;
        this.activiteList = activiteList;
    }

    public Event(String title, String desc, Date date, String loc, int userId, int categoryId) {
        this.title = title;
        this.description = desc;
        this.location = loc;
        this.date_event = java.sql.Date.valueOf(String.valueOf(date));
        this.user_id = user_id;
        this.category_id = categoryId;
        this.activiteList = activiteList;
    }
    public Event(String title,String description,String image){
        this.title = title;
        this.description = description;
        this.image = image;
        this.activiteList = activiteList;

    }

    public Event(int idEvent, TextField titleMod) {
        this.id_event=idEvent;
        this.title=titleMod.getText();
    }

    public Event(int id_event,String title, String description, java.sql.Date dateEvent,String categoryName) {
        this.id_event=id_event;
        this.title=title;
        this.description=description;
        this.date_event=dateEvent;
        this.category_name=categoryName;
        this.activiteList = activiteList;
    }

    public Event(int id, String title, String description, java.sql.Date dateEvent, String location, int userId, int categoryId, String categoryName, List<String> activiteList,String status) {
        this.id_event=id;
        this.title=title;
        this.description=description;
        this.date_event=dateEvent;
        this.location=location;
        this.user_id = user_id;
        this.category_id=categoryId;
        this.category_name=categoryName;
        this.activiteList=activiteList;
        this.Status=status;
    }

  /*  public Event(String titleEvent, String descEve, Date dateEve, String locEve, int id,String category_name, String imagePath, Double prixEve, String activities) {
        this.title = titleEvent;
        this.description = descEve;
        this.date_event = dateEve;
        this.location = locEve;
        this.user_id=currentUser.getId();
        this.category_id=id;
        this.category_name=category_name;
        this.image=imagePath;
        this.price=prixEve;
    }*/

    public int getId_event() {
        return id_event;
    }

    public void setId_event(int id_event) {
        this.id_event = id_event;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate_event() {
        return date_event;
    }

    public void setDate_event(Date date_event) {
        this.date_event = date_event;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getImage(){return image;}

    public void setImage(String image){this.image = image;}

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }
    public List<String> getActivities() {
        return activiteList;
    }

    public void setActivities(List<String> activities) {
        this.activiteList = activities;
    }

    // Add an activity to the list
    public void addActivity(String activityName) {
        this.activiteList.add(activityName);
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @Override
    public String toString() {
        return "Event{" +
               "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date_event='" + date_event + '\'' +
                ", location='" + location + '\'' +
                ", user_id=" + user_id +
                ", category=" + category_id +
                ", category_name='" + category_name +
                 ", image='" + image +
                ", activiteList=" + activiteList +
                '}';
    }

}
