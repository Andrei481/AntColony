package utils;

import entity.Pheromone;

public class EvaporationThread extends Thread {
    private Pheromone[][] pheromoneGrid;
    private boolean running;

    public EvaporationThread(Pheromone[][] pheromoneGrid) {
        this.pheromoneGrid = pheromoneGrid;
        this.running = true;
    }

    public void stopEvaporation() {
        running = false;
    }

    @Override
    public void run() {
        // Define the evaporation rate and sleep duration
        double evaporationRate = 0.1;  // Adjust the rate as needed
        long sleepDuration = 1000;  // Sleep duration in milliseconds

        while (running) {
            // Iterate through the pheromoneGrid and update pheromone levels
            for (int i = 0; i < pheromoneGrid.length; i++) {
                for (int j = 0; j < pheromoneGrid[0].length; j++) {
                    Pheromone pheromone = pheromoneGrid[i][j];
                    if (pheromone != null) {
                        // Reduce pheromone level by the evaporation rate
                        pheromone.evaporate();
                    }
                }
            }

            try {
                // Sleep for the specified duration
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
