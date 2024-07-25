package buildings;

import props.*;
import rabbitMQ.*;
import java.util.*;


public class Building {
    private final String buildingName;
    private final int buildingID;
    private final List<ConferenceRoom> rooms;
    private final Sender sender;

    public Building(int nmOfRooms) {
        this.buildingID = BuildingManager.getLastIndexOfBuildingsList() + 1;
        this.buildingName = "Building-" + buildingID;
        this.rooms = new ArrayList<>();
        for (int i = 1; i <= nmOfRooms; i++) {
            rooms.add(new ConferenceRoom("Room-" + i));
        }
        sender = new Sender();
    }
    /**
     * Begins listening for responses
     */
    public void startListening() {
        try{
            String queueName = RoutingConfig.BUILDING_QUEUE.getValue() + buildingID;
            String keyName = RoutingConfig.BUILDING_KEY.getValue() + buildingID;
            Receiver.receiveDirectMessage(queueName, keyName, this::handleReceivedMessage);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Handles different types of received messages.
     *
     * @param message Message received from the RentalAgent.
     */
    private void handleReceivedMessage(Message message) {
        switch (message.getType()){
            case MessageType.MAKE_RESERVATION:
                handleReservationRequest(message);
                break;
            case MessageType.CANCEL_RESERVATION:
                handleCancellationRequest(message);
                break;
        }
    }

    private void handleReservationRequest(Message message) {
        String roomName = (String) message.getPayload();
        Optional<ConferenceRoom> roomOptional = findRoom(roomName);
        if (roomOptional.isPresent()) {
            handleRoomFound(roomOptional.get());
        } else {
            Message roomNotFoundMessage = new Message(MessageType.ROOM_NOT_FOUND,"This room is not exist!");
            sendMessage(roomNotFoundMessage);
        }
    }

    private Optional<ConferenceRoom> findRoom(String roomName) {
        return rooms.stream()
                .filter(room -> room.getRoomName().equalsIgnoreCase(roomName))
                .findFirst();
    }

    private void handleRoomFound(ConferenceRoom room) {
        if (!room.isBooked()) {
            bookRoom(room);
        } else {
            Message message = new Message(MessageType.ALREADY_BOOKED,"This room is already booked.");
            sendMessage(message);
        }
    }

    private void bookRoom(ConferenceRoom room) {
        room.bookRoom();
        Message responseMessage = new Message(MessageType.CONFIRM_RESERVATION, "The reservation is confirmed for " + this.buildingName + " " + room.getRoomName());
        sendMessage(responseMessage);
    }

    private void handleCancellationRequest(Message message) {
        String roomName = (String) message.getPayload();
        Optional<ConferenceRoom> roomOptional = findRoom(roomName);
        if (roomOptional.isPresent()) {
            unbookRoom(roomOptional.get());
        } else {
            Message roomNotFoundMessage = new Message(MessageType.ROOM_NOT_FOUND, "This room does not exist!");
            sendMessage(roomNotFoundMessage);
        }
    }

    private void unbookRoom(ConferenceRoom room) {
        if (room.isBooked()) {
            room.unbookRoom();
            Message responseMessage = new Message(MessageType.RESERVATION_CANCELLED, "Reservation cancelled for " + room.getRoomName());
            sendMessage(responseMessage);
        } else {
            Message message = new Message(MessageType.ROOM_NOT_BOOKED, "This room is not booked.");
            sendMessage(message);
        }
    }

    private void sendMessage(Message message) {
        try {
            sender.sendDirectMessage(RoutingConfig.RENTAL_AGENT_QUEUE.getValue(),
                    RoutingConfig.RENTAL_AGENT_KEY.getValue(),
                    message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBuildingName() {
        return buildingName;
    }

    public List<ConferenceRoom> getRooms() {
        return rooms;
    }

    @Override
    public String toString() {
        return buildingName + " Rooms: " + rooms;
    }

    public int getBuildingID() {
        return buildingID;
    }

}
