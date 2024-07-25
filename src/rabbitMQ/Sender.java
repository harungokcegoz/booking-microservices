package rabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import props.Message;
import utils.JSONHandler;

public class Sender {

    /**
     * Sends a message to a direct exchange on RabbitMQ.
     *
     * @param queueName  The queue name to send the message to.
     * @param routingKey The routing key for the message.
     * @param message    The message to be sent.
     * @throws Exception Throws if the message sending fails.
     */
    public void sendDirectMessage(String queueName, String routingKey, Message message) throws Exception {
        try {
            Channel channel = RabbitMQConnector.getChannel();
            setupDirectExchange(queueName, routingKey, channel);
            publishMessage(RabbitMQExchanges.DIRECT_RENTAL.getExchangeName(), routingKey, message, channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up a direct exchange with a queue and a routing key.
     *
     * @param queueName  Name of the queue.
     * @param routingKey Routing key for the queue.
     * @param channel    The channel used to set up the exchange.
     * @throws Exception Throws if setup fails.
     */
    private void setupDirectExchange(String queueName, String routingKey, Channel channel) throws Exception {
        channel.exchangeDeclare(RabbitMQExchanges.DIRECT_RENTAL.getExchangeName(), "direct");
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, RabbitMQExchanges.DIRECT_RENTAL.getExchangeName(), routingKey);
    }

    /**
     * Publishes a message to a specified exchange using a routing key.
     *
     * @param exchange   The exchange to publish to.
     * @param routingKey The routing key for the message.
     * @param message    The message to publish.
     * @param channel    The channel used for publishing.
     * @throws Exception Throws if publishing fails.
     */
    private void publishMessage(String exchange, String routingKey, Message message, Channel channel) throws Exception {
        String serializedMessage = JSONHandler.serialize(message);
        channel.basicPublish(exchange, routingKey, null, serializedMessage.getBytes());
    }


    /**
     * Sends a message to a fanout exchange on RabbitMQ.
     *
     * @param exchange The exchange to send the message to.
     * @param message  The message to be sent.
     * @throws Exception Throws if the message sending fails.
     */
    public void sendFanoutMessage(RabbitMQExchanges exchange, Message message) throws Exception {
        try {
            Channel channel = RabbitMQConnector.getChannel();
            channel.exchangeDeclare(exchange.getExchangeName(), "fanout");

            String serializedMessage = JSONHandler.serialize(message);

            channel.basicPublish(exchange.getExchangeName(), "", null, serializedMessage.getBytes());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
