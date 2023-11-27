package entities;

import definitions.PheromoneType;

import java.awt.*;

import static definitions.PheromoneColors.pheromoneColor;

public class Pheromone {
    private final int x;
    private final int y;
    private final PheromoneType type;
    private Color color;    /* red for food and blue for home */
    private int level;      /* starts at 3 (strongest) and gradually decrements */

    public Pheromone(int x, int y, PheromoneType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.level = 3;
        this.color = pheromoneColor[type.ordinal()][level];
    }

    public void evaporate() {
        if (level > 0) {
            level--;
            color = pheromoneColor[type.ordinal()][level];
        }
    }

    public void draw(Graphics2D g2) {
        if (level > 0) {
            g2.setColor(color);

            int circleSize = 10;
            int circleX = x + circleSize / 2 - 1;
            int circleY = y + circleSize / 2 - 1;

            g2.fillOval(circleX, circleY, circleSize, circleSize);
        }
    }

    public int getLevel() {
        return this.level;
    }

}
