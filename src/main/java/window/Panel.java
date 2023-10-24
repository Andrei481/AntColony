package window;

import entity.Ant;
import tile.Tile_manager;

import javax.swing.*;
import java.awt.*;

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
    Tile_manager tile_manager=new Tile_manager(this);
    Thread GUIThread;
    public CollisionChecker col_checker=new CollisionChecker(this);
    Ant ant=new Ant(this);

    public Panel(){
        this.setPreferredSize(new Dimension(screenWidth,screenHeight));
        this.setBackground(Color.green);
        this.setDoubleBuffered(true);
    }


    @Override
    public void run() {
        double drawInterval=1000000000/FPS;
        double nextDrawTime=System.nanoTime()+drawInterval;
        while(GUIThread!=null){
            long currentTime=System.nanoTime();
            update();
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
    }

    public void update(){ant.update();}
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D) g;
        tile_manager.draw(g2);
        ant.draw(g2);
        g2.dispose();
    }
}
