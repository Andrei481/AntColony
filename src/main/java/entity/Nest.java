package entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Nest {

    private Set<Ant> antsReady = new HashSet<>();

    public void addAntReady(Ant ant) {
        synchronized (antsReady) {
            antsReady.add(ant);
        }
    }

    public void removeAntReady(Ant ant) {
        synchronized (antsReady) {
            ant.foundFood = false;
            ant.sentReadySignal = false;
            ant.reproduce();
            antsReady.remove(ant);
        }
    }

    public Set<Ant> getAntsReady() {
        synchronized (antsReady) {
            return new HashSet<>(antsReady);
        }
    }
}