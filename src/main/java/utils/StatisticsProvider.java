package utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import definitions.SimulationEventType;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class StatisticsProvider {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final boolean deleteMessagesOnExit = true;
    private static Channel channel;

    static {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            System.err.println("Error connecting to RabbitMQ: " + e.getMessage());
        }
    }

    public static void sendMessage(SimulationEventType event, String message) {
        executorService.execute(() -> {
            try {
                channel.queueDeclare(event.name(), false, deleteMessagesOnExit, false, null);
                channel.basicPublish("", event.name(), false, null, message.getBytes());
            } catch (IOException e) {
                System.err.println("Error sending message to RabbitMQ: " + e.getMessage());
            }
        });
    }
}
