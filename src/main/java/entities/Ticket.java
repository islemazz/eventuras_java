package entities;



public class Ticket {

    private int ticketId;
    private String ticketCode;
    private String seatNumber;

    private Reservation reservation;
    public Ticket(){}
    public Ticket(String ticketCode,String seatNumber) {
        this.ticketCode = ticketCode;
        this.seatNumber = seatNumber;
    }

    public Ticket(int ticketId, String ticketCode,String seatNumber) {
        this.ticketId = ticketId;
        this.ticketCode = ticketCode;
        this.seatNumber = seatNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }
    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    @Override
    public String toString() {
        return
                ticketCode +
                        seatNumber ;

    }
}