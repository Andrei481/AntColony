package entity;

import window.Panel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Ant implements Runnable {
    public int worldX, worldY;
    public int speed;
    private int id;

    public BufferedImage up, down, left, right;
    public String direction;
    public Rectangle solidArea;
    public boolean collisionOn = false;
    public boolean foundFood=false;
    public boolean isHome=false;

    Panel ap;
    private int actionLock = 0;
    private Pheromone[][] pheromoneGrid;
    private int startPosX,startPosY;
    private int nestPosX,nestPosY;

    public Ant(Panel ap,int id) {
        this.id=id;
        this.ap = ap;
        solidArea = new Rectangle(0, 0, ap.tileSize, ap.tileSize);
        pheromoneGrid = new Pheromone[ap.maxScreenCol][ap.maxScreenRow];
        setDefaultValues();
        getPlayerImages();
        Random random = new Random();
        worldX = startPosX;
        worldY = startPosY;
    }

    public void depositPheromone(int prevX, int prevY) {
        if (prevX >= 0 && prevX < ap.maxScreenCol && prevY >= 0 && prevY < ap.maxScreenRow) {
            Pheromone pheromone;
            if (foundFood)
                pheromone = new Pheromone(prevX * ap.tileSize, prevY * ap.tileSize, PheromoneType.HOME);
            else
                pheromone = new Pheromone(prevX * ap.tileSize, prevY * ap.tileSize, PheromoneType.FOOD);
            pheromoneGrid[prevX][prevY] = pheromone;
        }
    }

    public void setDefaultValues() {
        startPosX=13*ap.tileSize;
        startPosY=13*ap.tileSize;
        nestPosX=5*ap.tileSize;
        nestPosY=5*ap.tileSize;
        speed = 3;
        direction = "down";
    }

    public void getPlayerImages() {
        try {
            up = ImageIO.read(new FileInputStream("res/ant_sprites/up.png"));
            down = ImageIO.read(new FileInputStream("res/ant_sprites/down.png"));
            right = ImageIO.read(new FileInputStream("res/ant_sprites/right.png"));
            left = ImageIO.read(new FileInputStream("res/ant_sprites/left.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAction() {

        actionLock++;
        if (actionLock == 5) {
            if (!foundFood) {
                Random random = new Random();
                int random_dir = random.nextInt(125);
                if (random_dir < 25) {
                    direction = "up";
                } else if (random_dir < 50) {
                    direction = "down";
                } else if (random_dir < 75) {
                    direction = "left";
                } else {
                    direction = "right";
                }
                actionLock = 0;
                // Check collision
                collisionOn = false;
                ap.col_checker.checkTile(this);
                // if collision = false, can move
                if (!collisionOn) {
                    switch (direction) {
                        case "up":
                            worldY -= speed;
                            break;
                        case "down":
                            worldY += speed;
                            break;
                        case "left":
                            worldX -= speed;
                            break;
                        case "right":
                            worldX += speed;
                            break;
                    }
                }
            } else {
                actionLock = 0;

                boolean moved = false;
                collisionOn=false;
                if (worldX > nestPosX && !moved) {
                    direction = "left";
                    ap.col_checker.checkTile(this);
                    if (!collisionOn) {
                        worldX -= speed;
                        moved = true;
                    }
                }
                if (worldX < nestPosX && !moved) {
                    direction = "right";
                    ap.col_checker.checkTile(this);
                    if (!collisionOn) {
                        worldX += speed;
                        moved = true;
                    }
                }

                if (worldY > nestPosY && !moved) {
                    direction = "up";
                    ap.col_checker.checkTile(this);
                    if (!collisionOn) {
                        worldY -= speed;
                        moved = true;
                    }
                }
                if (worldY < nestPosY && !moved) {
                    direction = "down";
                    worldY += speed;
                }
                //System.out.println("Ant " + id +foundFood+isHome+ " has found food, is going "+direction);
            }
        }


    }

    public void update() {
        int prevX = worldX;
        int prevY = worldY;
        setAction();
//        if(foundFood)
        depositPheromone(prevX / ap.tileSize, prevY / ap.tileSize); // this will leave a pheromone behind each move
        for(int x=0;x<ap.maxScreenCol;x++){
            for(int y=0;y<ap.maxScreenRow;y++)
                if(pheromoneGrid[x][y]!=null)
                    pheromoneGrid[x][y].update();
            }
    }

    @Override
    public void run() {
        while (true) {
            update();
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch (direction) {
            case "up":
                image = up;
                break;
            case "down":
                image = down;
                break;
            case "left":
                image = left;
                break;
            case "right":
                image = right;
                break;
        }

        g2.drawImage(image, worldX, worldY, ap.tileSize * 2, ap.tileSize * 2, null);

        for (int i = 0; i < ap.maxScreenCol; i++) {
            for (int j = 0; j < ap.maxScreenRow; j++) {
                Pheromone pheromone = pheromoneGrid[i][j];
                if (pheromone != null) {
                    pheromone.draw(g2);
                }
            }
        }
    }
}
