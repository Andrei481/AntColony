package events;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConnection {
    public static Connection getConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost:15672"); // Replace with the actual RabbitMQ server host
        factory.setUsername("guest");
        factory.setPassword("guest");

        return factory.newConnection();
    }
}