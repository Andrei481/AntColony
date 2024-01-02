package simulation;

import entities.Pheromone;

import static simulation.SimulationMain.pheromoneGrid;

public class Evaporation implements Runnable {
    private static Thread evaporationThread;
    public static void launch() {
        evaporationThread = new Thread(new Evaporation());
        evaporationThread.start();
    }

    @Override
    public void run() {
        while (evaporationThread != null) {

            for (Pheromone[] pheromones : pheromoneGrid) {
                for (Pheromone pheromone : pheromones) {
                    if (pheromone != null) {
                        pheromone.evaporate();
                    }
                }
            }

            try {
                Thread.sleep(3500);

            } catch (InterruptedException e) {
                System.err.println("Pheromone evaporation error: " + e.getMessage());
            }
        }
    }

}
