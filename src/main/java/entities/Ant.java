package entities;

import definitions.Direction;
import definitions.PheromoneType;
import definitions.SimulationEventType;
import utils.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import static definitions.Direction.*;
import static definitions.SimulationEventType.REPRODUCTION;
import static screens.SimulationScreen.*;

public class Ant implements Runnable {

    private final int id;
    public boolean isDead = false;
    public int worldX, worldY;
    public int speed;
    public BufferedImage upSprite, downSprite, leftSprite, rightSprite;
    public Direction direction;
    public Rectangle solidArea;
    public boolean collisionOn = false;
    public boolean gotFood = false;
    public boolean isHome = false;
    public boolean sentReadySignal = false;
    private int deadCount = 0;
    private int startPosX, startPosY;
    private int nestPosX, nestPosY;
    private int reproducedCounter;
    public int visionRadius = 20;
    public int[] detectedFoodCoords = new int[]{-1,-1};
    public int[] detectedHomeCoords = new int[]{-1,-1};



    public Ant(int id) {
        this.id = id;
        solidArea = new Rectangle(0, 0, tileSize, tileSize);
        setDefaultValues();
        getAntSprites();
        worldX = startPosX;
        worldY = startPosY;
        this.reproducedCounter = 0;
    }

    public int[] getAntLocation() {
        return new int[]{this.worldX, this.worldY};
    }

    public void reproduce() {
        this.reproducedCounter++;
        Logger.logSimulation(REPRODUCTION, this);
        detectedFoodCoords = new int[]{-1,-1};
        detectedHomeCoords = new int[]{-1,-1};
    }

    public int getReproducedCounter() {
        return this.reproducedCounter;
    }

    private void depositPheromone(int prevX, int prevY) {
        if (prevX >= 0 && prevX < maxScreenCol && prevY >= 0 && prevY < maxScreenRow) {
            Pheromone pheromone;
            if (gotFood)
                pheromone = new Pheromone(prevX * tileSize, prevY * tileSize, PheromoneType.HOME);
            else
                pheromone = new Pheromone(prevX * tileSize, prevY * tileSize, PheromoneType.FOOD);
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

    private void setAction() throws InterruptedException {
        if (!gotFood) {
            if (this.detectedFoodCoords[0] >= 0 && this.detectedFoodCoords[1] >= 0) {
                int foodX = detectedFoodCoords[0] * tileSize;
                int foodY = detectedFoodCoords[1] * tileSize;
                collisionOn = true;
                // Move towards the detected food
                moveToPosition(foodX, foodY);
            }
            else {
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

            }
        } else {
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
            //System.out.println("Ant " + id +foundFood+isHome+ " has found food, is going "+direction);
        }


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
            moveBasedOnDirection();
        }
    }

    private void moveBasedOnDirection() {
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

    private void update() throws InterruptedException {
        try {

            if (!isHome) {
                if (deadCount == 500) {
                    isDead = true;
                    Thread.currentThread().interrupt();
                    Logger.logSimulation(SimulationEventType.DEATH, this);
                    //throw new Exception("Ant " + this.id + " is dead");
                }
                deadCount++;
            }

            int prevX = worldX;
            int prevY = worldY;
            setAction();
            depositPheromone(prevX / tileSize, prevY / tileSize); // this will leave a pheromone behind each move
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public int getID() {
        return this.id;
    }
}
