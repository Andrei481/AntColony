package tile;

import window.Panel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;

public class Tile_manager {
    Panel ap;
    public Tile[] tile;
    public int[][] mapTileNum;

    public Tile_manager(Panel ap){
        this.ap=ap;
        tile=new Tile[4];
        mapTileNum=new int[ap.maxScreenCol][ap.maxScreenRow];
        getTileImage();
        loadMap("res/maps/map1.txt");
    }


    public void getTileImage(){
        try{
            tile[0]=new Tile();
            tile[0].image=ImageIO.read(new FileInputStream("res/tiles/land.png"));

            tile[1]=new Tile();
            tile[1].image=ImageIO.read(new FileInputStream("res/tiles/nest.png"));
            tile[1].collision=true;
            tile[1].isHome=true;

            tile[2]=new Tile();
            tile[2].image=ImageIO.read(new FileInputStream("res/tiles/food.png"));
            tile[2].collision=true;
            tile[2].isFood=true;

            tile[3]=new Tile();
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
                    String numbers[] = line.split(" ");
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
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
