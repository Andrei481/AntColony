package simulation;

import definitions.TileType;
import entities.Food;
import screens.SimulationScreen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/* This class is responsible for loading the map. A single instance is created in Panel.java.
 * On creation, it draws the initial map.
 */

public class TileManager {
    public TileType[] tile;         /* Contains all available tile types: 0 = land, 1 = nest, 2 = food, 3 = wall */
    public int[][] mapTileNum;  /* The tile map: a matrix that contains the type of each tile */

    public TileManager() {
        tile = new TileType[4];
        mapTileNum = new int[SimulationScreen.maxScreenCol][SimulationScreen.maxScreenRow];
        getTileImage();
        SimulationScreen.foods = new ArrayList<>();
        loadMap("res/maps/map50x50.txt");
    }


    public void getTileImage() {
        try {
            tile[0] = new TileType();
            tile[0].image = ImageIO.read(new FileInputStream("res/tiles/land.png"));

            tile[1] = new TileType();
            tile[1].image = ImageIO.read(new FileInputStream("res/tiles/nest.png"));
            tile[1].collision = false;
            tile[1].isHome = true;

            tile[2] = new TileType();
            tile[2].image = ImageIO.read(new FileInputStream("res/tiles/food.png"));
            tile[2].collision = true;
            tile[2].isFood = true;

            tile[3] = new TileType();
            tile[3].image = ImageIO.read(new FileInputStream("res/tiles/wall.png"));
            tile[3].collision = true;
        } catch (IOException e) {
            System.err.println("Error getting tile image: " + e.getMessage());
        }
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < SimulationScreen.maxScreenCol && worldRow < SimulationScreen.maxScreenRow) {
            int tileNum = mapTileNum[worldCol][worldRow];
            int worldX = worldCol * SimulationScreen.tileSize;
            int worldY = worldRow * SimulationScreen.tileSize;
            g2.drawImage(tile[tileNum].image, worldX, worldY, SimulationScreen.tileSize, SimulationScreen.tileSize, null);
            worldCol++;

            if (worldCol == SimulationScreen.maxScreenCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

    public void loadMap(String filepath) {
        try {
            InputStream is = new FileInputStream(filepath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;
            while (col < SimulationScreen.maxScreenCol && row < SimulationScreen.maxScreenRow) {
                String line = br.readLine();
                while (col < SimulationScreen.maxScreenCol) {
                    String[] numbers = line.split(" ");
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    if (num == 2) {
                        Food food = new Food(col, row);
                        SimulationScreen.foods.add(food);
                    }
                    col++;
                }
                if (col == SimulationScreen.maxScreenCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error loading map: " + e.getMessage());
        }
    }

    public int[][] getMap() {
        return mapTileNum;
    }
}
