package entities;

import utils.Session;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class Participation {
    private int id_part;
    private int user_id;
    private int event_id;
    private String status;
    private String activities;
    private Timestamp part_date;
    user currentUser = Session.getInstance().getCurrentUser();
    public Participation() {

    }
    public Participation(int id_part, int user_id, int event_id, String status) {
        this.id_part = id_part;
        this.user_id = currentUser.getId();
        this.event_id = event_id;
        this.status = status;
    }
    public Participation(int user_id, int event_id,String status) {
        this.user_id = currentUser.getId();
        this.event_id = event_id;
        this.status = status;
    }
    public Participation(int id_part, int user_id, int event_id, String status, String activities) {
        this.id_part = id_part;
        this.user_id = currentUser.getId();
        this.event_id = event_id;
        this.status = status;
        this.activities = activities;
    }


    public int getId_part() {
        return id_part;
    }

    public void setId_part(int id_part) {
        this.id_part = id_part;
    }

    public int getUser_id() {
      return  this.user_id = currentUser.getId();
    }

    public void setUser_id(int user_id) {
        this.user_id = currentUser.getId();
    }

    public int getEvent_id() {
        return event_id;
    }

    public void setEvent_id(int event_id) {
        this.event_id = event_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }
    public Timestamp getPart_date() {
        return part_date;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id_part=" + id_part +
                ", user_id=" + user_id +
                ", event_id=" + event_id +
                ", status=" + status +
                '}';
    }
}
