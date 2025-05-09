package services;

import entities.Reservation;


import entities.Ticket;

import java.sql.SQLException;
import java.util.Map;

public interface IService1<T> {
    void ajouter(T t) throws SQLException;
    void update(String ticketCode, String newEtat, int newNbPlaces, double newPrix, String newSeatNumber) throws SQLException;
    void delete(String ticketCode) throws SQLException;
    Map<Reservation, Ticket> afficherAll() throws SQLException;
    Reservation getReservationWithTicketByTicketCode(String ticketCode) throws SQLException;

}