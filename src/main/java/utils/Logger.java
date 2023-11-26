package utils;

import definitions.SimulationEventType;
import entities.Ant;
import entities.Food;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Logger {

    private static final boolean consoleLoggingEnabled = true;
    private static final boolean infoLoggingEnabled = true;
    private static final boolean simulationLoggingEnabled = true;
    private static final String logDirectoryName = "logs";
    // END CONFIG
    private static final String logFileName = logDirectoryName + "/latest.log";
    private static final String olderLogsDirectoryName = logDirectoryName + "/older";
    private static final String logFileDate = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
    private static final ExecutorService loggerThread = Executors.newSingleThreadExecutor();
    // CONFIG
    private static boolean loggingEnabled;  // set in Main

    static {

        File logDirectory = new File(logDirectoryName);
        if (!logDirectory.exists()) {
            if (!logDirectory.mkdirs()) {
                System.err.println("Failed to create the log directory.");
            }
        }
        File olderLogsDirectory = new File(olderLogsDirectoryName);
        if (!olderLogsDirectory.exists()) {
            if (!olderLogsDirectory.mkdirs()) {
                System.err.println("Failed to create the older logs directory.");
            }
        }

        File logFile = new File(logFileName);
        if (logFile.exists()) {
            try (BufferedReader reader = Files.newBufferedReader(logFile.toPath())) {
                String firstLine = reader.readLine();

                if (firstLine != null) {
                    Path destinationPath = Path.of(olderLogsDirectoryName, firstLine + ".log");
                    Files.move(logFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    System.err.println("Old log file is empty.");
                }
            } catch (IOException e) {
                System.err.println("Failed to move and rename latest.log: " + e.getMessage());
            }
        }

        try (FileWriter fileWriter = new FileWriter(logFileName, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(logFileDate);
            printWriter.println();
        } catch (IOException e) {
            System.err.println("Error writing to the log file: " + e.getMessage());
        }
    }

    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    public static void logInfo(String message) {
        if (infoLoggingEnabled) log(" [INFO]       ", message);
    }

    public static void logSimulation(SimulationEventType eventType, Ant ant) {
        if (!simulationLoggingEnabled) return;

        String message = "Ant " + ant.getId() + switch (eventType) {
            case BIRTH -> " has spawned.";
            case MEAL -> " has eaten.";
            case REPRODUCTION -> " has reproduced.";
            case DEATH -> " has died.";

            default ->
                    throw new IllegalStateException("Unexpected value when logging simulation ant event: " + eventType);
        };
        StatisticsProvider.sendMessage(eventType, message);
        log(" [SIMULATION] ", message);
    }

    public static void logSimulation(SimulationEventType eventType, Food food) {
        if (!simulationLoggingEnabled) return;

        String message = "Food " + food.getId() + switch (eventType) {
            case FOOD_CREATED -> " has been created";
            case FOOD_REDUCED -> " has " + food.getQuantity() + " servings left";
            case FOOD_DEPLETED -> " has been depleted";

            default ->
                    throw new IllegalStateException("Unexpected value when logging simulation food event: " + eventType);
        };

        StatisticsProvider.sendMessage(eventType, message);
        log(" [SIMULATION] ", message);
    }

    private static void log(String level, String message) {
        if (!loggingEnabled) return;

        loggerThread.execute(() -> {
            try (FileWriter fileWriter = new FileWriter(logFileName, true);
                 PrintWriter printWriter = new PrintWriter(fileWriter)) {
                String logContent = new SimpleDateFormat("HH:mm:ss").format(new Date()) + level + message;
                printWriter.println(logContent);
                if (consoleLoggingEnabled) System.out.println(logContent);
            } catch (IOException e) {
                System.err.println("Error writing to the log file: " + e.getMessage());
            }
        });
    }
}
