package org.enkop.skyblockSpawner.Data;

import java.util.ArrayList;

public class ConfigData{
    private int islandsSpawned;
    private ArrayList<Integer> releasedIndices;

    // Constructor
    public ConfigData(int islandsSpawned, ArrayList<Integer> releasedIndices) {
        this.islandsSpawned = islandsSpawned;
        this.releasedIndices = releasedIndices;
    }

    // Getters
    public int islandsSpawned() {
        return islandsSpawned;
    }

    public ArrayList<Integer> releasedIndices() {
        return releasedIndices;
    }

    // Setters (now you can mutate!)
    public void setIslandsSpawned(int islandsSpawned) {
        this.islandsSpawned = islandsSpawned;
    }

    public void setReleasedIndices(ArrayList<Integer> releasedIndices) {
        this.releasedIndices = releasedIndices;
    }
}
