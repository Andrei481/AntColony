package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Pheromone {
    private int x, y;      // Position of the pheromone trail
    private Color color;   // Color of the pheromone trail
    private int level = 3;
    private PheromoneType type = PheromoneType.DEFAULT;// Strength of the pheromone trail

    private BufferedImage image;  // Image representing the pheromone trail

    public Pheromone(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = Color.BLUE;  // You can set the color to green or any color you prefer

        // Initialize the image for rendering the pheromone trail
        this.image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(this.color);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
    }

    public void evaporate() {
        // Implement pheromone evaporation logic here
        // Reduce the strength of the pheromone over time
        // You can decrease the strength by a certain amount in each time step
    }

    public void draw(Graphics2D g2) {
        // Set the color for drawing the pheromone circle
        g2.setColor(color);

        // Calculate the position and size of the circle
        int circleSize = 10;  // Adjust the size as needed
        int circleX = x - circleSize / 2;
        int circleY = y - circleSize / 2;

        // Draw the filled circle
        g2.fillOval(circleX, circleY, circleSize, circleSize);
    }

    // Getter and setter methods for strength, position, and other properties
}
