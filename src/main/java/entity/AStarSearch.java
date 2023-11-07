package entity;

import tile.Tile;
import window.Panel;

import java.util.LinkedList;

public class AStarSearch{
    private Panel ap;
    private int startX,startY;
    private int goalX,goalY;
    public AStarSearch(int startX, int startY,int goalX,int goalY, Panel ap){
        this.startX=startX;
        this.startY=startY;
        this.goalX=goalX;
        this.goalY=goalY;
        this.ap=ap;
    }
    public LinkedList<Tile> search(Tile start, Tile goal){
        return null;
    }

}
