package entities;

import definitions.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import static definitions.AntActionType.SEARCH_FOOD;
import static definitions.AntMovementType.*;
import static definitions.Direction.*;
import static definitions.SimulationEventType.*;
import static entities.Nest.nestPosX;
import static entities.Nest.nestPosY;
import static screens.SimulationScreen.*;
import static simulation.SimulationMain.*;
import static utils.Logger.logSimulation;

public class Ant implements Runnable {

    /* Number of moves before starving */
    public static final int MAX_HUNGER = 200;
    private final int id;
    public boolean alive = true;
    public int worldX, worldY;
    public int speed;
    public AntActionType currentAction = SEARCH_FOOD;
    public Direction direction = DOWN;
    public Rectangle solidArea;
    public boolean collisionOn = false;
    public boolean gotFood = false;
    /* Variables used to send log message only once. */
    public boolean loggedFollowFood = false;
    public boolean loggedUseHomePheromone = false;
    public boolean loggedUseFoodPheromone = false;

    public boolean isHome = false;
    public boolean sentReadySignal = false;
    public int visionRadius = 7;
    public boolean detectedFood = false;
    public int[] detectedFoodTile = new int[]{-1, -1};
    public int[] detectedHomePheromoneTile = new int[]{-1, -1};
    public int[] detectedFoodPheromoneTile = new int[]{-1, -1};
    private AntMovementType movement = RANDOM;
    private int hunger = 0;
    private final int startPosX;
    private final int startPosY;
    private final int updateCooldownMillis;
    private int reproducedCounter;
    private boolean nestDetected = false;

    public Ant() {
        antIdCount++;
        id = antIdCount;
        Thread antThread = new Thread(this);
        antThreadMap.put(this, antThread);
        solidArea = new Rectangle(0, 0, tileSize, tileSize);
        startPosX = 13 * tileSize;
        startPosY = 13 * tileSize;
        nestPosX = 5 * tileSize;
        nestPosY = 5 * tileSize;
        speed = 10;
        worldX = startPosX;
        worldY = startPosY;
        reproducedCounter = 0;
        updateCooldownMillis = 100 + getRandomNumber(200);
        antThread.start();
        logSimulation(BIRTH, this);
    }



    /*
     * The Ant thread starts at run() and executes update() every X milliseconds.
     * The update() method takes care of 1 ant movement by calling performAction();
     * The draw() method is called in the SimulationScreen thread and places the ant on the screen.
     *
     * The map is made of tiles. We can refer to a tile by using (Col,Row). (0,0) is top left.
     * Each tile is a square of (tileSize * tileSize) pixels. We can refer to a pixel by using (X,Y). (0,0) is top left.
     * The position of an entity is its top left pixel.
     */

    @Override
    public void run() {
        while (alive) {
            try {
                update();
                if (!alive) return;
                Thread.sleep(updateCooldownMillis);

            } catch (InterruptedException e) {
                System.err.println("Ant run error: " + e.getMessage());
            }
        }
    }

    private void update() throws InterruptedException {
        try {
            int[] previousLocation = {worldX, worldY};

            performAction();
            if (!alive) return;
            depositPheromone(previousLocation);
            increaseHunger();

        } catch (Exception e) {
            System.err.println("Ant update error: " + e.getMessage());
        }
    }

    private void performAction() throws InterruptedException {
        switch (currentAction) {
            case SEARCH_FOOD:
                searchFood();
                break;
            case SEARCH_NEST:
                searchNest();
                break;
            case SEARCH_MATE:
                break;
        }
    }

    private void detectFood() {
    }

    private boolean seesFood() {
//        if (!detectedFood) detectFood();
        return (detectedFoodTile[0] >= 0 && detectedFoodTile[1] >= 0
                && tile_manager.mapTileNum[detectedFoodTile[0]][detectedFoodTile[1]] == 2);
    }

    private boolean seesHomePheromone() {
        return (detectedHomePheromoneTile[0] >= 0 && detectedHomePheromoneTile[1] >= 0                                        // if collided with a pheromone
                && !isPheromoneDepleted(detectedHomePheromoneTile[0], detectedHomePheromoneTile[1])                           // if pheromone not empty
                && detectedHomePheromoneTile[0] > worldX / tileSize && detectedHomePheromoneTile[1] > worldY / tileSize);     // if pheromone is above and to the right of the ant?
    }

    private void searchFood() throws InterruptedException {

        if (seesFood())
            movement = DIRECT;
        else if (seesHomePheromone())
            movement = PHEROMONE;
        else
            movement = RANDOM_RIGHT;

        switch (movement) {
            case DIRECT: {
                int[] foodPosition = {detectedFoodTile[0] * tileSize, detectedFoodTile[1] * tileSize};
                //System.out.println("ant found food:"+id+"\n food cords:"+foodX+" "+foodY);
                //collisionOn = true; // no clue what this line does
                if (!loggedFollowFood) {
                    logSimulation(FOLLOW_FOOD, this);
                    loggedFollowFood = true;            // log FOLLOW_FOOD only once
                    loggedUseHomePheromone = false;     // move somewhere where we reset all flags?
                }
                moveTowards(foodPosition);
//                detectedFoodCoords = new int[]{-1,-1};
//                col_checker.checkTile(this);
                break;
            }
            case PHEROMONE: {
                int[] pheromonePosition = {detectedHomePheromoneTile[0] * tileSize, detectedHomePheromoneTile[1] * tileSize};
                if (!loggedUseHomePheromone) {
                    logSimulation(USE_HOME_PH, this);
                    loggedUseHomePheromone = true;
                }
                moveTowards(pheromonePosition);
//                detectedHomePheromones = new int[]{-1,-1};
                break;
            }
            case RANDOM_RIGHT: {
                Random random = new Random();
                int random_dir = random.nextInt(125);
                if (random_dir < 25) {
                    direction = UP;
                } else if (random_dir < 50) {
                    direction = DOWN;
                } else if (random_dir < 75) {
                    direction = LEFT;
                } else {
                    direction = RIGHT;
                }
                // Check collision
                collisionOn = false;
                col_checker.checkTile(this);
                // if collision = false, can move
                if (!collisionOn) {
                    stepForward();
                }
                break;
            }
        }
    }

    private boolean seesFoodPheromone() {
        return (this.detectedFoodPheromoneTile[0] >= 0 &&
                this.detectedFoodPheromoneTile[1] >= 0 &&
                !isPheromoneDepleted(detectedFoodPheromoneTile[0], detectedFoodPheromoneTile[1]) &&
                detectedFoodPheromoneTile[0] < worldX / tileSize &&
                detectedFoodPheromoneTile[1] < worldY / tileSize);
    }

    private void searchNest() throws InterruptedException {

        if (nestDetected)
            movement = DIRECT;
        else if (seesFoodPheromone())
            movement = PHEROMONE;
        else
            movement = RANDOM_LEFT;

//        Logger.logInfo("Ant " + id + " position: " + worldX/tileSize + " " + worldY/tileSize);
        switch (movement) {
            case DIRECT: {
                int[] nestPosition = {nestPosX, nestPosY};
                collisionOn = false;
                moveTowards(nestPosition);
                loggedUseFoodPheromone = false;
                break;
            }
            case PHEROMONE: {
                int[] pheromonePosition = {detectedFoodPheromoneTile[0] * tileSize, detectedFoodPheromoneTile[1] * tileSize};
                if (!loggedUseFoodPheromone) {
                    logSimulation(USE_FOOD_PH, this);
                    loggedUseFoodPheromone = true;
                }

                moveTowards(pheromonePosition);
//                detectedFoodPheromones = new int[]{-1,-1};
                break;
            }
            case RANDOM_LEFT:
                Random random = new Random();
                int random_dir = random.nextInt(125);
                if (random_dir < 25) {
                    direction = UP;
                } else if (random_dir < 50) {
                    direction = DOWN;
                } else if (random_dir < 75) {
                    direction = RIGHT;
                } else {
                    direction = LEFT;
                }
                // Check collision
                collisionOn = false;
                col_checker.checkTile(this);
                // if collision = false, can move
                if (!collisionOn) {
                    switch (direction) {
                        case UP:
                            worldY -= speed;
                            break;
                        case DOWN:
                            worldY += speed;
                            break;
                        case LEFT:
                            worldX -= speed;
                            break;
                        case RIGHT:
                            worldX += speed;
                            break;
                    }
                }
                break;

            //System.out.println("Ant " + id +foundFood+isHome+ " has found food, is going "+direction);


        }
    }

    private void searchMate() {

    }

    public void moveRandomly() throws InterruptedException {
        int randomDirection = getRandomNumber(4);
        direction = switch (randomDirection) {
            case 1 -> UP;
            case 2 -> DOWN;
            case 3 -> LEFT;
            case 4 -> RIGHT;
            default -> throw new IllegalStateException("Unexpected value in moveRandomly(): " + randomDirection);
        };
    }

    public void moveRandomly(Direction bias) throws InterruptedException {
        int biasedDirection = getRandomNumber(5);
        direction = switch (biasedDirection) {
            case 1 -> UP;
            case 2 -> DOWN;
            case 3 -> LEFT;
            case 4 -> RIGHT;
            case 5 -> bias;
            default -> throw new IllegalStateException("Unexpected value in moveRandomly(bias):" + biasedDirection);
        };
    }

    /* Location is a pixel  */
    public void moveTowards(int[] location) throws ArrayIndexOutOfBoundsException, InterruptedException {
        if (location.length != 2) {
            throw new ArrayIndexOutOfBoundsException("Location array must have exactly 2 elements: {X,Y}");
        }
        int x = location[0];
        int y = location[1];

        rotateTowards(location);
        col_checker.checkTile(this);


        if (collisionOn)
            return;

        // check this
        if (isFoodDepleted(x / tileSize, y / tileSize))
            return;

        stepForward();

    }

    private void rotateTowards(int[] location) {
        int targetX = location[0];
        int targetY = location[1];

        if (worldX > targetX) {
            direction = LEFT;
        } else if (worldX < targetX) {
            direction = RIGHT;
        } else if (worldY > targetY) {
            direction = UP;
        } else if (worldY < targetY) {
            direction = DOWN;
        }
    }

    private void stepForward() {
        switch (direction) {
            case UP:
                worldY -= speed;
                break;
            case DOWN:
                worldY += speed;
                break;
            case LEFT:
                worldX -= speed;
                break;
            case RIGHT:
                worldX += speed;
                break;
        }
    }

    private void consumeFood() {
        hunger = 0;
        gotFood = false;
        currentAction = SEARCH_FOOD;
        logSimulation(MEAL, this);
    }

    private void increaseHunger() {
        if (hunger == MAX_HUNGER) {
            if (gotFood) {
                consumeFood();
            } else {
                die(DEATH_STARVATION);
            }
        } else {
            hunger++;
        }
    }

    private void die(SimulationEventType causeOfDeath) {
        alive = false;
        antThreadMap.remove(this);
        logSimulation(causeOfDeath, this);
    }

    public void draw(Graphics2D g2) {

        BufferedImage image = switch (direction) {
            case UP -> upSprite;
            case DOWN -> downSprite;
            case LEFT -> leftSprite;
            case RIGHT -> rightSprite;
        };

        if (alive) {
            g2.drawImage(image, worldX, worldY, tileSize, tileSize, null);
        }
    }

    public int getId() {
        return this.id;
    }

    public int[] getNestCoordinates() {
        return new int[]{nestPosX, nestPosY};
    }

    public void setNestDetected(boolean detected) {
        this.nestDetected = detected;
    }

    private boolean isFoodDepleted(int x, int y) {
        for (Food foodItem : foods) {
            int[] foodItemsLocation = foodItem.getFoodLocation();
            if (Arrays.equals(foodItemsLocation, new int[]{x, y})) {
                return foodItem.getQuantity() == 0;
            }
        }
        return false;
    }

    private boolean isPheromoneDepleted(int x, int y) {
        return pheromoneGrid[x][y] == null || pheromoneGrid[x][y].getLevel() == 0;
    }

    public void reproduceWith(Ant partnerAnt) {
        logSimulation(REPRODUCTION, this);
        logSimulation(REPRODUCTION, partnerAnt);
        reproducedCounter++;
        partnerAnt.reproducedCounter++;

        detectedFoodTile = new int[]{-1, -1};
        nestDetected = false;
        detectedHomePheromoneTile = new int[]{-1, -1};
        detectedFoodPheromoneTile = new int[]{-1, -1};
        partnerAnt.detectedFoodTile = new int[]{-1, -1};
        partnerAnt.nestDetected = false;
        partnerAnt.detectedHomePheromoneTile = new int[]{-1, -1};
        partnerAnt.detectedFoodPheromoneTile = new int[]{-1, -1};

        consumeFood();
        partnerAnt.consumeFood();

        nest.removeAntReady(partnerAnt);

        if (reproducedCounter == 5) {
            die(DEATH_AGE);
        }

        if (partnerAnt.reproducedCounter == 5) {
            partnerAnt.die(DEATH_AGE);
        }

        new Ant();
    }

    private void depositPheromone(int[] previousLocation) {
        if (isHome) return;

        int prevX = previousLocation[0] / tileSize;
        int prevY = previousLocation[1] / tileSize;

        if (prevX >= 0 && prevX < maxScreenCol && prevY >= 0 && prevY < maxScreenRow) {
            if (pheromoneGridLock.writeLock().tryLock()) {
                try {
                    Pheromone pheromone;
                    if (gotFood)
                        pheromone = new Pheromone(prevX * tileSize, prevY * tileSize, PheromoneType.HOME, this.id);
                    else
                        pheromone = new Pheromone(prevX * tileSize, prevY * tileSize, PheromoneType.FOOD, this.id);
                    pheromoneGrid[prevX][prevY] = pheromone;
                } finally {
                    pheromoneGridLock.writeLock().unlock();
                }
            }
        }
    }

    public int[] getAntLocation() {
        return new int[]{this.worldX, this.worldY};
    }

    public int getReproducedCounter() {
        return this.reproducedCounter;
    }
}
