package simulation;

import entities.Ant;
import entities.Food;
import utils.Logger;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static definitions.SimulationEventType.*;
import static screens.SimulationScreen.*;

public class CollisionChecker {
    private final Semaphore foodSemaphore;
    private final Semaphore reproduceSemaphore;


    public CollisionChecker(Semaphore foodSemaphore, Semaphore reproduceSemaphore) {
        this.foodSemaphore = foodSemaphore;
        this.reproduceSemaphore = reproduceSemaphore;
    }

    public void checkTile(Ant ant) throws InterruptedException {
        int entityLeftWorldX = ant.worldX + ant.solidArea.x;
        int entityRightWorldX = ant.worldX + ant.solidArea.x + ant.solidArea.width;
        int entityTopWorldY = ant.worldY + ant.solidArea.y;
        int entityBotWorldY = ant.worldY + ant.solidArea.y + ant.solidArea.height;

        int entityLeftCol = entityLeftWorldX / tileSize;
        int entityRightCol = entityRightWorldX / tileSize;
        int entityTopRow = entityTopWorldY / tileSize;
        int entityBotRow = entityBotWorldY / tileSize;

        int tileNum1 = 0, tileNum2 = 0;
        boolean collision = false;
        boolean food = false;
        boolean home = false;
        int[] foundFoodLocation;
        if (entityLeftCol >= 0 && entityRightCol >= 0 &&

                entityLeftCol < tile_manager.mapTileNum.length &&
                entityRightCol < tile_manager.mapTileNum.length &&
                entityTopRow >= 0 && entityBotRow >= 0 &&
                entityTopRow < tile_manager.mapTileNum[0].length) {

            switch (ant.direction) {
                case UP:
                    entityTopRow = (entityTopWorldY - ant.speed) / tileSize;
                    tileNum1 = tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = tile_manager.mapTileNum[entityRightCol][entityTopRow];
                    break;
                case DOWN:
                    entityBotRow = (entityBotWorldY + ant.speed) / tileSize;
                    tileNum1 = tile_manager.mapTileNum[entityLeftCol][entityBotRow];
                    tileNum2 = tile_manager.mapTileNum[entityRightCol][entityBotRow];
                    break;
                case LEFT:
                    entityLeftCol = (entityLeftWorldX - ant.speed) / tileSize;
                    tileNum1 = tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = tile_manager.mapTileNum[entityLeftCol][entityBotRow];

                    break;
                case RIGHT:
                    entityRightCol = (entityRightWorldX + ant.speed) / tileSize;
                    tileNum1 = tile_manager.mapTileNum[entityRightCol][entityTopRow];
                    tileNum2 = tile_manager.mapTileNum[entityRightCol][entityBotRow];
                    break;
            }

            collision = tile_manager.tile[tileNum1].collision || tile_manager.tile[tileNum2].collision;
            food = tile_manager.tile[tileNum1].isFood || tile_manager.tile[tileNum2].isFood;
            home = tile_manager.tile[tileNum1].isHome || tile_manager.tile[tileNum2].isHome;

        }

        ant.collisionOn = collision;
        ant.isHome = home;
        int tolerance = 1;

        if (ant.isHome) {
            //Logger.logSimulation("Ant " + ant.getID() + " is Home");
            if (ant.foundFood) {
                //reproduceSemaphore.acquire();
                if (!ant.sentReadySignal) {
                    Set<Ant> antsReady = nest.getAntsReady();
                    if (antsReady.isEmpty()) {
                        nest.addAntReady(ant);
                        ant.sentReadySignal = true;
                    } else {
                        Ant partnerAnt = antsReady.iterator().next();
                        nest.removeAntReady(partnerAnt);
                        ant.foundFood = false;
                        ant.reproduce();
                        id++;
                        Ant babyAnt = new Ant(id);
                        Thread babyAntThread = new Thread(babyAnt);
                        babyAntThread.start();
                        antThreadMap.put(babyAnt, babyAntThread);
                        Logger.logSimulation(BIRTH, babyAnt);


                    }
                    //reproduceSemaphore.release();
                }
            }
        } else if (food) {
            int x = 0, y = 0;
            switch (ant.direction) {
                case UP:
                    if (tile_manager.tile[tileNum1].isFood) {
                        x = entityLeftCol;
                    } else {
                        x = entityRightCol;
                    }
                    y = entityTopRow;
                    break;
                case DOWN:
                    if (tile_manager.tile[tileNum1].isFood) {
                        x = entityLeftCol;
                    } else {
                        x = entityRightCol;
                    }
                    y = entityBotRow;
                    break;
                case LEFT:
                    if (tile_manager.tile[tileNum1].isFood) {
                        x = entityLeftCol;
                        y = entityTopRow;
                    } else {
                        x = entityLeftCol;
                        y = entityBotRow;
                    }
                    break;
                case RIGHT:
                    if (tile_manager.tile[tileNum1].isFood) {
                        x = entityRightCol;
                        y = entityTopRow;
                    } else {
                        x = entityRightCol;
                        y = entityBotRow;
                    }
                    break;
            }
            foundFoodLocation = new int[]{x, y};

            if (!ant.foundFood) {
                foodSemaphore.acquire();
                for (Food foodItem : foods) {
                    int[] foodItemsLocation = foodItem.getFoodLocation();
                    if (Arrays.equals(foodItemsLocation, foundFoodLocation)) {
                        foodItem.decreaseQuantity();
                        Logger.logSimulation(MEAL, ant);
                        if (foodItem.getQuantity() == 0) {
                            foods.remove(foodItem);
                            tile_manager.mapTileNum[x][y] = 0;
                            Logger.logSimulation(FOOD_DEPLETED, foodItem);
                        }
                        break;
                    }
                }
                ant.foundFood = true;
//                Logger.logSimulation("Ant " + ant.getID() + " has gotten food");
                foodSemaphore.release();
            }
        }
    }

}
