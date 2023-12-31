package simulation;

import entities.Ant;
import entities.Food;
import entities.Nest;
import entities.Pheromone;
import screens.SimulationScreen;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static screens.SimulationScreen.maxScreenCol;
import static screens.SimulationScreen.maxScreenRow;

public class SimulationMain implements Runnable {
    private static final Semaphore foodSemaphore = new Semaphore(1);
    private static final Random rng = new Random();
    public static CollisionChecker col_checker = new CollisionChecker(foodSemaphore);
    public static Pheromone[][] pheromoneGrid = new Pheromone[maxScreenCol][maxScreenRow];
    public static ReentrantReadWriteLock pheromoneGridLock = new ReentrantReadWriteLock();
    public static ArrayList<Food> foods;
    public static Nest nest = new Nest();
    public static int antIdCount = 0;
    public static Map<Ant, Thread> antThreadMap = new ConcurrentHashMap<>();

    public static void launch() {
        Thread simulationThread = new Thread(new SimulationMain());
        simulationThread.start();
    }

    @Override
    public void run() {
        spawnAnts(10);
        Evaporation.launch();
        SimulationScreen.launch();
    }

    private static void spawnAnts(int count) {
        for (int i = 0; i < count; i++) {
            new Ant();
        }
    }

    public static int getRandomNumber(int maxNumber) {
        /* Outputs a random number in the range [1,maxNumber] */

        return rng.nextInt(maxNumber) + 1;
    }
}
