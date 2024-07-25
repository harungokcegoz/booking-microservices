package reservations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReservationManager {
    private static final Map<String, Reservation> reservations = new HashMap<>();

    public static void createReservations(Reservation reservation) {
        reservation.confirmReservation();
        reservations.put(reservation.getReservationNumber(), reservation);
    }


    /**
     * Cancels a reservation with the given reservation number.
     *
     * @param reservationPassed The reservation object that contains the number of the reservation to be cancelled.
     */
    public static void cancelReservation(Reservation reservationPassed) {
        Reservation reservation = reservations.get(reservationPassed.getReservationNumber());

        if (reservation != null && reservation.getStatus() != ReservationStatus.CANCELLED) {
            reservation.cancelReservation();
            // Use the reservation number to remove the reservation from the map
            reservations.remove(reservation.getReservationNumber());
        }
    }

    /**
     * Retrieves all reservations made by a specific user.
     * This method filters through all reservations stored in the system and returns a list
     * of reservations that match the given username. If no reservations are found for the user,
     * an empty list is returned.
     *
     * @param username The username of the user whose reservations are to be retrieved.
     * @return A List of {@code Reservation} objects associated with the given username.
     */
    public static List<Reservation> getReservationsByUser(String username) {
        return reservations.values().stream()
                .filter(reservation -> username.equals(reservation.getUsername()))
                .collect(Collectors.toList());
    }


    /**
     * Retrieves a reservation by its reservation number.
     *
     * @param reservationNumber The unique number of the reservation.
     * @return The reservation corresponding to the reservation number, or null if not found.
     */
    public static Reservation getReservationByNumber(String reservationNumber) {
        return reservations.get(reservationNumber);
    }

}
