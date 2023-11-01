package window;

import entity.Ant;

public class CollisionChecker {
    Panel ap;
    public CollisionChecker(Panel ap){
        this.ap=ap;
    }

    public void checkTile(Ant ant){
        int entityLeftWorldX= ant.worldX+ant.solidArea.x;
        int entityRightWorldX= ant.worldX+ant.solidArea.x+ant.solidArea.width;
        int entityTopWorldY=ant.worldY+ant.solidArea.y;
        int entityBotWorldY=ant.worldY+ant.solidArea.y+ant.solidArea.height;

        int entityLeftCol=entityLeftWorldX/ap.tileSize;
        int entityRightCol=entityRightWorldX/ap.tileSize;
        int entityTopRow=entityTopWorldY/ap.tileSize;
        int entityBotRow=entityBotWorldY/ap.tileSize;

        int tileNum1,tileNum2;
        switch(ant.direction){
            case "up":
                entityTopRow=(entityTopWorldY-ant.speed)/ap.tileSize;
                tileNum1=ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2=ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    ant.collisionOn=true;
                }
                break;
            case "down":
                entityBotRow=(entityBotWorldY+ant.speed)/ap.tileSize;
                //System.out.println(entityTopRow+" "+entityBotRow+" "+entityLeftCol+" "+entityRightCol);
                tileNum1=ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];
                tileNum2=ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    ant.collisionOn=true;
                }
                break;
            case "left":
                entityLeftCol=(entityLeftWorldX-ant.speed)/ap.tileSize;
                tileNum1=ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2=ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    ant.collisionOn=true;
                }
                break;
            case "right":
                entityRightCol=(entityRightWorldX+ant.speed)/ap.tileSize;
                tileNum1=ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];
                tileNum2=ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    ant.collisionOn=true;
                }
                break;

        }
    }
}
