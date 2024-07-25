package buildings;

import props.Message;
import props.MessageType;
import rabbitMQ.RabbitMQExchanges;
import rabbitMQ.Receiver;
import rabbitMQ.RoutingConfig;
import rabbitMQ.Sender;

import java.util.ArrayList;
import java.util.List;

public class BuildingManager {

    private static List<Building> buildings = new ArrayList<>();

    public static List<Building> getBuildings() {
        return buildings;
    }

    private static final Receiver receiver = new Receiver();


    public BuildingManager() {
        startListeningToFanout();
    }

    /**
     * Begins listening for fanout messages in a given exchange.
     */
    public void startListeningToFanout() {
        try {
            receiver.receiveFanoutMessage(RoutingConfig.BUILDING_MANAGER_QUEUE.getValue(), this::handleFanoutMessage);
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
            case MessageType.CREATE_BUILDING:
                registerBuilding(message.getPayload().toString());
        }

    }

    /**
     * Creates a new building object based on the received number of rooms, adds it to the management list,
     * and starts a separate listening service for it. If the number of rooms is invalid or a building already exists,
     * it handles the error without disrupting the service.
     *
     * @param numberOfRooms A string representing the number of rooms to be set for the new building.
     */
    private void registerBuilding(String numberOfRooms) {
        try {
            int rooms = Integer.parseInt(numberOfRooms);
            Building building = new Building(rooms);

            buildings.add(building);
            new Thread(() -> {
                try {
                    building.startListening();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            notifyNewBuilding(building);

        } catch (NumberFormatException e) {
            System.err.println("Error parsing number of rooms: " + numberOfRooms);
            e.printStackTrace();
        }
    }

    /**
     * Notifies listeners about a new building by publishing to a fanout exchange.
     *
     * @param building The Building object that has been registered.
     */
    private static void notifyNewBuilding(Building building) {
        try {
            Sender sender = new Sender();
            Message message = new Message(MessageType.BUILDING_CREATED, building.getBuildingName() + " is created!");
            sender.sendFanoutMessage(RabbitMQExchanges.FANOUT_BUILDINGS, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the index that would be assigned to the next building added to the list.
     * This is equivalent to the size of the current list of buildings.
     *
     * @return The index for the next building or 0 if the list is empty.
     */
    public static int getLastIndexOfBuildingsList(){
        if (buildings.isEmpty()) {
            return 0;
        } else {
            return buildings.size();
        }
    }

    /**
     * Retrieves a building by its name.
     *
     * @param buildingName The name of the building to find.
     * @return The {@code Building} object with the matching name, or null if not found.
     */
    public static Building getBuildingByName(String buildingName) {
        return buildings.stream()
                .filter(b -> b.getBuildingName().equalsIgnoreCase(buildingName))
                .findFirst()
                .orElse(null);
    }

}
