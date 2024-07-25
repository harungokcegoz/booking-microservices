package rabbitMQ;

import com.rabbitmq.client.*;
import props.Message;
import utils.JSONHandler;
import java.util.function.Consumer;


public class Receiver {
    /**
     * Receives messages from a direct exchange in RabbitMQ.
     *
     * @param queueName  The name of the queue from which messages are received.
     * @param routingKey The routing key to bind the queue to the exchange.
     * @param callback   A Consumer functional interface for processing received messages.
     * @throws Exception Throws if any RabbitMQ operation fails.
     */
    public static void receiveDirectMessage(String queueName, String routingKey, Consumer<Message> callback) throws Exception {
        Channel channel = setupDirectChannel(queueName, routingKey);
        configureMessageConsumer(channel, queueName, callback);
    }

    /**
     * Sets up and returns a Channel for a direct exchange with the specified queue and routing key.
     *
     * @param queueName  Name of the queue.
     * @param routingKey Routing key for binding.
     * @return Configured Channel.
     * @throws Exception Throws if channel setup fails.
     */
    private static Channel setupDirectChannel(String queueName, String routingKey) throws Exception {
        Channel channel = RabbitMQConnector.getConnection().createChannel();
        channel.exchangeDeclare(RabbitMQExchanges.DIRECT_RENTAL.getExchangeName(), "direct");
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, RabbitMQExchanges.DIRECT_RENTAL.getExchangeName(), routingKey);
        return channel;
    }

    public static void receiveFanoutMessage(String queueName, Consumer<Message> callback) throws Exception {
        Channel channel = RabbitMQConnector.getConnection().createChannel();
        channel.exchangeDeclare(RabbitMQExchanges.FANOUT_BUILDINGS.getExchangeName(), "fanout");
        channel.queueDeclare(queueName, false,false,false,null);
        channel.queueBind(queueName, RabbitMQExchanges.FANOUT_BUILDINGS.getExchangeName(), "");

        configureMessageConsumer(channel, queueName, callback);
    }

    /**
     * Configures a consumer to receive messages, process them using the provided callback, and acknowledges the messages.
     *
     * @param channel  The channel to receive messages from.
     * @param queueName The queue to consume messages from.
     * @param callback The action to perform on received messages.
     * @throws Exception Throws if consuming messages fails.
     */
    private static void configureMessageConsumer(Channel channel, String queueName, Consumer<Message> callback) throws Exception {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String messageStr = new String(delivery.getBody(), "UTF-8");
                Message message = JSONHandler.deserialize(messageStr);
                callback.accept(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    }

}
