package utils;

public class ThreadMonitor implements Runnable {

    private static Thread monitorThread;
    private static final int checkInterval = 1000;   // milliseconds

    @Override
    public void run() {
        int oldThreadCount = 0;
        Logger.logInfo("Thread monitor started");

        while (monitorThread != null) {
            int currentThreadCount = Thread.activeCount();
            if (currentThreadCount != oldThreadCount) {
                Logger.logInfo("Number of active threads changed to: " + currentThreadCount);
                oldThreadCount = currentThreadCount;
            }

            try {
                Thread.sleep(checkInterval); // Sleep for a while before checking again
            } catch (InterruptedException e) {
                System.err.println("Thread monitor interrupted: " + e.getMessage());
            }
        }
    }

    public static void launch() {
        monitorThread = new Thread(new ThreadMonitor());
        monitorThread.start();
    }
}
