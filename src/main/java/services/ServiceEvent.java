package services;

import entities.Event;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ServiceEvent implements IService<Event> {
    private Connection cnx;
    private ServiceParticipation serviceParticipation;

    public ServiceEvent(){
        cnx = MyConnection.getInstance().getConnection();
        serviceParticipation = new ServiceParticipation();
    }
    //Add
    @Override
    public void ajouter(Event event) throws SQLException {
        // Convert the list of activities to a comma-separated string
        String activitiesString = String.join(", ", event.getActivities());
        if (event.getDate_event() == null) {
            throw new IllegalArgumentException("La date de l'événement ne peut pas être null !");
        }

        // Update the SQL query to include the 'status' field
        String sql = "INSERT INTO event (title, description, date_event, location, user_id, category_id, image, price, activities, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, event.getTitle());
            pst.setString(2, event.getDescription());
            pst.setDate(3, new java.sql.Date(event.getDate_event().getTime()));
            pst.setString(4, event.getLocation());
            pst.setInt(5, event.getUser_id());
            pst.setInt(6, event.getCategory_id());
            pst.setString(7, event.getImage());
            pst.setString(8, activitiesString);
            pst.setString(9, "En cours"); // Set the status to "En cours"
            pst.executeUpdate();
            System.out.println("Event ajouté avec succès !");
        }
    }
    //Modify for Organiser
    @Override
    public void update(Event event) throws SQLException {
        String sql = "UPDATE event SET title = ?, description = ?, activities = ? WHERE id_event = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, event.getTitle());
            pst.setString(2, event.getDescription());
            pst.setString(3, String.join(", ", event.getActivities()));
            pst.setInt(4, event.getId_event());
            pst.executeUpdate();
            System.out.println("Event modifie avec succes!");
        }
    }
    //delete
    @Override
    public void delete(Event event) throws SQLException {
        String sql = "DELETE FROM event WHERE id_event = ?";
        PreparedStatement pst = cnx.prepareStatement(sql);
        pst.setInt(1, event.getId_event());
        pst.executeUpdate();
        System.out.println("Event supprime avec succes!");
    }
    //Affichage pour l'organisateur et l'admin
    public ArrayList<Event> afficherAllForOrg() throws SQLException {
        ArrayList<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event";
        Statement st = cnx.createStatement();
        st.executeQuery(sql);
        ResultSet rs = st.getResultSet();

        while (rs.next()) {
            int id = rs.getInt("id_event");
            String title = rs.getString("title");
            String description = rs.getString("description");
            Date date_event = rs.getDate("date_event");
            String location = rs.getString("location");
            int user_id = rs.getInt("user_id");
            int category_id = rs.getInt("category_id");
            String activitiesString = rs.getString("activities");
            String status =rs.getString("Status");// Retrieve the activities string

            // Convert the activities string to a List<String>
            List<String> activiteList = new ArrayList<>();
            if (activitiesString != null && !activitiesString.isEmpty()) {
                String[] activitiesArray = activitiesString.split(", ");
                for (String activity : activitiesArray) {
                    activiteList.add(activity.trim()); // Trim any extra spaces
                }
            }

            // Retrieve category name based on category_id
            String category_name = "";
            String categoryQuery = "SELECT name FROM categorie WHERE category_id = ?";
            PreparedStatement ps = cnx.prepareStatement(categoryQuery);
            ps.setInt(1, category_id);
            ResultSet rsCategory = ps.executeQuery();

            if (rsCategory.next()) {
                category_name = rsCategory.getString("name");
            }

            // Create the Event object with activiteList
            Event event = new Event(id, title, description, date_event, location, user_id, category_id, category_name, activiteList,status);
            events.add(event);
        }

        return events;
    }
    public ArrayList<Event> afficherAll() throws SQLException {
        ArrayList<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event where status='Accepté' ";
        Statement st = cnx.createStatement();
        st.executeQuery(sql);
        ResultSet rs = st.getResultSet();

        while (rs.next()) {
            // Retrieve event details
            int id_event = rs.getInt("id_event");
            String title = rs.getString("title");
            String description = rs.getString("description");
            Date date_event = rs.getDate("date_event");
            String location = rs.getString("location");
            int user_id = rs.getInt("user_id");
            int category_id = rs.getInt("category_id");
            String image = rs.getString("image");
            String activitiesString = rs.getString("activities"); // Retrieve the comma-separated activities string

            // Retrieve category name based on category_id
            String category_name = "";
            String categoryQuery = "SELECT name FROM categorie WHERE category_id = ?";
            PreparedStatement psCategory = cnx.prepareStatement(categoryQuery);
            psCategory.setInt(1, category_id);
            ResultSet rsCategory = psCategory.executeQuery();

            if (rsCategory.next()) {
                category_name = rsCategory.getString("name");
            }

            // Split the activities string into a list
            List<String> activities = new ArrayList<>();
            if (activitiesString != null && !activitiesString.isEmpty()) {
                String[] activitiesArray = activitiesString.split(", ");
                for (String activity : activitiesArray) {
                    activities.add(activity.trim()); // Trim any extra spaces
                }
            }

            // Create Event object with activities
            Event event = new Event(id_event, title, description, date_event, location, user_id, category_id, category_name, image);
            event.setActivities(activities); // Set the activities list

            events.add(event);
        }

        return events;
    }
    /*Pour afficher seulement les 3 derniers events dans home*/
    public ArrayList<Event> afficherLastEve() throws SQLException {
        ArrayList<Event> events = new ArrayList<>();
        String sql ="Select * from event where status = 'Accepté' order by creation_date desc limit 3 ";
        Statement st = cnx.createStatement();
        st.executeQuery(sql);
        ResultSet rs = st.getResultSet();
        while (rs.next()) {
            String title = rs.getString("title");
            String description = rs.getString("description");
            String image = rs.getString("image");
            Event event = new Event(title,description,image);
            events.add(event);
        }
        return events;
    }
    //update for Admin
    public void updateStatus(Event event) throws SQLException {
        String sql = "UPDATE event SET status ='Accepté' WHERE id_event = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, event.getStatus());
            pst.setInt(2, event.getId_event());
            pst.executeUpdate();
            System.out.println("Event modifie avec succes!");
        }
    }
    //Stats par mois:
    private int getMonthFromDate(java.util.Date date) {
        if (date == null) {
            throw new IllegalArgumentException("La date ne peut pas être null !");
        }
        // Convertir java.util.Date en java.sql.Date
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sqlDate); // Utiliser java.sql.Date
        return calendar.get(Calendar.MONTH) + 1; // +1 car les mois sont indexés à partir de 0
    }
    public Map<Integer, Long> calculateEventsPerMonth() throws SQLException {
        List<Event> events = afficherAllForOrg(); // Récupérer tous les événements
        return events.stream()
                .filter(e -> e.getDate_event() != null) // Filtrer les événements sans date
                .collect(Collectors.groupingBy(
                        e -> getMonthFromDate(e.getDate_event()), // Extraire le mois
                        Collectors.counting()
                ));
    }
    //stats par category:
    public Map<String, Long> calculateEventsByCategory() throws SQLException {
        List<Event> events = afficherAllForOrg(); // Récupérer tous les événements
        return events.stream()
                .collect(Collectors.groupingBy(
                        Event::getCategory_name,
                        Collectors.counting()
                ));
    }
    //stats nb participants par categorie:
    public Map<String, Integer> calculateParticipantsPerEventCategory() throws SQLException {
        List<Event> events = afficherAllForOrg(); // Récupérer tous les événements
        return events.stream()
                .collect(Collectors.groupingBy(
                        Event::getCategory_name, // Regrouper par catégorie
                        Collectors.summingInt(e -> {
                            try {
                                return serviceParticipation.getParticipantCount(e.getId_event()); // Utiliser ServiceParticipation
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                return 0;
                            }
                        })
                ));
    }

}
