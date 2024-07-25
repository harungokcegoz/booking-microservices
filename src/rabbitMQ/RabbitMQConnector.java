package rabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnector {
    private static Connection connection;
    private static Channel channel;
    private static final String HOST = "localhost";
    private static final int PORT = 5672;

    /**
     * Handle the connection of RabbitMQ. Singleton is used to create only one connection in whole process.
     */
    public static synchronized Connection getConnection() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setPort(PORT);
            connection = factory.newConnection();
        }
        return connection;
    }
    /**
     * Get the default channel of the exist connection
     */
    public static synchronized Channel getChannel() throws IOException, TimeoutException {
        if (channel == null || !channel.isOpen()) {
            if (connection == null || !connection.isOpen()) {
                getConnection();
            }
            channel = connection.createChannel();
        }
        return channel;
    }

}
