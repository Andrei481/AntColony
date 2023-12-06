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
        int visionRadius = ant.visionRadius;
        int antCol = ant.worldX / tileSize;
        int antRow = ant.worldY / tileSize;
        int[] foundFoodLocation;

        if(!ant.gotFood) {
            for (int i = antCol - visionRadius; i <= antCol + visionRadius; i++) {
                for (int j = antRow - visionRadius; j <= antRow + visionRadius; j++) {
                    // Check if the indices are within bounds
                    if (i >= 0 && i < tile_manager.mapTileNum.length && j >= 0 && j < tile_manager.mapTileNum[0].length) {
                        int tileNum = tile_manager.mapTileNum[i][j];

                        // Check if the tile contains a food entity
                        if (tile_manager.tile[tileNum].isFood) {
                            // Food entity found within vision radius
                            // You can add your logic here, such as updating the ant's state or taking some action
                            ant.detectedFoodCoords = new int[]{i, j};
                            Logger.logInfo("Food found within vision radius for Ant " + ant.getId() + " at coordinates: " + Arrays.toString(ant.detectedFoodCoords));
                        }
                    }
                }
            }
        }
        if(ant.gotFood) {
            for (int i = antCol - visionRadius; i <= antCol + visionRadius; i++) {
                for (int j = antRow - visionRadius; j <= antRow + visionRadius; j++) {
                    if (i >= 0 && i < tile_manager.mapTileNum.length && j >= 0 && j < tile_manager.mapTileNum[0].length) {
                        int tileNum = tile_manager.mapTileNum[i][j];
                        if (tile_manager.tile[tileNum].isHome) {
                            ant.setNestDetected();
                            Logger.logInfo("Nest found within vision radius for Ant " + ant.getId() + " at coordinates: " + Arrays.toString(ant.detectedFoodCoords));
                        }
                    }
                }
            }
        }

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
            if (ant.gotFood) {
                //reproduceSemaphore.acquire();
                if (!ant.sentReadySignal) {
                    Set<Ant> antsReady = nest.getAntsReady();
                    if (antsReady.isEmpty()) {
                        nest.addAntReady(ant);
                        ant.sentReadySignal = true;
                    } else {
                        Ant partnerAnt = antsReady.iterator().next();
                        nest.removeAntReady(partnerAnt);
                        ant.gotFood = false;
                        ant.reproduce();
                        antIdCount++;
                        Ant babyAnt = new Ant(antIdCount);
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

            if (!ant.gotFood) {
                foodSemaphore.acquire();
                for (Food foodItem : foods) {
                    int[] foodItemsLocation = foodItem.getFoodLocation();
                    if (Arrays.equals(foodItemsLocation, foundFoodLocation)) {
                        Logger.logInfo("Got food at location: " + Arrays.toString(foodItemsLocation));
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
                ant.gotFood = true;
//                Logger.logSimulation("Ant " + ant.getID() + " has gotten food");
                foodSemaphore.release();

            }

        }
    }

}
