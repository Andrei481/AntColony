package window;

import entity.Ant;

public class CollisionChecker {
    Panel ap;
    public CollisionChecker(Panel ap){
        this.ap=ap;
    }

    public void checkTile(Ant ant) {
        int entityLeftWorldX = ant.worldX + ant.solidArea.x;
        int entityRightWorldX = ant.worldX + ant.solidArea.x + ant.solidArea.width;
        int entityTopWorldY = ant.worldY + ant.solidArea.y;
        int entityBotWorldY = ant.worldY + ant.solidArea.y + ant.solidArea.height;

        int entityLeftCol = entityLeftWorldX / ap.tileSize;
        int entityRightCol = entityRightWorldX / ap.tileSize;
        int entityTopRow = entityTopWorldY / ap.tileSize;
        int entityBotRow = entityBotWorldY / ap.tileSize;

        int tileNum1 = 0, tileNum2 = 0;
        boolean collision = false;
        boolean food=false;

        if (entityLeftCol >= 0 && entityRightCol >= 0 &&
                entityLeftCol < ap.tile_manager.mapTileNum.length &&
                entityRightCol < ap.tile_manager.mapTileNum.length &&
                entityTopRow >= 0 && entityBotRow >= 0 &&
                entityTopRow < ap.tile_manager.mapTileNum[0].length) {

            switch (ant.direction) {
                case "up":
                    entityTopRow = (entityTopWorldY - ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];

                    break;
                case "down":
                    entityBotRow = (entityBotWorldY + ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];

                    break;
                case "left":
                    entityLeftCol = (entityLeftWorldX - ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityLeftCol][entityTopRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityLeftCol][entityBotRow];

                    break;
                case "right":
                    entityRightCol = (entityRightWorldX + ant.speed) / ap.tileSize;
                    tileNum1 = ap.tile_manager.mapTileNum[entityRightCol][entityTopRow];
                    tileNum2 = ap.tile_manager.mapTileNum[entityRightCol][entityBotRow];

                    break;
            }
            collision = ap.tile_manager.tile[tileNum1].collision || ap.tile_manager.tile[tileNum2].collision;
            food = ap.tile_manager.tile[tileNum1].isFood || ap.tile_manager.tile[tileNum2].isFood;

        }
        ant.foundFood=food;
        ant.collisionOn = collision;
    }

}
