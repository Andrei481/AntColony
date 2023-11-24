package tile;

import entity.Food;
import window.Panel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/* This class is responsible for loading the map. A single instance is created in Panel.java.
 * On creation, it draws the initial map.
 * It also redraws the entire map every frame which I think is wasteful!
 * TO DO: redraw only what is necessary! */

public class TileManager {
    Panel ap;
    public TileType[] tile;         /* Contains all available tile types: 0 = land, 1 = nest, 2 = food, 3 = wall */
    public int[][] mapTileNum;  /* The tile map: a matrix that contains the type of each tile */

    public TileManager(Panel ap){
        this.ap=ap;
        tile=new TileType[4];
        mapTileNum=new int[ap.maxScreenCol][ap.maxScreenRow];
        getTileImage();
        ap.foods = new ArrayList<>();
        loadMap("res/maps/map1.txt");
    }


    public void getTileImage(){
        try{
            tile[0]=new TileType();
            tile[0].image=ImageIO.read(new FileInputStream("res/tiles/land.png"));

            tile[1]=new TileType();
            tile[1].image=ImageIO.read(new FileInputStream("res/tiles/nest.png"));
            tile[1].collision=false;
            tile[1].isHome=true;

            tile[2]=new TileType();
            tile[2].image=ImageIO.read(new FileInputStream("res/tiles/food.png"));
            tile[2].collision=true;
            tile[2].isFood=true;

            tile[3]=new TileType();
            tile[3].image=ImageIO.read(new FileInputStream("res/tiles/wall.png"));
            tile[3].collision=true;
        }catch(IOException e){e.printStackTrace();}
    }

    public void draw(Graphics2D g2){
        int worldCol=0;
        int worldRow=0;

        while(worldCol<ap.maxScreenCol&&worldRow<ap.maxScreenRow){
            int tileNum=mapTileNum[worldCol][worldRow];
            int worldX=worldCol*ap.tileSize;
            int worldY=worldRow*ap.tileSize;
            g2.drawImage(tile[tileNum].image,worldX,worldY,ap.tileSize,ap.tileSize,null);
            worldCol++;

            if(worldCol==ap.maxScreenCol){
                worldCol=0;
                worldRow++;
            }
        }
    }

    public void loadMap(String filepath){
        try{
            InputStream is=new FileInputStream(filepath);
            BufferedReader br=new BufferedReader(new InputStreamReader(is));

            int col=0;
            int row=0;
            while(col<ap.maxScreenCol&&row<ap.maxScreenRow){
                String line=br.readLine();
                while(col<ap.maxScreenCol) {
                    String[] numbers = line.split(" ");
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    if(num == 2) {
                        Food food = new Food(col, row, ap);
                        ap.foods.add(food);
                    }
                    col++;
                }
                    if(col==ap.maxScreenCol){
                        col=0;row++;
                    }
            }
            br.close();
        }catch (Exception e){e.printStackTrace();}
    }
    public int[][] getMap(){return mapTileNum;}
}
