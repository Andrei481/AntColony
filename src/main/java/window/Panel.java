package window;

import entity.Ant;
import entity.Pheromone;
import tile.Tile_manager;
import utils.EvaporationThread;
import utils.Logger;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Panel extends JPanel implements Runnable {

    //screen settings
    final int originalTileSize=10;
    final int scale=1;

    public final int tileSize=originalTileSize*scale;
    public final int maxScreenCol=100;
    public final int maxScreenRow=100;
    public final int screenWidth=tileSize*maxScreenCol;
    public final int screenHeight=tileSize*maxScreenRow;
    public int reproducedCounter;
    int FPS=60;
    public Tile_manager tile_manager=new Tile_manager(this);
    Thread GUIThread;
    private Semaphore foodSemaphore = new Semaphore(1);
    private Semaphore reproduceSemaphore = new Semaphore(1);
    public CollisionChecker col_checker=new CollisionChecker(this, foodSemaphore, reproduceSemaphore);
    private ArrayList<Ant> ants = new ArrayList<>();
    private ArrayList<Thread> threadList=new ArrayList<>();
    public Pheromone[][] pheromoneGrid;
    private EvaporationThread evaporationThread;

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
        evaporationThread = new EvaporationThread(this.pheromoneGrid);
        evaporationThread.start();
    }


    @Override
    public void run() {
        double drawInterval=1000000000/FPS;
        double nextDrawTime=System.nanoTime()+drawInterval;
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

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D) g;
        tile_manager.draw(g2);
        for(Ant ant : ants) {
            ant.draw(g2);
        }
        g2.dispose();
    }
}
