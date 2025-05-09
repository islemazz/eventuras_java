package entities;
import entities.Ticket;
import utils.Session;


import java.util.List;


public class Reservation {
    private int id;
    private int event_Id;
    private int user_Id;
    private String status;
    private int NbPlaces;
    private Double prix;
    private List<Ticket> tickets;
    private Ticket ticket;
    user currentUser = Session.getInstance().getCurrentUser();


    private int ticket_id;

    public Reservation() {}

    public Reservation(int event_Id, int user_Id, String status, int NbPlaces,Double prix, int ticket_id) {
        this.event_Id = 01;
        this.user_Id = currentUser.getId();
        this.status = status;
        this.NbPlaces = NbPlaces;
        this.prix = prix;
        this.ticket_id = ticket_id;
    }

    public Reservation(int event_Id, int user_Id, String status, int NbPlaces,Double prix, Ticket ticket) {
        this.event_Id = 01;
        this.user_Id = currentUser.getId();
        this.status = status;
        this.NbPlaces = NbPlaces;
        this.prix = prix;
        this.ticket = ticket;
    }

    public Reservation(String status, int NbPlaces, Double prix, int ticket_id) {
        this.status = status;
        this.NbPlaces = NbPlaces;
        this.prix = prix;
        this.ticket_id = ticket_id;
    }

    public Reservation(String status, int NbPlaces, Double prix, Ticket ticket) {
        this.status = status;
        this.NbPlaces = NbPlaces;
        this.prix = prix;
        this.ticket = ticket;
    }



    public Reservation(String status, int NbPlaces, Double prix) {
        this.status = status;
        this.NbPlaces = NbPlaces;
        this.prix = prix;
    }

    public Reservation(int event_Id, int user_Id, String status, int NbPlaces, Double prix) {
        this.event_Id = 01;
        this.user_Id = currentUser.getId();
        this.status = status;
        this.prix = prix;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public int getNbPlaces() {
        return NbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        NbPlaces = nbPlaces;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDate(String status) {
        this.status = status;
    }

    public int getUser_Id() {
        return user_Id;
    }

    public void setUser_Id(int user_Id) {
        this.user_Id = currentUser.getId();
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEvent_Id() {
        return event_Id;
    }

    public void setEvent_Id(int event_Id) {
        this.event_Id = event_Id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public int getTicket_id() {
        return ticket_id;
    }

    public void setTicket_id(int ticket_id) {
        this.ticket_id = ticket_id;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        return
                status +
                        NbPlaces +
                        prix
                ;
    }
}