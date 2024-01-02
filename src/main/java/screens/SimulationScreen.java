package screens;

import entities.Ant;
import entities.Pheromone;
import simulation.TileManager;
import utils.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static java.lang.Thread.sleep;
import static simulation.SimulationMain.antThreadMap;
import static simulation.SimulationMain.pheromoneGrid;

/* Keep only graphical things in this file */

public class SimulationScreen extends JLayeredPane implements Runnable {

    public static final int maxScreenCol = 50;
    public static final int maxScreenRow = 30;
    private final static int originalTileSize = 10;
    private final static int scale = 2;
    public static int tileSize = originalTileSize * scale;
    public static int screenWidth = tileSize * maxScreenCol;
    public static final int screenHeight = tileSize * maxScreenRow;
    public static TileManager tile_manager = new TileManager();
    private static Thread SimulationScreenThread;
    private static boolean mapUpdateNeeded = true;
    private static final int FPS = 30;
    private static final int drawIntervalMillis = 1000 / FPS; // 1 sec = 1000 ms
    private static long nextDrawTimeMillis;
    private static BufferedImage bufferedMap;
    public static BufferedImage upSprite, downSprite, rightSprite, leftSprite;
    private static SimulationScreen simulationScreen;


    public static void launch() {
        simulationScreen = new SimulationScreen();
        SimulationScreenThread = new Thread(simulationScreen);
        SimulationScreenThread.start();
        Logger.logInfo("GUI started");
    }
    @Override
    public void run() {
        createAppWindow();
        bufferedMap = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        getAntSprites();

        while (SimulationScreenThread != null) {
            nextDrawTimeMillis = System.currentTimeMillis() + drawIntervalMillis;
            repaint();

            try {
                sleepUntilNextFrame();
            } catch (InterruptedException e) {
                System.err.println("Screen drawing error: " + e.getMessage());
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        drawMap(g2);
        drawAnts(g2);
        drawPheromones(g2);

        g2.dispose();
    }

    public static void updateBufferedMap() {
        mapUpdateNeeded = true;
    }
    private void drawMap(Graphics2D g2) {
        if (mapUpdateNeeded) {
            /* Update map tile by tile if any tile changed */
            tile_manager.draw((Graphics2D) bufferedMap.getGraphics());
            mapUpdateNeeded = false;
        }

        /* Draw the map using the already computed buffer */
        g2.drawImage(bufferedMap, 0, 0, this);
    }
    private void drawAnts(Graphics2D g2) {
        /* Draw every ant */
        for (Map.Entry<Ant, Thread> entry : antThreadMap.entrySet()) {
            entry.getKey().draw(g2);
        }
    }
    private void drawPheromones(Graphics2D g2) {
        for (int i = 0; i < maxScreenCol; i++) {
            for (int j = 0; j < maxScreenRow; j++) {
                Pheromone pheromone = pheromoneGrid[i][j];
                if (pheromone != null) {
                    pheromone.draw(g2);
                }
            }
        }
    }

    private void sleepUntilNextFrame() throws InterruptedException {
        long remainingTimeMillis = nextDrawTimeMillis - System.currentTimeMillis();

        if (remainingTimeMillis > 0) {
            sleep(remainingTimeMillis);
        }
    }

    private void getAntSprites() {
        try {
            upSprite = ImageIO.read(new FileInputStream("res/ant_sprites/up.png"));
            downSprite = ImageIO.read(new FileInputStream("res/ant_sprites/down.png"));
            rightSprite = ImageIO.read(new FileInputStream("res/ant_sprites/right.png"));
            leftSprite = ImageIO.read(new FileInputStream("res/ant_sprites/left.png"));
        } catch (IOException e) {
            System.err.println("Error getting ant sprites: " + e.getMessage());
        }
    }
    private static void createAppWindow() {
        simulationScreen.setPreferredSize(new Dimension(screenWidth, screenHeight));
        simulationScreen.setBackground(Color.green);
        simulationScreen.setDoubleBuffered(true);

        JFrame appWindow = new JFrame();
        appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
        appWindow.setTitle("Ant Colony Simulation");
        appWindow.add(simulationScreen);
        appWindow.pack();
        appWindow.setLocationRelativeTo(null);
        appWindow.setVisible(true);
    }
}
