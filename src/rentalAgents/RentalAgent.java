package rentalAgents;

import props.*;
import rabbitMQ.*;
import buildings.*;
import reservations.Reservation;
import reservations.ReservationManager;

import java.util.List;
import java.util.stream.Collectors;

public class RentalAgent {

    private Receiver receiver;
    private Sender sender;

    public RentalAgent() {
        this.receiver = new Receiver();
        this.sender = new Sender();
    }

    /**
     * Begins listening for responses in RENTAL_AGENT_QUEUE
     */
    public void startListening() {
        try {
            receiver.receiveDirectMessage(RoutingConfig.RENTAL_AGENT_QUEUE.getValue(), RoutingConfig.RENTAL_AGENT_KEY.getValue(), this::handleReceivedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles different types of received messages.
     *
     * @param message Message received from the listener.
     */
    private void handleReceivedMessage(Message message) {
        switch (message.getType()) {
            case MessageType.REQUEST_BUILDINGS:
                handleRequestBuildings();
                break;
            case MessageType.MAKE_RESERVATION:
                handleMakeReservation(message);
                break;
                //responses from building
            case MessageType.CONFIRM_RESERVATION,
                    MessageType.RESERVATION_CANCELLED,
                    MessageType.ROOM_NOT_BOOKED,
                    MessageType.ROOM_NOT_FOUND,
                    MessageType.ALREADY_BOOKED:
                passTheMessageFromBuildingToClient(message);
                break;
            case MessageType.CANCEL_RESERVATION:
                handleCancelReservation(message);
                break;
            default:
                break;
        }

    }

    public void registerBuilding(int numberOfRooms){
        try {
            Message message = new Message(MessageType.CREATE_BUILDING, numberOfRooms);
            sender.sendFanoutMessage(RabbitMQExchanges.FANOUT_BUILDINGS, message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the List<Building> to List<String>
     */
    private List<String> convertBuildingsToStringList() {
        return BuildingManager.getBuildings().stream().map(building -> {
            String buildingInfo = "Building: " + building.getBuildingName();
            String roomsInfo = building.getRooms().stream()
                    .map(room -> "    " + room.getRoomName() + " - " + (room.isBooked() ? "Booked" : "Available"))
                    .collect(Collectors.joining("\n"));
            return buildingInfo + "\n" + roomsInfo;
        }).collect(Collectors.toList());
    }

    /**
     * Handles the request to provide a list of all available buildings by sending a response with the building names.
     */
    private void handleRequestBuildings() {
        List<String> buildingNames = convertBuildingsToStringList();
        Message response = new Message(MessageType.RESPONSE_BUILDINGS, buildingNames);
        try {
            sender.sendDirectMessage(RoutingConfig.CLIENT_QUEUE.getValue(), RoutingConfig.CLIENT_KEY.getValue(), response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the action of making a reservation based on a received message containing reservation details.
     *
     * @param message The message containing reservation details.
     */
    private void handleMakeReservation(Message message) {
        Reservation reservation = createReservationFromMessage(message);
        Building building = BuildingManager.getBuildingByName(reservation.getBuildingName());

        if (building == null) {
            sendBuildingNotFoundMessage(reservation.getBuildingName());
        } else {
            handleBuildingReservation(building, reservation);
        }
    }

    /**
     * Manages the reservation, creating reservations and notifying buildings.
     *
     * @param building     The building where the reservation is to be made.
     * @param reservation  The reservation details.
     */
    private void handleBuildingReservation(Building building, Reservation reservation) {
        try {
            ReservationManager.createReservations(reservation);
            sendBuildingReservationRequest(building, reservation);
            sendClientReservationNumber(reservation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a reservation request to a specific building's queue.
     *
     * @param building     The building to which the reservation request is sent.
     * @param reservation  The reservation details to be sent.
     * @throws Exception if there is a failure in message sending.
     */
    private void sendBuildingReservationRequest(Building building, Reservation reservation) throws Exception {
        Message buildingRequest = new Message(MessageType.MAKE_RESERVATION, reservation.getRoomName());
        String buildingQueueName = RoutingConfig.BUILDING_QUEUE.getValue() + building.getBuildingID();
        String buildingKey = RoutingConfig.BUILDING_KEY.getValue() + building.getBuildingID();
        sender.sendDirectMessage(buildingQueueName, buildingKey, buildingRequest);
    }

    /**
     * Sends the client their reservation number after making a reservation.
     *
     * @param reservation The reservation which has been made.
     * @throws Exception if there is a failure in message sending.
     */
    private void sendClientReservationNumber(Reservation reservation) throws Exception {
        sender.sendDirectMessage(RoutingConfig.CLIENT_QUEUE.getValue(), RoutingConfig.CLIENT_KEY.getValue(),
                new Message(MessageType.RESERVATION_NUMBER, reservation.getReservationNumber()));
    }

    /**
     * Handles the cancellation of a reservation based on the reservation number provided in the message payload.
     *
     * @param message The message containing the reservation number to cancel.
     */
    private void handleCancelReservation(Message message) {
        try {
            String reservationNumber = (String) message.getPayload();
            Reservation reservation = ReservationManager.getReservationByNumber(reservationNumber);

            if (reservation == null) {
                // Handle case where the reservation was not found
                 sendReservationNotFoundMessage(reservationNumber);
            } else {
                // If the building is found, proceed with cancellation
                Building building = BuildingManager.getBuildingByName(reservation.getBuildingName());
                if (building == null){
                    sendBuildingNotFoundMessage(reservation.getBuildingName());
                } else {
                    sendBuildingCancelRequest(building, reservation);
                    ReservationManager.cancelReservation(reservation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends a cancellation request to the specific building's queue for a reservation.
     *
     * @param building     The building where the cancellation request needs to be sent.
     * @param reservation  The reservation that is to be cancelled.
     * @throws Exception if there is a failure in message sending.
     */
    private void sendBuildingCancelRequest(Building building, Reservation reservation) throws Exception {
        Message buildingRequest = new Message(MessageType.CANCEL_RESERVATION, reservation.getRoomName());
        String buildingQueueName = RoutingConfig.BUILDING_QUEUE.getValue() + building.getBuildingID();
        String buildingKey = RoutingConfig.BUILDING_KEY.getValue() + building.getBuildingID();
        sender.sendDirectMessage(buildingQueueName, buildingKey, buildingRequest);
    }

    /**
     * Creates a Reservation object from the provided message payload.
     *
     * @param message The message containing reservation details.
     * @return The created Reservation object.
     */
    private Reservation createReservationFromMessage(Message message) {
        String payload = (String) message.getPayload();
        String[] parts = payload.split(",", 3);
        return new Reservation(parts[0], parts[1], parts[2]);
    }

    /**
     * Sends a message to the client indicating that the specified building was not found.
     *
     * @param buildingName The name of the building that was not found.
     */
    private void sendBuildingNotFoundMessage(String buildingName) {
        try {
            Message message = new Message(MessageType.BUILDING_NOT_FOUND, "The building " + buildingName + " does not exist.");
            sender.sendDirectMessage(RoutingConfig.CLIENT_QUEUE.getValue(), RoutingConfig.CLIENT_KEY.getValue(), message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Forwards messages received from the building to the client.
     *
     * @param message The message to be forwarded to the client.
     */
    private void passTheMessageFromBuildingToClient(Message message) {
        try {
            sender.sendDirectMessage(RoutingConfig.CLIENT_QUEUE.getValue(), RoutingConfig.CLIENT_KEY.getValue(), message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a message to the client indicating that the reservation with the specified number was not found.
     *
     * @param reservationNumber The reservation number that was not found.
     */
    private void sendReservationNotFoundMessage(String reservationNumber) {
        try {
            Message message = new Message(MessageType.RESERVATION_CANT_FOUND, reservationNumber + " numbered reservation cant be found in the system.");
            sender.sendDirectMessage(RoutingConfig.CLIENT_QUEUE.getValue(), RoutingConfig.CLIENT_KEY.getValue(), message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
