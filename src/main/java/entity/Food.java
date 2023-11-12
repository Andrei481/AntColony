package entity;

import utils.Logger;

import java.awt.*;

public class Food {
    private int quantity;
    private int id;
    private int posX, posY;
    private static int idCounter = 0;
    public Food(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        this.quantity = 5;
        this.id = ++idCounter;
        Logger.logSimulation("Food created ID: " + this.id);
    }

    public int[] getFoodCoords() {
        return new int[]{this.posX, this.posY};
    }

    public int getId() {
        return id;
    }
    public int getQuantity() {
        return this.quantity;
    }

    public void decreaseQuantity() {
        this.quantity--;
    }
}
