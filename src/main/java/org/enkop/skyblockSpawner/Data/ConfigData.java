package org.enkop.skyblockSpawner.Data;

import java.util.ArrayList;

public class ConfigData{
    private int islandIndex;
    private ArrayList<Integer> releasedIndices;

    // Constructor
    public ConfigData(int islandIndex, ArrayList<Integer> releasedIndices) {
        this.islandIndex = islandIndex;
        this.releasedIndices = releasedIndices;
    }

    // Getters
    public int islandIndex() {
        return islandIndex;
    }

    public ArrayList<Integer> releasedIndices() {
        return releasedIndices;
    }

    // Setters (now you can mutate!)
    public void setIslandIndex(int islandIndex) {
        this.islandIndex = islandIndex;
    }

    public void setReleasedIndices(ArrayList<Integer> releasedIndices) {
        this.releasedIndices = releasedIndices;
    }
}
