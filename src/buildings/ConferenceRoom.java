package buildings;

public class ConferenceRoom {
    private String roomName;
    private boolean isBooked;

    public ConferenceRoom(String roomName) {
        this.roomName = roomName;
        this.isBooked = false;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void bookRoom() {
        this.isBooked = true;
    }

    public void unbookRoom() {
        this.isBooked = false;
    }

    @Override
    public String toString() {
        return "ConferenceRoom{" +
                "roomName='" + roomName + '\'' +
                ", isBooked=" + isBooked +
                '}';
    }
}
