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
    public int visionRadius = 7;
    public boolean detectedFood = false;
    public int[] detectedFoodTile = new int[]{-1, -1};
    public int[] detectedHomePheromoneTile = new int[]{-1, -1};
    public int[] detectedFoodPheromoneTile = new int[]{-1, -1};
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
     *
     * The map is made of tiles. We can refer to a tile by using (Col,Row). (0,0) is top left.
     * Each tile is a square of (tileSize * tileSize) pixels. We can refer to a pixel by using (X,Y). (0,0) is top left.
     * The position of an entity is its top left pixel.
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
            case DIRECT:

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

            case PHEROMONE:
                int[] pheromonePosition = {detectedHomePheromoneTile[0] * tileSize, detectedHomePheromoneTile[1] * tileSize};
                if (!loggedUseHomePheromone) {
                    logSimulation(USE_HOME_PH, this);
                    loggedUseHomePheromone = true;
                }
                moveTowards(pheromonePosition);
//                detectedHomePheromones = new int[]{-1,-1};
                break;

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

    /* Location is a pixel  */
    public void moveTowards(int[] location) throws ArrayIndexOutOfBoundsException, InterruptedException {
        if (location.length != 2) {
            throw new ArrayIndexOutOfBoundsException("Location array must have exactly 2 elements: {X,Y}");
        }
        int x = location[0];
        int y = location[1];

        col_checker.checkTile(this);

        rotateTowards(location);

        if (collisionOn)
            return;

        if (isFoodDepleted(x / tileSize, y / tileSize))
            return;

        stepForward();

    }

    private void rotateTowards(int[] location) {
        int targetX = location[0];
        int targetY = location[1];

        if (worldX > targetX) {
            direction = Direction.LEFT;
        } else if (worldX < targetX) {
            direction = Direction.RIGHT;
        } else if (worldY > targetY) {
            direction = Direction.UP;
        } else if (worldY < targetY) {
            direction = Direction.DOWN;
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
        detectedFoodTile = new int[]{-1, -1};
        nestDetected = false;
        detectedHomePheromoneTile = new int[]{-1, -1};
        detectedFoodPheromoneTile = new int[]{-1, -1};
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
