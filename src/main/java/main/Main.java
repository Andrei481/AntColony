package main;

import simulation.SimulationMain;
import utils.Logger;
import utils.ThreadMonitor;

public class Main {
    public static void main(String[] args) {

        Logger.setLoggingEnabled(true);
        Logger.logInfo("Program launched");
        //Logger.launch();
        ThreadMonitor.launch();

        SimulationMain.launch();
    }
}