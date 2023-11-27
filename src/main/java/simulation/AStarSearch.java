package simulation;

import definitions.TileType;

import java.util.LinkedList;

public class AStarSearch {
    private final int startX;
    private final int startY;
    private final int goalX;
    private final int goalY;

    public AStarSearch(int startX, int startY, int goalX, int goalY) {
        this.startX = startX;
        this.startY = startY;
        this.goalX = goalX;
        this.goalY = goalY;
    }

    public LinkedList<TileType> search(TileType start, TileType goal) {
        return null;
    }

}
