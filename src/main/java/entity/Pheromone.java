package entity;

import helpers.PheromoneType;
import static helpers.PheromoneColors.*;

import java.awt.Color;
import java.awt.Graphics2D;

public class Pheromone {
    private final int x;
    private final int y;
    private Color color;    /* red for food and blue for home */
    private int level;      /* starts at 3 (strongest) and gradually decrements */
    private final PheromoneType type;   /* there are 2 types:
                                         * looking for FOOD (O)
                                         * going HOME (1)       */
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
