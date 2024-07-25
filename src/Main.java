import buildings.Building;
import buildings.BuildingManager;
import clients.Client;
import rabbitMQ.RabbitMQConnector;
import rentalAgents.RentalAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

    public class Main {

        private static final ExecutorService executorService = Executors.newCachedThreadPool();

        public static void main(String[] args) {
            try {
                startApplication();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void startApplication() throws InterruptedException {
            System.out.println("Hello! Welcome the booking.nl!");
            System.out.print("Please create a username:");
            Scanner scanner = new Scanner(System.in);
            String username = scanner.nextLine();

            Client client = new Client(username);
            RentalAgent rentalAgent = new RentalAgent();
            //Created to listen the fanout exchange
            BuildingManager manager = new BuildingManager();

            registerBuildings(rentalAgent);

            executorService.submit(() -> {
                try {
                    rentalAgent.startListening();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            executorService.submit(() -> {
                try {
                    client.startListening();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            // Start the user interface
            runDashboard(client, rentalAgent);
        }

        private static void runDashboard(Client client, RentalAgent agent) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    System.out.println("Dashboard is loading...");
                    Thread.sleep(1000);
                    showMenu();
                    int choice = scanner.nextInt();
                    handleUserChoice(choice, client, agent);
                } catch (Exception e) {
                    System.out.println("Invalid input or error occurred: " + e.getMessage());
                    scanner.nextLine();
                }
            }
        }

        private static void showMenu() throws InterruptedException {
            Thread.sleep(1000);
            System.out.println("----------------------- &&&&& -----------------------");
            System.out.println("1. List Buildings");
            System.out.println("2. Make a Reservation");
            System.out.println("3. Cancel the Reservation");
            System.out.println("4. View My Reservations");
            System.out.println("5. Quit");
            System.out.print("Choose an option: ");
        }

        private static void handleUserChoice(int choice, Client client, RentalAgent agent) throws Exception {
            switch (choice) {
                case 1:
                    requestBuildingList(client);
                    break;
                case 2:
                    requestBuildingList(client);
                    Thread.sleep(500);
                    makeReservation(client);
                    break;
                case 3:
                    cancelReservation(client);
                    break;
                case 4:
                    client.viewMyReservations();
                    break;
                case 5:
                    exitApplication();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        /**
         * Creates default buildings as pretending rental operates that
         */
        private static void registerBuildings(RentalAgent rentalAgent) {
            rentalAgent.registerBuilding(3);
            rentalAgent.registerBuilding(5);
        }

        private static void makeReservation(Client client) {
            try {
                Scanner scanner = new Scanner(System.in);

                System.out.print("Enter Building Name (e.g., building-1): ");
                String buildingName = scanner.nextLine();

                System.out.print("Enter Room Name (e.g., room-1): ");
                String roomName = scanner.nextLine();


                if (validateReservationDetails(buildingName, roomName)) {
                    client.makeReservation(buildingName, roomName);
                } else {
                    System.out.println("Invalid building or room name entered. Please follow the correct format.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private static boolean validateReservationDetails(String buildingName, String roomName) {
            boolean validBuilding = buildingName != null && buildingName.matches("building-\\d+");
            boolean validRoom = roomName != null && roomName.matches("room-\\d+");

            return validBuilding && validRoom;
        }

        private static void requestBuildingList(Client client) {
            executorService.submit(() -> {
                try {
                    client.requestListOfBuildings();
                    System.out.println("Building list is fetching. Loading...");
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        private static void cancelReservation(Client client) {
            try {
                Scanner scanner = new Scanner(System.in);
                if (client.viewMyReservations()){
                    System.out.print("Enter the reservation number that you want to cancel: ");
                    String reservationNumber = scanner.nextLine();
                    client.cancelReservation(reservationNumber);
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        private static void exitApplication() {
            System.out.println("Exiting...");
            executorService.shutdown();
            System.exit(0);
        }
    }

