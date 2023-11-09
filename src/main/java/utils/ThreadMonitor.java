package utils;

public class ThreadMonitor implements Runnable {

    private static final int checkInterval = 1000;   // milliseconds

    @Override
    public void run() {
        int oldThreadCount = 0;

        while (true) {
            int currentThreadCount = Thread.activeCount();
            if (currentThreadCount != oldThreadCount) {
                Logger.logInfo("Number of active threads changed to: " + currentThreadCount);
                oldThreadCount = currentThreadCount;
            }

            try {
                Thread.sleep(checkInterval); // Sleep for a while before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void launch() {
        Thread monitorThread = new Thread(new ThreadMonitor());
        monitorThread.start();
        Logger.logInfo("Thread monitor started");

    }
}
