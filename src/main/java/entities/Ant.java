package entities;

import definitions.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static definitions.AntActionType.SEARCH_FOOD;
import static definitions.AntMovementType.*;
import static definitions.Direction.*;
import static definitions.SimulationEventType.*;
import static screens.SimulationScreen.*;
import static utils.Logger.logSimulation;

public class Ant implements Runnable {

    /* Number of moves before starving */
    public static final int MAX_HUNGER = 500;
    private final int id;
    public boolean isDead = false;
    public int worldX, worldY;
    public int speed;
    public AntActionType currentAction = SEARCH_FOOD;
    public BufferedImage upSprite, downSprite, leftSprite, rightSprite;
    public Direction direction;
    public Rectangle solidArea;
    public boolean collisionOn = false;
    public boolean gotFood = false;
    /* Variables used to send log message only once. */
    public boolean loggedFollowFood = false;
    public boolean loggedUseHomePheromone = false;
    public boolean loggedUseFoodPheromone = false;
    public boolean isHome = false;
    public boolean sentReadySignal = false;
    public int visionRadius = 10;
    public boolean detectedFood = false;
    public int[] detectedFoodCoords = new int[]{-1, -1};
    public int[] detectedHomePheromones = new int[]{-1, -1};
    public int[] detectedFoodPheromones = new int[]{-1, -1};
    private AntMovementType movement = RANDOM;
    private int hunger = 0;
    private int startPosX, startPosY;
    private int nestPosX, nestPosY;
    private int reproducedCounter;
    private boolean nestDetected = false;


    public Ant(int id) {
        this.id = id;
        solidArea = new Rectangle(0, 0, tileSize, tileSize);
        setDefaultValues();
        getAntSprites();
        worldX = startPosX;
        worldY = startPosY;
        this.reproducedCounter = 0;
    }

    /*
     * The Ant thread starts at run() and executes update() every 100 milliseconds.
     * The update() method takes care of 1 ant movement by calling setAction();
     * The draw() method is called by the SimulationScreen thread and places the ant on the screen.
     */

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                update();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                if (!Thread.currentThread().isInterrupted())
                    Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Ant update error: " + e.getMessage());
            }
        }
    }

    private void update() throws InterruptedException {
        try {
            int[] previousLocation = {worldX, worldY};
            performAction();
            depositPheromone(previousLocation);
            increaseHunger();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        return (detectedFoodCoords[0] >= 0 && detectedFoodCoords[1] >= 0
                && tile_manager.mapTileNum[detectedFoodCoords[0]][detectedFoodCoords[1]] == 2);
    }

    private boolean seesHomePheromone() {
        return (detectedHomePheromones[0] >= 0 && detectedHomePheromones[1] >= 0                                        // if collided with a pheromone
                && !isPheromoneDepleted(detectedHomePheromones[0], detectedHomePheromones[1])                           // if pheromone not empty
                && detectedHomePheromones[0] > worldX / tileSize && detectedHomePheromones[1] > worldY / tileSize);     // if pheromone is above and to the right of the ant?
    }

    private void searchFood() throws InterruptedException {

        if (seesFood())
            movement = DIRECT;
        else if (seesHomePheromone())
            movement = PHEROMONE;
        else
            movement = RANDOM_RIGHT;

        switch (movement) {
            case DIRECT:
                int foodX = detectedFoodCoords[0] * tileSize;
                int foodY = detectedFoodCoords[1] * tileSize;
                //System.out.println("ant found food:"+id+"\n food cords:"+foodX+" "+foodY);
                //collisionOn = true; // no clue what this does
                if (!loggedFollowFood) {
                    logSimulation(FOLLOW_FOOD, this);
                    loggedFollowFood = true;            // log FOLLOW_FOOD only once
                    loggedUseHomePheromone = false;     // move somewhere where we reset all flags
                }
                moveToPosition(foodX, foodY);
//                detectedFoodCoords = new int[]{-1,-1};
//                col_checker.checkTile(this);
                break;

            case PHEROMONE:
                int pheromoneX = detectedHomePheromones[0] * tileSize;
                int pheromoneY = detectedHomePheromones[1] * tileSize;
                if (!loggedUseHomePheromone) {
                    logSimulation(USE_HOME_PH, this);
                    loggedUseHomePheromone = true;
                }
                moveToPosition(pheromoneX, pheromoneY);
//                detectedHomePheromones = new int[]{-1,-1};
                break;

            case RANDOM_RIGHT:
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

    private boolean seesFoodPheromone() {
        return (this.detectedFoodPheromones[0] >= 0 &&
                this.detectedFoodPheromones[1] >= 0 &&
                !isPheromoneDepleted(detectedFoodPheromones[0], detectedFoodPheromones[1]) &&
                detectedFoodPheromones[0] < worldX / tileSize &&
                detectedFoodPheromones[1] < worldY / tileSize);
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
            case DIRECT:
                collisionOn = false;
                if (worldX > nestPosX) {
                    direction = LEFT;
                    col_checker.checkTile(this);
                    if (!collisionOn) {
                        worldX -= speed;
                        return;
                    }
                }
                if (worldX < nestPosX) {
                    direction = RIGHT;
                    col_checker.checkTile(this);
                    if (!collisionOn) {
                        worldX += speed;
                        return;
                    }
                }

                if (worldY > nestPosY) {
                    direction = UP;
                    col_checker.checkTile(this);
                    if (!collisionOn) {
                        worldY -= speed;
                        return;
                    }
                }
                if (worldY < nestPosY) {
                    direction = DOWN;
                    worldY += speed;
                }
                loggedUseFoodPheromone = false;
//                nestDetected = false;
                break;
            case PHEROMONE:
                int pheromoneX = detectedFoodPheromones[0] * tileSize;
                int pheromoneY = detectedFoodPheromones[1] * tileSize;
                if (!loggedUseFoodPheromone) {
                    logSimulation(USE_FOOD_PH, this);
                    loggedUseFoodPheromone = true;
                }

                moveToPosition(pheromoneX, pheromoneY);
//                detectedFoodPheromones = new int[]{-1,-1};
                break;

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
    }

    public void moveToPosition(int x, int y) throws InterruptedException {
        col_checker.checkTile(this);

        if (worldX > x) {
            direction = Direction.LEFT;
        } else if (worldX < x) {
            direction = Direction.RIGHT;
        } else if (worldY > y) {
            direction = Direction.UP;
        } else if (worldY < y) {
            direction = Direction.DOWN;
        }

        if (!collisionOn) {
            if (isFoodDepleted(x / tileSize, y / tileSize)) {
                return;
            }

            stepForward();
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

    private void increaseHunger() {
        if (gotFood)
            return;

        if (hunger == MAX_HUNGER) {
            logSimulation(SimulationEventType.DEATH_STARVATION, this);
            die();
        }

        hunger++;

    }

    private void die() {
        isDead = true;
        Thread.currentThread().interrupt();
    }

    public void draw(Graphics2D g2) {

        BufferedImage image = switch (direction) {
            case UP -> upSprite;
            case DOWN -> downSprite;
            case LEFT -> leftSprite;
            case RIGHT -> rightSprite;
        };

        if (!isDead) {
            g2.drawImage(image, worldX, worldY, tileSize * 2, tileSize * 2, null);
            g2.drawImage(image, 0, 0, tileSize * 2, tileSize * 2, null);
            for (int i = 0; i < maxScreenCol; i++) {
                for (int j = 0; j < maxScreenRow; j++) {
                    Pheromone pheromone = pheromoneGrid[i][j];
                    if (pheromone != null) {
                        pheromone.draw(g2);
                    }
                }
            }
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

    public void reproduce() {
        logSimulation(REPRODUCTION, this);
        reproducedCounter++;
        detectedFoodCoords = new int[]{-1, -1};
        nestDetected = false;
        detectedHomePheromones = new int[]{-1, -1};
        detectedFoodPheromones = new int[]{-1, -1};
        if (reproducedCounter == 5) {
            logSimulation(DEATH_AGE, this);
            die();
        }
        currentAction = SEARCH_FOOD;
    }

    private void depositPheromone(int[] previousLocation) {
        int prevX = previousLocation[0] / tileSize;
        int prevY = previousLocation[1] / tileSize;

        if (prevX >= 0 && prevX < maxScreenCol && prevY >= 0 && prevY < maxScreenRow) {
            Pheromone pheromone;
            if (gotFood)
                pheromone = new Pheromone(prevX * tileSize, prevY * tileSize, PheromoneType.HOME, this.id);
            else
                pheromone = new Pheromone(prevX * tileSize, prevY * tileSize, PheromoneType.FOOD, this.id);
            pheromoneGrid[prevX][prevY] = pheromone;
        }
    }

    private void setDefaultValues() {
        startPosX = 13 * tileSize;
        startPosY = 13 * tileSize;
        nestPosX = 5 * tileSize;
        nestPosY = 5 * tileSize;
        speed = 5;
        direction = DOWN;
    }

    private void getAntSprites() {
        try {
            upSprite = ImageIO.read(new FileInputStream("res/ant_sprites/up.png"));
            downSprite = ImageIO.read(new FileInputStream("res/ant_sprites/down.png"));
            rightSprite = ImageIO.read(new FileInputStream("res/ant_sprites/right.png"));
            leftSprite = ImageIO.read(new FileInputStream("res/ant_sprites/left.png"));
        } catch (IOException e) {
            System.err.println("Error getting ant sprites: " + e.getMessage());
        }
    }

    public int[] getAntLocation() {
        return new int[]{this.worldX, this.worldY};
    }

    public int getReproducedCounter() {
        return this.reproducedCounter;
    }
}
