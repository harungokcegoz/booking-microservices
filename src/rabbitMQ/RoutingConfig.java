package rabbitMQ;

public enum RoutingConfig {
    RENTAL_AGENT_QUEUE("RENTAL_AGENT_QUEUE"),
    RENTAL_AGENT_KEY("RENTAL_AGENT_KEY"),
    CLIENT_QUEUE("CLIENT_QUEUE"),
    CLIENT_NOTIFICATION_QUEUE("CLIENT_NOTIFICATION_QUEUE"),
    BUILDING_MANAGER_QUEUE("BUILDING_MANAGER_QUEUE"),
    CLIENT_KEY("CLIENT_KEY"),
    BUILDING_QUEUE("BUILDING_QUEUE"),
    BUILDING_KEY("BUILDING_KEY");

    private final String value;

    RoutingConfig(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}



