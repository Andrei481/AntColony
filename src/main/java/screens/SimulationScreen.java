package screens;

import entities.Ant;
import simulation.TileManager;
import utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import static java.lang.Thread.sleep;
import static simulation.SimulationMain.antThreadMap;

public class SimulationScreen extends JLayeredPane implements Runnable {

    public static final int maxScreenCol = 50;
    public static final int maxScreenRow = 50;
    final static int originalTileSize = 10;
    final static int scale = 2;
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

        if (mapUpdateNeeded) {
            /* Update the buffered map tile by tile if any tile changed */
            tile_manager.draw((Graphics2D) bufferedMap.getGraphics());
            mapUpdateNeeded = false;
        }

        /* Draw the map using the already computed buffer */
        g2.drawImage(bufferedMap, 0, 0, this);

        /* Draw every ant */
        for (Map.Entry<Ant, Thread> entry : antThreadMap.entrySet()) {
            entry.getKey().draw(g2);
        }

        g2.dispose();
    }

    public static void updateBufferedMap() {
        mapUpdateNeeded = true;
    }

    private void sleepUntilNextFrame() throws InterruptedException {
        long remainingTimeMillis = nextDrawTimeMillis - System.currentTimeMillis();

        if (remainingTimeMillis > 0) {
            sleep(remainingTimeMillis);
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
