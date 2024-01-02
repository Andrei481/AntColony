package entities;

import java.util.HashSet;
import java.util.Set;

public class Nest {
    public static int nestPosX, nestPosY;
    private final Set<Ant> antsReady = new HashSet<>();

    public void addAntReady(Ant ant) {
        synchronized (antsReady) {
            antsReady.add(ant);
        }
    }

    public void removeAntReady(Ant ant) {
        synchronized (antsReady) {
            ant.sentReadySignal = false;
            antsReady.remove(ant);
        }
    }

    public Set<Ant> getAntsReady() {
        synchronized (antsReady) {
            return new HashSet<>(antsReady);
        }
    }
}