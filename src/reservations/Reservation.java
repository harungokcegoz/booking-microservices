package reservations;

import java.util.*;

public class Reservation {
    private String reservationNumber;
    private String buildingName;
    private String username;
    private String roomName;
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(String username, String buildingName, String roomName) {
        this.reservationNumber = UUID.randomUUID().toString();
        this.buildingName = buildingName;
        this.roomName = roomName;
        this.status = ReservationStatus.PENDING;
        this.username = username;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }


    public String getUsername() {
        return username;
    }
    public void confirmReservation() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancelReservation() {
        this.status = ReservationStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationNumber:'" + reservationNumber + '\'' +
                ", buildingName:'" + buildingName + '\'' +
                ", roomName:'" + roomName + '\'' +
                ", status:" + status +
                '}';
    }

}

enum ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
