package window;

import entity.Ant;
import entity.Food;
import entity.Pheromone;
import entity.Nest;
import tile.Tile_manager;
import utils.EvaporationThread;
import utils.Logger;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Panel extends JPanel implements Runnable {

    //screen settings
    final int originalTileSize=10;
    final int scale=1;

    public final int tileSize=originalTileSize*scale;
    public final int maxScreenCol=100;
    public final int maxScreenRow=100;
    public final int screenWidth=tileSize*maxScreenCol;
    public final int screenHeight=tileSize*maxScreenRow;
    int FPS=60;
    public Tile_manager tile_manager=new Tile_manager(this);
    Thread GUIThread;
    private Semaphore foodSemaphore = new Semaphore(1);
    private Semaphore reproduceSemaphore = new Semaphore(1);
    public CollisionChecker col_checker=new CollisionChecker(this, foodSemaphore, reproduceSemaphore);
    public ArrayList<Ant> ants = new ArrayList<>();
    public ArrayList<Thread> threadList=new ArrayList<>();
    public Pheromone[][] pheromoneGrid;
    private EvaporationThread evaporationThread;
    public ArrayList<Food> foods;
    public Nest nest = new Nest();
    public int id=0;


    public Map<Ant,Thread> threadMap=new HashMap<>();

    public Panel(){
        this.setPreferredSize(new Dimension(screenWidth,screenHeight));
        this.setBackground(Color.green);
        this.setDoubleBuffered(true);
        this.pheromoneGrid = new Pheromone[maxScreenCol][maxScreenRow];

        int antNumber = 10;
        for(int i = 0; i < antNumber; i++) {
            id++;
            Ant ant = new Ant(this,id);
            threadMap.put(ant,new Thread(ant));
            //ants.add(ant);
            //threadList.add(new Thread(ants.get(ants.size()-1)));
            Logger.logSimulation("Ant " + ant.getID() + " has spawned"+java.lang.Thread.activeCount());
        }
        evaporationThread = new EvaporationThread(this.pheromoneGrid);
        evaporationThread.start();
    }


    @Override
    public void run() {
        double drawInterval=1000000000/FPS;
        double nextDrawTime=System.nanoTime()+drawInterval;
        for(Map.Entry<Ant,Thread> entry:threadMap.entrySet()){
            System.out.println(entry.getKey().getID()+" "+entry.getValue().getName());
            entry.getValue().start();
        }
        while(GUIThread!=null){
            long currentTime=System.nanoTime();
            for(Map.Entry<Ant,Thread> entry:threadMap.entrySet()){
                if(entry.getKey().isDead)
                    entry.getValue().interrupt();
            }
            Iterator<Map.Entry<Ant,Thread>> iterator=threadMap.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<Ant,Thread> entry=iterator.next();
                if(entry.getValue().isInterrupted()){
                    iterator.remove();
                }
            }
            repaint();
            try{
                double remainingTime=nextDrawTime-System.nanoTime();
                remainingTime=remainingTime/100000;

                if (remainingTime<0){remainingTime=0;}
                sleep((long)remainingTime);
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
        for(Map.Entry<Ant,Thread> entry:threadMap.entrySet()){
            entry.getKey().draw(g2);
        }
        g2.dispose();
    }
}
