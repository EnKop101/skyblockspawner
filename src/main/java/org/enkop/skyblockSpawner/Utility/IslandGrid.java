package org.enkop.skyblockSpawner.Utility;

import org.enkop.skyblockSpawner.Configs.ConfigSettings;
import org.enkop.skyblockSpawner.Configs.ConfigSettingsManager;
import org.enkop.skyblockSpawner.Data.ConfigData;
import org.enkop.skyblockSpawner.Data.ConfigDataManager;
import org.enkop.skyblockSpawner.SkyblockSpawner;

import java.util.Random;

public class IslandGrid {
    private final SkyblockSpawner plugin;
    private final ConfigSettingsManager configSettingsManager;
    private final ConfigDataManager configDataManager;

    private volatile ConfigSettings configSettings;
    private volatile ConfigData configData;

    private final Random random = new Random();

    public IslandGrid(SkyblockSpawner _plugin, ConfigSettingsManager _configSettingsManager, ConfigDataManager _configDataManager){
        this.plugin = _plugin;
        this.configSettingsManager = _configSettingsManager;
        this.configDataManager = _configDataManager;

        this.configSettings = configSettingsManager.getConfigSettings();
        this.configData = configDataManager.getConfigData();
    }

    public IslandCoords allocateNext(){
        int index = configData.islandsSpawned();

        IslandCoords coords = computeCenter(index);

        configData.setIslandsSpawned(index + 1);
        configDataManager.update();

        return coords;
    }

    public IslandCoords computeCenter(int index) {
        int pitch = configSettings.regionSize() + configSettings.maxSpacing();
        int jitterRange = (configSettings.maxSpacing() - configSettings.minSpacing()) / 2;

        int[] gc = spiralCoords(index);
        int gridCenterX = configSettings.startX() + gc[0] * pitch + pitch / 2;
        int gridCenterZ = configSettings.startZ() + gc[1] * pitch + pitch / 2;

        int jitterX = random.nextInt(jitterRange * 2 + 1) - jitterRange;
        int jitterZ = random.nextInt(jitterRange * 2 + 1) - jitterRange;

        return new IslandCoords(gridCenterX + jitterX, gridCenterZ + jitterZ);
    }

    public static int[] spiralCoords(int index) {
        if (index == 0) return new int[]{0, 0};

        // Find which "ring" of the spiral we're on
        int ring = (int) Math.ceil((Math.sqrt(index + 1) - 1) / 2);

        // Length of one side of this ring (minus 1)
        int sideLength = 2 * ring;

        // Index within the ring
        int ringStart = (2 * ring - 1) * (2 * ring - 1);
        int ringIndex = index - ringStart;

        // Which side of the ring (0=right, 1=top, 2=left, 3=bottom)
        int side = ringIndex / sideLength;
        int sideOffset = ringIndex % sideLength;

        int col, row;
        row = switch (side) {
            case 0 -> {
                col = ring;
                yield -ring + 1 + sideOffset;
            }
            case 1 -> {
                col = ring - 1 - sideOffset;
                yield ring;
            }
            case 2 -> {
                col = -ring;
                yield ring - 1 - sideOffset;
            }
            case 3 -> {
                col = -ring + 1 + sideOffset;
                yield -ring;
            }
            default -> {
                col = 0;
                yield 0;
            }
        };

        return new int[]{col, row};
    }
}