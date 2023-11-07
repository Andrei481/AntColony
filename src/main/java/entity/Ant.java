package entity;

import window.Panel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.Random;

public class Ant implements Runnable {
    public int worldX, worldY;
    public int speed;

    public BufferedImage up, down, left, right;
    public String direction;
    public Rectangle solidArea;
    public boolean collisionOn = false;
    public boolean foundFood=false;
    public boolean isHome=false;

    Panel ap;
    private int actionLock = 0;
    private Pheromone[][] pheromoneGrid;

    public Ant(Panel ap) {
        this.ap = ap;
        solidArea = new Rectangle(0, 0, ap.tileSize, ap.tileSize);
        pheromoneGrid = new Pheromone[ap.maxScreenCol][ap.maxScreenRow];
        setDefaultValues();
        getPlayerImages();
        Random random = new Random();
        worldX = 10 * ap.tileSize;
        worldY = 10 * ap.tileSize;
    }

    public void depositPheromone(int prevX, int prevY) {
        if (prevX >= 0 && prevX < ap.maxScreenCol && prevY >= 0 && prevY < ap.maxScreenRow) {
            pheromoneGrid[prevX][prevY] =new Pheromone(prevX * ap.tileSize, prevY * ap.tileSize);
        }
    }

    public void setDefaultValues() {
        worldX = ap.screenWidth / 2 - (ap.tileSize / 2);
        worldY = ap.screenHeight / 2 - (ap.tileSize / 2);
        speed = 10;
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
        }
    }

    public void update() {
        int prevX = worldX;
        int prevY = worldY;
        setAction();
        if(foundFood)
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
                Thread.sleep(100);
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
