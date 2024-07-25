package rabbitMQ;

public enum RabbitMQExchanges {
    FANOUT_BUILDINGS("buildings_fanout"),
    DIRECT_RENTAL("rental_direct");

    private final String exchangeName;

    RabbitMQExchanges(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return exchangeName;
    }
}
