package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.min;

public class Pheromone {
    private int x, y;
    private Color color;
    private int level;
    private PheromoneType type;
    private BufferedImage image;

    private Timer evaporateTimer;
    private int timeCounter;

    public Pheromone(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = Color.BLUE;
        this.level = 3;
        this.type = PheromoneType.DEFAULT;
        this.image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(this.color);
        g.fillRect(0, 0, 1, 1);
        g.dispose();
        timeCounter=0;
        // Schedule the evaporation timer
        //evaporateTimer = new Timer();
        //evaporateTimer.schedule(new EvaporateTask(), 10000, 10000);
    }

    public void evaporate() {
        if(timeCounter==500) {
            if (level > 0) {
                level--;
                color = makeDimmerColor(color);

            }
            timeCounter=0;
        }
    }

    public void draw(Graphics2D g2) {
        if(level>0) {
            g2.setColor(color);

            int circleSize = 10;  // Adjust the size as needed
            int circleX = x - circleSize / 2;
            int circleY = y - circleSize / 2;

            g2.fillOval(circleX, circleY, circleSize, circleSize);
        }
    }

    private Color makeDimmerColor(Color originalColor) {
        int r = originalColor.getRed();
        int g = originalColor.getGreen();
        int b = originalColor.getBlue();

        r = min(255, r + 75);
        g = min(255, g + 75);
        b = min(255, b + 75);

        return new Color(r, g, b);
    }

    public int getLevel() {
        return this.level;
    }

    private class EvaporateTask extends TimerTask {
        @Override
        public void run() {
            evaporate();
        }
    }

    public void update(){
        timeCounter++;
        evaporate();
    }

}
