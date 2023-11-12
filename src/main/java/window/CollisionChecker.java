package window;

import entity.Ant;
import entity.Food;
import utils.Logger;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class CollisionChecker {
    Panel ap;
    private Semaphore foodSemaphore, reproduceSemaphore;


    public CollisionChecker(Panel ap, Semaphore foodSemaphore, Semaphore reproduceSemaphore){
        this.ap=ap;
        this.foodSemaphore = foodSemaphore;
        this.reproduceSemaphore = reproduceSemaphore;
    }

    public void checkTile(Ant ant) throws InterruptedException {
        int entityLeftWorldX = ant.worldX + ant.solidArea.x;
        int entityRightWorldX = ant.worldX + ant.solidArea.x + ant.solidArea.width;
        int entityTopWorldY = ant.worldY + ant.solidArea.y;
        int entityBotWorldY = ant.worldY + ant.solidArea.y + ant.solidArea.height;

        int entityLeftCol = entityLeftWorldX / ap.tileSize;
        int entityRightCol = entityRightWorldX / ap.tileSize;
        int entityTopRow = entityTopWorldY / ap.tileSize;
        int entityBotRow = entityBotWorldY / ap.tileSize;

        int tileNum1 = 0, tileNum2 = 0;
        boolean collision = false;
        boolean food=false;
        boolean home=false;
        int[] foundFoodCoords = new int[0];
        if (entityLeftCol >= 0 && entityRightCol >= 0 &&

                entityLeftCol < ap.tile_manager.mapTileNum.length &&
                entityRightCol < ap.tile_manager.mapTileNum.length &&
                entityTopRow >= 0 && entityBotRow >= 0 &&
                entityTopRow < ap.tile_manager.mapTileNum[0].length) {

            switch (ant.direction) {
                case "up":
                    entityTopRow = (entityTopWorldY - ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];
                    break;
                case "down":
                    entityBotRow = (entityBotWorldY + ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];
                    break;
                case "left":
                    entityLeftCol = (entityLeftWorldX - ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];

                    break;
                case "right":
                    entityRightCol = (entityRightWorldX + ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];
                    break;
            }
            
            collision = ap.tile_manager.tile[tileNum1].collision || ap.tile_manager.tile[tileNum2].collision;
            food = ap.tile_manager.tile[tileNum1].isFood || ap.tile_manager.tile[tileNum2].isFood;
            home=ap.tile_manager.tile[tileNum1].isHome || ap.tile_manager.tile[tileNum2].isHome;

        }

        ant.collisionOn = collision;
        ant.isHome=home;
        int tolerance = 1;

        if(ant.isHome) {
//            Logger.logSimulation("Ant " + ant.getID() + " is Home");
            if (ant.foundFood) {
                reproduceSemaphore.acquire();
//                Logger.logSimulation("Ant " + ant.getID() + " has reproduced");
                reproduceSemaphore.release();
            }
            //ant.foundFood = false;
        }

        else if(food) {
            int x = 0,y = 0;
            switch (ant.direction) {
                case "up":
                    if(ap.tile_manager.tile[tileNum1].isFood) {
                        x = entityLeftCol;
                        y = entityTopRow;
                    }
                    else {
                        x = entityRightCol;
                        y = entityTopRow;
                    }
                    break;
                case "down":
                    if(ap.tile_manager.tile[tileNum1].isFood) {
                        x = entityLeftCol;
                        y = entityBotRow;
                    }
                    else {
                        x = entityRightCol;
                        y = entityBotRow;
                    }
                    break;
                case "left":
                    if(ap.tile_manager.tile[tileNum1].isFood) {
                        x = entityLeftCol;
                        y = entityTopRow;
                    }
                    else {
                        x = entityLeftCol;
                        y = entityBotRow;
                    }
                    break;
                case "right":
                    if(ap.tile_manager.tile[tileNum1].isFood) {
                        x = entityRightCol;
                        y = entityTopRow;
                    }
                    else {
                        x = entityRightCol;
                        y = entityBotRow;
                    }
                    break;
            }
            foundFoodCoords = new int[]{x, y};

            if(!ant.foundFood) {
                foodSemaphore.acquire();
                for(Food foodItem: ap.foods) {
                    int[] foodItemsCoords = foodItem.getFoodCoords();
//                    Logger.logInfo("foodCoords = " + Arrays.toString(foodItemsCoords) + " | found food coords = " + Arrays.toString(foodCoords));
                    if (Arrays.equals(foodItemsCoords, foundFoodCoords))  {
                        foodItem.decreaseQuantity();
                        Logger.logSimulation("Ant " + ant.getID() + " has gotten food " + foodItem.getId() + ". Food " + foodItem.getId() + " left: " + foodItem.getQuantity());
                        if (foodItem.getQuantity() == 0) {
                            ap.foods.remove(foodItem);
                            ap.tile_manager.mapTileNum[x][y] = 0;
                            Logger.logSimulation("Food " + foodItem.getId() + " removed from the map.");
                        }
                        break;
                    }
                }
                ant.foundFood=true;
//                Logger.logSimulation("Ant " + ant.getID() + " has gotten food");
            foodSemaphore.release();
            }
        }
    }

}
