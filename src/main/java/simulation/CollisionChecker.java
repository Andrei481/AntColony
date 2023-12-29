package simulation;

import definitions.PheromoneType;
import entities.Ant;
import entities.Food;
import utils.Logger;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static definitions.AntActionType.SEARCH_NEST;
import static definitions.SimulationEventType.FOOD_DEPLETED;
import static definitions.SimulationEventType.MEAL;
import static screens.SimulationScreen.tileSize;
import static screens.SimulationScreen.tile_manager;
import static simulation.SimulationMain.*;

/*
 * CollisionChecker contains the checkTile method.
 * This method is used by every ant.
 */

public class CollisionChecker {
    private final Semaphore foodSemaphore;


    public CollisionChecker(Semaphore foodSemaphore) {
        this.foodSemaphore = foodSemaphore;
    }

    private void lookAround(Ant ant) {

        int visionRadius = ant.visionRadius;
        int antCol = ant.worldX / tileSize;
        int antRow = ant.worldY / tileSize;

        for (int i = antCol - visionRadius; i <= antCol + visionRadius; i++) {
            for (int j = antRow - visionRadius; j <= antRow + visionRadius; j++) {
                if (i >= 0 && i < tile_manager.mapTileNum.length && j >= 0 && j < tile_manager.mapTileNum[0].length) {
                    int tileNum = tile_manager.mapTileNum[i][j];
                    if (!ant.gotFood) {
                        ant.setNestDetected(false);
                        ant.detectedFoodPheromoneTile = new int[]{-1, -1};
                        if (tile_manager.tile[tileNum].isFood) {
                            ant.detectedFoodTile = new int[]{i, j};
//                          Logger.logInfo("Food found within vision radius for Ant " + ant.getId() + " at coordinates: " + Arrays.toString(ant.detectedFoodCoords));
                        }
                        if (pheromoneGrid[i][j] != null && pheromoneGrid[i][j].getType() == PheromoneType.HOME && pheromoneGrid[i][j].getAntId() != ant.getId()) {
                            ant.detectedHomePheromoneTile = new int[]{i, j};
//                            Logger.logInfo("Home pheromone found within vision radius for Ant " + ant.getId() + " at coordinates: " + Arrays.toString(ant.detectedHomePheromones));
                        }
                    } else {
                        ant.detectedFoodTile = new int[]{-1, -1};
                        ant.detectedHomePheromoneTile = new int[]{-1, -1};
                        if (tile_manager.tile[tileNum].isHome) {
                            ant.setNestDetected(true);
//                            Logger.logInfo("Nest found within vision radius for Ant " + ant.getId() + " at coordinates: " + Arrays.toString(ant.detectedFoodCoords));
                        }
                        if (pheromoneGrid[i][j] != null && pheromoneGrid[i][j].getType() == PheromoneType.FOOD && pheromoneGrid[i][j].getAntId() != ant.getId()) {
                            ant.detectedFoodPheromoneTile = new int[]{i, j};
//                            Logger.logInfo("Food pheromone found within vision radius for Ant " + ant.getId() + " at coordinates: " + Arrays.toString(ant.detectedFoodPheromones));
                        }
                    }
                }
            }
        }
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

        lookAround(ant);

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
                if (!ant.sentReadySignal) { //first ant that arrives at the nest
                    Set<Ant> antsReady = nest.getAntsReady();
                    if (antsReady.isEmpty()) {
                        nest.addAntReady(ant);
                        ant.sentReadySignal = true;
                    } else {    //second ant
                        Ant partnerAnt = antsReady.iterator().next();
                        ant.reproduceWith(partnerAnt);
                    }
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
                        //Logger.logInfo("Got food at location: " + Arrays.toString(foodItemsLocation));
                        Logger.logSimulation(MEAL, ant);
                        foodItem.decreaseQuantity();
                        if (foodItem.getQuantity() == 0) {
                            foods.remove(foodItem);
                            tile_manager.mapTileNum[x][y] = 0;
                            Logger.logSimulation(FOOD_DEPLETED, foodItem);
                        }
                        break;
                    }
                }
                ant.gotFood = true;
                ant.currentAction = SEARCH_NEST;
                ant.loggedFollowFood = false;
//                Logger.logSimulation("Ant " + ant.getID() + " has gotten food");
                foodSemaphore.release();

            }

        }
    }

}
