package events;
import com.rabbitmq.client.*;

public class StatisticsService {

    private static final String EXCHANGE_NAME = "cell_events";

    public static void main(String[] args) {
        try (Connection connection = RabbitMQConnection.getConnection();
             Channel channel = connection.createChannel()) {

            // Declare the exchange
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

            // Create a queue for receiving events
            String queueName = channel.queueDeclare().getQueue();

            // Bind the queue to the exchange with routing key patterns
            channel.queueBind(queueName, EXCHANGE_NAME, "cell.*");

            System.out.println(" [*] Waiting for cell events. To exit press Ctrl+C");

            // Set up a consumer to process incoming messages
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                processCellEvent(message);
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processCellEvent(String message) {
        // Implement the logic to calculate statistics based on the received cell event
        System.out.println(" [x] Received '" + message + "'");
        // Add your statistics calculation logic here
    }
}
