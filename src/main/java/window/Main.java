package window;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        JFrame window=new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setTitle("Ant Colony Simulation");

        Panel AntColony=new Panel();
        window.add(AntColony);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        AntColony.startRunThread();
    }
}