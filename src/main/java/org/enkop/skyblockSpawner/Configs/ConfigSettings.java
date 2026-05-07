package org.enkop.skyblockSpawner.Configs;

import org.bukkit.World;

import java.io.File;

public class ConfigSettings {
    private int regionSize;
    private int minSpacing;
    private int maxSpacing;
    private World world;
    private double yLevel;
    private int startX;
    private int startZ;
    private File schematic;

    // Constructor
    public ConfigSettings(
            int regionSize,
            int minSpacing,
            int maxSpacing,
            World world,
            double yLevel,
            int startX,
            int startZ,
            File schematic
    )
    {
        this.regionSize = regionSize;
        this.minSpacing = minSpacing;
        this.maxSpacing = maxSpacing;
        this.world = world;
        this.yLevel = yLevel;
        this.startX = startX;
        this.startZ = startZ;
        this.schematic = schematic;
    }

    // Getters
    public int regionSize() {
        return regionSize;
    }

    public int minSpacing() {
        return minSpacing;
    }

    public int maxSpacing() {
        return maxSpacing;
    }

    public World world() {
        return world;
    }

    public double yLevel() {
        return yLevel;
    }

    public int startX() {
        return startX;
    }

    public int startZ() {
        return startZ;
    }

    public File schematic() {
        return schematic;
    }

    // Setters
    public void setRegionSize(int regionSize) {
        this.regionSize = regionSize;
    }

    public void setMinSpacing(int minSpacing) {
        this.minSpacing = minSpacing;
    }

    public void setMaxSpacing(int maxSpacing) {
        this.maxSpacing = maxSpacing;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setYLevel(double yLevel) {
        this.yLevel = yLevel;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public void setStartZ(int startZ) {
        this.startX = startZ;
    }

    public void setSchematic(File schematic) {
        this.schematic = schematic;
    }
}
