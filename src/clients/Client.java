package clients;

import props.*;
import rabbitMQ.*;
import reservations.Reservation;
import reservations.ReservationManager;


import java.util.*;

public class Client {
    private final String username;
    private final Sender sender;
    private final Receiver receiver;

    public final String ANSI_GREEN = "\u001B[32m";
    public final String ANSI_RESET = "\u001B[0m";
    public final String ANSI_RED = "\u001B[31m";

    public Client(String username) {
        this.username = username;
        this.sender = new Sender();
        this.receiver = new Receiver();
    }

    /**
     * Begins listening for responses in CLIENT_RESPONSE_QUEUE.
     */
    public void startListening() throws Exception {
        try {
            receiver.receiveDirectMessage(RoutingConfig.CLIENT_QUEUE.getValue(), RoutingConfig.CLIENT_KEY.getValue(), this::handleReceivedMessage);
            startListeningToFanout();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Begins listening for fanout messages in a given exchange.
     */
    public void startListeningToFanout() {
        try {
            receiver.receiveFanoutMessage(RoutingConfig.CLIENT_NOTIFICATION_QUEUE.getValue(), this::handleFanoutMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles received fanout messages.
     *
     * @param message Message received from the fanout exchange.
     */
    private void handleFanoutMessage(Message message) {
        switch (message.getType()){
            case MessageType.BUILDING_CREATED:
                printGreen("Fanout Message Received: " + message.getPayload());
        }

    }


    /**
     * Handles different types of received messages.
     *
     * @param message Message received from the queue.
     */
    private void handleReceivedMessage(Message message) {
            switch (message.getType()) {
                case MessageType.RESPONSE_BUILDINGS:
                    printAvailableBuildings(message);
                    break;
                case MessageType.CONFIRM_RESERVATION:
                    printGreen(message.getPayload().toString());
                    break;
                case MessageType.RESERVATION_NUMBER:
                    printGreen("Your Reservation Number is: " + message.getPayload());
                    break;
                case MessageType.ROOM_NOT_FOUND:
                    printRed(message.getPayload() + " Input a valid room");
                    break;
                case MessageType.BUILDING_NOT_FOUND:
                    printRed(message.getPayload() + " Input a valid building");
                    break;
                case MessageType.ROOM_NOT_BOOKED:
                    printRed(message.getPayload() + " Pick a valid booking");
                    break;
                case MessageType.RESERVATION_CANCELLED:
                    printRed(message.getPayload().toString());
                    break;
            }
    }

    /**
     * Sends a reservation request for a specific building and room to the RentalAgent.
     *
     * @param buildingName The name of the building where the reservation is requested.
     * @param roomName     The name of the room to be reserved.
     * @throws Exception If there is an error in sending the reservation request message.
     */
    public void makeReservation(String buildingName, String roomName) throws Exception {
        sender.sendDirectMessage(RoutingConfig.RENTAL_AGENT_QUEUE.getValue(),
                RoutingConfig.RENTAL_AGENT_KEY.getValue(),
                new Message(MessageType.MAKE_RESERVATION, username + "," + buildingName + "," + roomName));
    }

    /**
     * Prints the details of available buildings as received in the message payload.
     *
     * @param message The message containing the payload with the available buildings' details.
     */
    private void printAvailableBuildings(Message message){
        List<String> buildingDetails = (List<String>) message.getPayload();
        System.out.println("Available buildings and rooms:");
        buildingDetails.forEach(System.out::println);
    }

    /**
     * Requests cancellation of an existing reservation identified by the reservation number.
     *
     * @param reservationNumber The unique number of the reservation to be cancelled.
     */
    public void cancelReservation(String reservationNumber){
        try {
            sender.sendDirectMessage(RoutingConfig.RENTAL_AGENT_QUEUE.getValue(),
                    RoutingConfig.RENTAL_AGENT_KEY.getValue(),
                    new Message(MessageType.CANCEL_RESERVATION, reservationNumber));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints the reservations of the user in a structured format.
     */
    public Boolean viewMyReservations() {
        List<Reservation> myReservations = ReservationManager.getReservationsByUser(this.username);
        if (myReservations.isEmpty()) {
            printRed("There is no reservation that is issued with your username.");
            return false;
        } else {
            // Convert reservations to a structured string list and print each
            myReservations.stream()
                    .map(this::convertReservationToString)
                    .forEach(System.out::println);
            return true;
        }
    }

    /**
     * Requests the list of available buildings from the RentalAgent.
     */
    public void requestListOfBuildings() throws Exception {
        Message requestMessage = new Message(MessageType.REQUEST_BUILDINGS, null);
        sender.sendDirectMessage(RoutingConfig.RENTAL_AGENT_QUEUE.getValue(),
                RoutingConfig.RENTAL_AGENT_KEY.getValue(),
                requestMessage);
    }

    /**
     * Prints the message in green color.
     *
     * @param message The message to be printed in green.
     */
    public void printGreen(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    /**
     * Prints the message in red color.
     *
     * @param message The message to be printed in red.
     */
    public void printRed(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }

    /**
     * Converts a reservation object to a structured string representation.
     *
     * @param reservation The reservation to convert.
     * @return A string representation of the reservation.
     */
    private String convertReservationToString(Reservation reservation) {
        return "*Reservation Number: " + reservation.getReservationNumber() +
                "\n- Building: " + reservation.getBuildingName() +
                "\n- Room: " + reservation.getRoomName() +
                "\n- Status: " + reservation.getStatus();
    }

}
