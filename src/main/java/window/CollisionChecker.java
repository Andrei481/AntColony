package window;

import entity.Entity;

public class CollisionChecker {
    Panel ap;
    public CollisionChecker(Panel ap){
        this.ap=ap;
    }

    public void checkTile(Entity entity){
        int entityLeftWorldX= entity.worldX+entity.solidArea.x;
        int entityRightWorldX= entity.worldX+entity.solidArea.x+entity.solidArea.width;
        int entityTopWorldY=entity.worldY+entity.solidArea.y;
        int entityBotWorldY=entity.worldY+entity.solidArea.y+entity.solidArea.height;

        int entityLeftCol=entityLeftWorldX/ap.tileSize;
        int entityRightCol=entityRightWorldX/ap.tileSize;
        int entityTopRow=entityTopWorldY/ap.tileSize;
        int entityBotRow=entityBotWorldY/ap.tileSize;

        int tileNum1,tileNum2;
        switch(entity.direction){
            case "up":
                entityTopRow=(entityTopWorldY-entity.speed)/ap.tileSize;
                tileNum1=ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2=ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    entity.collisionOn=true;
                }
                break;
            case "down":
                entityBotRow=(entityBotWorldY+entity.speed)/ap.tileSize;
                //System.out.println(entityTopRow+" "+entityBotRow+" "+entityLeftCol+" "+entityRightCol);
                tileNum1=ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];
                tileNum2=ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    entity.collisionOn=true;
                }
                break;
            case "left":
                entityLeftCol=(entityLeftWorldX-entity.speed)/ap.tileSize;
                tileNum1=ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2=ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    entity.collisionOn=true;
                }
                break;
            case "right":
                entityRightCol=(entityRightWorldX+entity.speed)/ap.tileSize;
                tileNum1=ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];
                tileNum2=ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];
                if(ap.tile_manager.tile[tileNum1].collision==true||ap.tile_manager.tile[tileNum2].collision==true){
                    entity.collisionOn=true;
                }
                break;

        }
    }
}
