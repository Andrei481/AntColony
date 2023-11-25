package simulation;

import definitions.TileType;
import screens.SimulationScreen;

import java.util.LinkedList;

public class AStarSearch {
    private final SimulationScreen ap;
    private final int startX;
    private final int startY;
    private final int goalX;
    private final int goalY;

    public AStarSearch(int startX, int startY, int goalX, int goalY, SimulationScreen ap) {
        this.startX = startX;
        this.startY = startY;
        this.goalX = goalX;
        this.goalY = goalY;
        this.ap = ap;
    }

    public LinkedList<TileType> search(TileType start, TileType goal) {
        return null;
    }

}
