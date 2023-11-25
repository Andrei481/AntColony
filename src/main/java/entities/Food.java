package entities;

import utils.Logger;
import screens.SimulationScreen;

import static definitions.SimulationEventType.FOOD_CREATED;

public class Food {
    private int quantity;
    private final int id;
    private final int posX;
    private final int posY;
    private static int idCounter = 0;
    public Food(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        this.quantity = 5;
        this.id = ++idCounter;
        Logger.logSimulation(FOOD_CREATED, this);
    }

    public int[] getFoodLocation() {
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
        if (quantity == 0) SimulationScreen.updateBufferedMap();
    }
}
