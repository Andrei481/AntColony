package simulation;

import entities.Pheromone;

import static simulation.SimulationMain.pheromoneGrid;

public class Evaporation implements Runnable {
    private final int updateIntervalMillis = 3500;
    private boolean isRunning = true;

    public static void launch() {
        Thread evaporationThread = new Thread(new Evaporation());
        evaporationThread.start();
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
