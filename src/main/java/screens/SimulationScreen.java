package screens;

import entities.Ant;
import entities.Food;
import entities.Nest;
import entities.Pheromone;
import simulation.CollisionChecker;
import simulation.EvaporationThread;
import simulation.TileManager;
import utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static definitions.SimulationEventType.BIRTH;
import static java.lang.Thread.sleep;

public class SimulationScreen extends JLayeredPane implements Runnable {

    public static final int maxScreenCol = 100;
    public static final int maxScreenRow = 100;
    //screen settings
    final static int originalTileSize = 10;
    final static int scale = 1;
    private static final Semaphore foodSemaphore = new Semaphore(1);
    private static final Semaphore reproduceSemaphore = new Semaphore(1);
    public static int tileSize = originalTileSize * scale;
    public static int screenWidth = tileSize * maxScreenCol;
    public static final int screenHeight = tileSize * maxScreenRow;
    public static TileManager tile_manager = new TileManager();
    public static CollisionChecker col_checker = new CollisionChecker(foodSemaphore, reproduceSemaphore);
    public static Pheromone[][] pheromoneGrid;
    public static ArrayList<Food> foods;
    public static Nest nest = new Nest();
    public static int antIdCount = 0;
    public static Map<Ant, Thread> antThreadMap = new ConcurrentHashMap<>();
    private static Thread GUIThread;
    private static boolean mapUpdateNeeded = false;
    int FPS = 60;
    private BufferedImage bufferedMap;


    public static void launch() {
        SimulationScreen screen = new SimulationScreen();

        screen.setPreferredSize(new Dimension(screenWidth, screenHeight));
        screen.setBackground(Color.green);
        screen.setDoubleBuffered(true);

        JFrame appWindow = new JFrame();
        appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
        appWindow.setTitle("Ant Colony Simulation");
        appWindow.add(screen);
        appWindow.pack();
        appWindow.setLocationRelativeTo(null);
        appWindow.setVisible(true);

        GUIThread = new Thread(screen);
        GUIThread.start();
        Logger.logInfo("GUI started");
    }

    public static void updateBufferedMap() {
        mapUpdateNeeded = true;
    }

    @Override
    public void run() {

        pheromoneGrid = new Pheromone[maxScreenCol][maxScreenRow];

        int initialAntCount = 10;
        for (int i = 0; i < initialAntCount; i++) {
            antIdCount++;
            Ant ant = new Ant(antIdCount);
            antThreadMap.put(ant, new Thread(ant));
            Logger.logSimulation(BIRTH, ant);
        }
        EvaporationThread evaporationThread = new EvaporationThread(pheromoneGrid);
        evaporationThread.start();

        double drawInterval = (double) 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        bufferedMap = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        tile_manager.draw((Graphics2D) bufferedMap.getGraphics());


        /* Start ant threads */
        for (Map.Entry<Ant, Thread> entry : antThreadMap.entrySet()) {
            entry.getValue().start();
        }
        while (GUIThread != null) {
            for (Map.Entry<Ant, Thread> entry : antThreadMap.entrySet()) {
                if (entry.getKey().isDead)
                    entry.getValue().interrupt();
            }
            antThreadMap.entrySet().removeIf(entry -> entry.getValue().isInterrupted());
            repaint();
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 100000;

                if (remainingTime < 0) {
                    remainingTime = 0;
                }
                sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                System.err.println("Screen drawing error: " + e.getMessage());
            }
        }

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (mapUpdateNeeded) {
            /* Redraw the map tile by tile */
            tile_manager.draw((Graphics2D) bufferedMap.getGraphics());
            mapUpdateNeeded = false;
        }
        g2.drawImage(bufferedMap, 0, 0, this);  /* Draw the map from the previous frame (no changes) */

        /* Draw every ant */

        /* antThreadMap is now a ConcurrentHashMap to avoid concurrency problems
         * e.g. ant spawns while we are drawing the ants */

        for (Map.Entry<Ant, Thread> entry : antThreadMap.entrySet()) {
            entry.getKey().draw(g2);
        }
        g2.dispose();
    }
}
