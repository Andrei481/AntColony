package simulation;

import entities.Pheromone;

public class EvaporationThread extends Thread {
    private final Pheromone[][] pheromoneGrid;
    private final int updateIntervalMillis = 3500;
    private boolean isRunning = true;

    public EvaporationThread(Pheromone[][] pheromoneGrid) {
        this.pheromoneGrid = pheromoneGrid;
    }

    @Override
    public void run() {
        while (isRunning) {
            for (Pheromone[] pheromones : pheromoneGrid) {
                for (Pheromone pheromone : pheromones) {
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
