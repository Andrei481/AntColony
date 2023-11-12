package entity;

import java.util.ArrayList;
import java.util.List;

public class Nest {

    private List<Ant> antsHome = new ArrayList<>();

    public void addAntHome(Ant ant) {
        synchronized (antsHome) {
            antsHome.add(ant);
        }
    }

    public void removeAntHome(Ant ant) {
        synchronized (antsHome) {
            antsHome.remove(ant);
        }
    }

    public List<Ant> getAntsHome() {
        synchronized (antsHome) {
            return new ArrayList<>(antsHome);
        }
    }
}