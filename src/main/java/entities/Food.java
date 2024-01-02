package entities;

import utils.Logger;

import static definitions.SimulationEventType.*;
import static screens.SimulationScreen.tile_manager;
import static screens.SimulationScreen.updateBufferedMap;
import static simulation.SimulationMain.foods;

public class Food {
    private static int idCounter = 0;
    private final int id;
    private final int posX;
    private final int posY;
    private int quantity;

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
        Logger.logSimulation(FOOD_REDUCED, this);
        if (quantity == 0) {
            updateBufferedMap();
            foods.remove(this);
            tile_manager.mapTileNum[posX][posY] = 0;
            Logger.logSimulation(FOOD_DEPLETED, this);

        }


    }
}
