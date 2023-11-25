package main;

import screens.SimulationScreen;
import utils.Logger;
import utils.ThreadMonitor;

public class Main {
    public static void main(String[] args) {

        Logger.setLoggingEnabled(true);
        Logger.logInfo("Program launched");
        ThreadMonitor.launch();
        SimulationScreen.launch();
    }
}