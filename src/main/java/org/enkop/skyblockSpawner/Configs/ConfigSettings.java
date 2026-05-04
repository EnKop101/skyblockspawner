package org.enkop.skyblockSpawner.Configs;

import org.bukkit.World;

import java.io.File;

public record ConfigSettings(
        int regionDiameter,
        int spacing,
        int maxColumnsPerRow,
        World world,
        double yLevel,
        File schematic,
        int currentColumn,
        int currentRow
){}
