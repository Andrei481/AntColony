package entity;

import utils.Logger;

import java.awt.*;

public class Food {
    private int quantity;
    private int id;
    private static int idCounter = 0;
    public Food() {
        this.quantity = 5;
        this.id = ++idCounter;
        Logger.logInfo("Food created ID: " + this.id);
    }

    public void decreaseQuantity() {
        this.quantity--;
    }
}
