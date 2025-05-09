package services;

import entities.Reservation;
import utils.MyConnection;


import entities.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service implements IService1<Reservation> {
    private Connection connection;
    public Service() {
        this.connection= MyConnection.getInstance().getConnection();
    }
    @Override
    public void ajouter(Reservation reservation) throws SQLException {



        Ticket ticket = new Ticket(0,"TICKET-" + System.currentTimeMillis(), "A" + (int)(Math.random() * 100));
        List<Reservation> reservations = new ArrayList<>();

        Reservation reservationWithTicket = new Reservation(
                reservation.getEvent_Id(),
                reservation.getUser_Id(),
                reservation.getStatus(),
                reservation.getNbPlaces(),
                reservation.getPrix(),
                ticket
        );

        reservations.add(reservationWithTicket);

        String sqlTicket = "INSERT INTO ticket (ticketCode, seatNumber) VALUES (?, ?)";
        String sqlReservation = "INSERT INTO reservation (event_Id,user_Id,status,NbPlaces,prix,ticket_id) VALUES (?,?,?,?, ?,?)";

        try (PreparedStatement pstTicket = connection.prepareStatement(sqlTicket, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstReservation = connection.prepareStatement(sqlReservation, Statement.RETURN_GENERATED_KEYS)) {


            pstTicket.setString(1, reservationWithTicket.getTicket().getTicketCode());
            pstTicket.setString(2, reservationWithTicket.getTicket().getSeatNumber());
            pstTicket.executeUpdate();


            ResultSet rsTicket = pstTicket.getGeneratedKeys();
            if (rsTicket.next()) {
                int ticketId = rsTicket.getInt(1);
                reservationWithTicket.getTicket().setTicketId(ticketId);
            }

            pstReservation.setInt(1,reservationWithTicket.getEvent_Id());
            pstReservation.setInt(2,reservationWithTicket.getUser_Id());
            pstReservation.setString(3, reservationWithTicket.getStatus());
            pstReservation.setInt(4,reservationWithTicket.getNbPlaces());
            pstReservation.setDouble(5, reservationWithTicket.getPrix());
            pstReservation.setInt(6, reservationWithTicket.getTicket().getTicketId());
            pstReservation.executeUpdate();
        }
    }

    public void update(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservation SET prix = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setDouble(1, reservation.getPrix());
            pst.setInt(2, reservation.getId());
            pst.executeUpdate();
        }
    }

    public void delete(String ticketcode) throws SQLException {
        String deleteReservationSql = "DELETE FROM reservation WHERE ticket_id = (SELECT ticket_id FROM ticket WHERE ticketCode = ? )";
        String deleteTicketSql = "DELETE FROM ticket WHERE ticketCode = ?";

        try (PreparedStatement pstReservation = connection.prepareStatement(deleteReservationSql);
             PreparedStatement pstTicket = connection.prepareStatement(deleteTicketSql)) {

            pstReservation.setString(1, ticketcode);
            pstReservation.executeUpdate();

            // Suppression du ticket lui-même
            pstTicket.setString(1, ticketcode);
            pstTicket.executeUpdate();
        }
    }





    public Map<Reservation, Ticket> afficherAll() throws SQLException {
        Map<Reservation, Ticket> reservations = new HashMap<>();
        String sql ="SELECT r.status,r.NbPlaces, r.prix, r.ticket_id, " +
                "t.ticketCode, t.seatNumber " +
                "FROM reservation r " +
                "LEFT JOIN ticket t ON r.ticket_id = t.ticket_id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String status = rs.getString("status");
                int NbPlaces = rs.getInt("NbPlaces");
                double prix = rs.getDouble("prix");
                int ticketId = rs.getInt("ticket_id");
                String ticketCode = rs.getString("ticketCode");
                String seatNumber = rs.getString("seatNumber");

                Reservation reservation = new Reservation(status,NbPlaces, prix);
                Ticket ticket = null;

                if (ticketId > 0) {
                    ticket = new Ticket(ticketCode, seatNumber);
                    reservation.setTicket(ticket);
                }

                reservations.put(reservation, ticket);
            }
        }
        return reservations;
    }


    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getString("status"),
                        rs.getInt("NbPlaces"),
                        rs.getDouble("prix"),
                        rs.getInt("ticket_id")

                );
                reservations.add(reservation);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des réservations : " + e.getMessage());
        }

        return reservations;
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM ticket";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ticket ticket = new Ticket(
                        rs.getString("ticketCode"),
                        rs.getString("seatNumber")

                );
                tickets.add(ticket);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des réservations : " + e.getMessage());
        }

        return tickets;
    }


    public void update(String ticketCode, String newEtat, int newNbPlaces, double newPrix, String newSeatNumber) throws SQLException {

        String getTicketIdQuery = "SELECT ticket_id FROM ticket WHERE ticketCode = ?";

        int ticketId = -1;
        try (PreparedStatement getTicketIdStmt = connection.prepareStatement(getTicketIdQuery)) {
            getTicketIdStmt.setString(1, ticketCode);
            ResultSet rs = getTicketIdStmt.executeQuery();

            if (rs.next()) {
                ticketId = rs.getInt("ticket_id");
            } else {
                throw new SQLException("Aucun ticket trouvé avec le code : " + ticketCode);
            }
        }


        String updateReservationQuery = "UPDATE reservation SET status = ?, NbPlaces = ?, prix = ? WHERE ticket_id = ?";
        String updateTicketQuery = "UPDATE ticket SET seatNumber = ? WHERE ticket_id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement reservationStatement = connection.prepareStatement(updateReservationQuery);
                 PreparedStatement ticketStatement = connection.prepareStatement(updateTicketQuery)) {


                reservationStatement.setString(1, newEtat);
                reservationStatement.setInt(2, newNbPlaces);
                reservationStatement.setDouble(3, newPrix);
                reservationStatement.setInt(4, ticketId);
                reservationStatement.executeUpdate();


                ticketStatement.setString(1, newSeatNumber);
                ticketStatement.setInt(2, ticketId);
                ticketStatement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la mise à jour de la réservation et du ticket.", e);
        }
    }


    public Reservation getReservationWithTicketByTicketCode(String ticketCode) throws SQLException {
        String query = "SELECT r.Id AS reservation_id, r.status, r.prix, r.NbPlaces, r.ticket_id, " +
                "t.ticketCode, t.seatNumber " +
                "FROM reservation r " +
                "JOIN ticket t ON r.ticket_id = t.ticket_id " +
                "WHERE t.ticketCode = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, ticketCode);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("reservation_id"));
                reservation.setStatus(rs.getString("status"));
                reservation.setPrix(rs.getDouble("prix"));
                reservation.setNbPlaces(rs.getInt("NbPlaces"));


                Ticket ticket = new Ticket();
                ticket.setTicketCode(rs.getString("ticketCode"));
                ticket.setSeatNumber(rs.getString("seatNumber"));


                reservation.setTicket(ticket);

                return reservation;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération de la réservation et du ticket.", e);
        }
    }






}