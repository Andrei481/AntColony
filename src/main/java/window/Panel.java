package window;

import entity.Ant;
import entity.Food;
import entity.Pheromone;
import entity.Nest;
import tile.TileManager;
import utils.EvaporationThread;
import utils.Logger;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import com.rabbitmq.client.*;
import java.awt.image.BufferedImage;

public class Panel extends JLayeredPane implements Runnable {

    //screen settings
    final int originalTileSize=10;
    final int scale=1;

    public final int tileSize=originalTileSize*scale;
    public final int maxScreenCol=100;
    public final int maxScreenRow=100;
    public final int screenWidth=tileSize*maxScreenCol;
    public final int screenHeight=tileSize*maxScreenRow;
    int FPS=60;
    public TileManager tile_manager=new TileManager(this);
    Thread GUIThread;
    private final Semaphore foodSemaphore = new Semaphore(1);
    private final Semaphore reproduceSemaphore = new Semaphore(1);
    public CollisionChecker col_checker=new CollisionChecker(this, foodSemaphore, reproduceSemaphore);
    public ArrayList<Ant> ants = new ArrayList<>();
    public ArrayList<Thread> threadList=new ArrayList<>();
    public Pheromone[][] pheromoneGrid;
    public ArrayList<Food> foods;
    public Nest nest = new Nest();
    private BufferedImage bufferedMap;
    private boolean mapUpdateNeeded = false;
    public Panel(){
        this.setPreferredSize(new Dimension(screenWidth,screenHeight));
        this.setBackground(Color.green);
        this.setDoubleBuffered(true);
        this.pheromoneGrid = new Pheromone[maxScreenCol][maxScreenRow];

        int antNumber = 10;
        for(int i = 0; i < antNumber; i++) {
            Ant ant = new Ant(this,i+1);
            ants.add(ant);
            threadList.add(new Thread(ants.get(ants.size()-1)));
            Logger.logSimulation("Ant " + ant.getID() + " has spawned");
        }
        EvaporationThread evaporationThread = new EvaporationThread(this.pheromoneGrid);
        evaporationThread.start();
    }


    @Override
    public void run() {
        double drawInterval= (double) 1000000000 /FPS;
        double nextDrawTime=System.nanoTime()+drawInterval;
        bufferedMap = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        tile_manager.draw((Graphics2D) bufferedMap.getGraphics());


        for(Thread thread:threadList){
            thread.start();
        }
        while(GUIThread!=null){
            long currentTime=System.nanoTime();
            //System.out.println("Threads used:"+java.lang.Thread.activeCount()+" FoodScore="+foodScore);
            repaint();
            try{
                double remainingTime=nextDrawTime-System.nanoTime();
                remainingTime=remainingTime/100000;

                if (remainingTime<0){remainingTime=0;}
                Thread.sleep((long)remainingTime);
                nextDrawTime+=drawInterval;
            }catch (InterruptedException e){e.printStackTrace();}
        }

    }

    public void startRunThread() {
        GUIThread=new Thread(this);
        GUIThread.start();
        Logger.logInfo("GUI started");
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (mapUpdateNeeded) {
            /* Redraw the map tile by tile */
            tile_manager.draw( (Graphics2D) bufferedMap.getGraphics() );
            mapUpdateNeeded = false;
        }
        g2.drawImage(bufferedMap, 0, 0, this);  /* Draw the map from the previous frame (no changes) */

        /* Make a copy of the ant list to avoid concurrency problems.
         * e.g. ant spawns while we are drawing the ants */
        ArrayList<Ant> antsBuffer = new ArrayList<>(ants);
        for(Ant ant : antsBuffer) {
            ant.draw(g2);
        }
        g2.dispose();
    }

    public void updateBufferedMap() {
        mapUpdateNeeded = true;
    }
}
