package utils;

import entity.Pheromone;

public class EvaporationThread extends Thread {
    private final Pheromone[][] pheromoneGrid;
    private boolean isRunning = true;
    private final int updateIntervalMillis = 2000;

    public EvaporationThread(Pheromone[][] pheromoneGrid) {
        this.pheromoneGrid = pheromoneGrid;
    }

    @Override
    public void run() {
        while (isRunning) {
            for (int x = 0; x < pheromoneGrid.length; x++) {
                for (int y = 0; y < pheromoneGrid[x].length; y++) {
                    Pheromone pheromone = pheromoneGrid[x][y];
                    if (pheromone != null) {
                        pheromone.evaporate();
                    }
                }
            }

            try {
                Thread.sleep(updateIntervalMillis);
            } catch (InterruptedException e) {
                // handle some bs here
            }
        }
    }

    public void stopEvaporation() {
        isRunning = false;
    }
}
